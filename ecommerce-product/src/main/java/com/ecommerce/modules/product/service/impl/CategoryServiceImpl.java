package com.ecommerce.modules.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ecommerce.modules.product.service.CategoryBrandRelationService;
import com.ecommerce.modules.product.vo.Catalog2Vo;
import io.netty.util.internal.StringUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.common.utils.PageUtils;
import com.ecommerce.common.utils.Query;

import com.ecommerce.modules.product.dao.CategoryDao;
import com.ecommerce.modules.product.entity.CategoryEntity;
import com.ecommerce.modules.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        /*
        功能：
        第一步：查出所有的分类
         */
        // baseMapper就是 categoryDao
        // param 为null 就是查询所有分类
        // entities是所有的分类（1 2 3级...）
        List<CategoryEntity> entities = baseMapper.selectList(null);

        /*
        第二步：
        组装成父子的树形结构

        2.1
        找到所有的一级分类
        parent_cid = 0
         */

        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == 0;
        }).map(menu -> {
            menu.setChildren(getChildren(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            // 将已经找到子菜单的categories们排序
            // 因为 menu1.getSort()返回值为Integer 类型，这里需要判断是否为null
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 1.检查当前删除的菜单，是否被别的地方引用

        // 逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCategoryPath(Long catelogId) {
        List<Long> path = new ArrayList<>();

        List<Long> parentPath = findParentPath(catelogId, path);
        Collections.reverse(parentPath);

        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     *
     * @param category
     * @CacheEvict: 缓存失效模式
     */
    // 方式1：
//    @Caching(evict = {
//            @CacheEvict(value = {"category"}, key = "'Level1Categories'"),
//            @CacheEvict(value = {"category"}, key = "'getCatalogJSON'")
//    })

    // 方式2：allEntries = true, 指定删除某个分区下的所有数据
    @CacheEvict(value = "category", allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updatecCategory(category.getCatId(), category.getName());

        /*
        更新后删除缓存，等待下次主动查询进行更新
         */

    }

    /*
    1. 每一个需要缓存的数据我们都要指定要放到哪个名字的缓存 [缓存的分区(按照业务类型分)]
    2. @Cacheable({"category"})
       代表当前方法的结果需要缓存，如果缓存中有，方法不用调用。
       如果缓存中没有，调用方法，最后将方法的结果放入缓存
    3. 默认行为
       1) 如果缓存中有，方法不需再调用
       2) key 默认自动生成: 缓存的名字 ::SimpleKey [] (自主生成的 key值)
       3) 缓存的 value 的值，默认使用 jdk序列化机制，将序列化后的数据存到 redis
       4）默认ttl 时间 -1:

    4. 自定义
       1) 指定生成的缓存使用的key
       2) 指定缓存的数据的存活时间: 配置文件中修改 ttl
       3) 将数据保存为json格式

    5. spring-cache 总结
     1. 读模式：
        缓存穿透：查询一个 null 数据
        解决：缓存空数据 spring.cache.redis.cache-null-values=true
        缓存击穿: 大量并发进来同时查询一个正好过期的数据
        解决: 加锁？

        默认不加锁
        sync = true 才加 本地锁

        缓存雪崩: 大量的 key 同时到期
        解决: 加随机时间
        加上过期时间 spring.cache.redis.time-to-live=3600000


     2。 写模式:
        1) 读写加锁
        2) 引入 canal, 感知到 MySQL 的更新去更新数据库
        3) 读多写多，直接去数据库查询

     总结：
      常规数据(读多写少。即时性、一致性要求不高的数据)： 完全可以使用 spring-cache

      特殊数据: 特殊设计
     */
    @Cacheable(value = "category", key = "#root.methodName", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categories() {

        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));

        return categoryEntities;
    }

    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJSON() {
        // querywrapper 为 null，查询所有数据
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        // 1. 查出所有1级分类
        List<CategoryEntity> level1Categories = getParent_cid(selectList, 0L);

        // 2. 封装数据
        Map<String, List<Catalog2Vo>> parent_cid = level1Categories.stream().collect(Collectors.toMap(key -> key.getCatId().toString(), value -> {
            // 1. 每一个的一级分类， 查询这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, value.getCatId());
            List<Catalog2Vo> catalog2Vos = null;

            // 2. 封装上面的结果
            if (categoryEntities != null) {
                catalog2Vos = categoryEntities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(value.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());

                    // 1. 找当前二级分类的三级分类，封装成 vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catalog2Vo.Catalog3Vo> collect = level3Catelog.stream().map(l3 -> {
                            // 2. 封装成指定格式
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return catalog3Vo;
                        }).collect(Collectors.toList());

                        catalog2Vo.setCatalog3List(collect);
                    }

                    return catalog2Vo;
                }).collect(Collectors.toList());
            }

            return catalog2Vos;
        }));

        return parent_cid;
    }

    public Map<String, List<Catalog2Vo>> getCatalogJSON2() {
        // 给缓存中放 json 字符串，拿出的 json 字符串，还得逆转为能用的对象类型
        // 序列化与反序列化过程

        /**
         * 1. 空结果缓存：解决缓存穿透问题
         * 2. 设置过期时间(加随机值): 解决缓存雪崩
         * 3. 加锁: 解决缓存击穿
         */

        // 1. 加入缓存逻辑, 缓存中存的数据是 Json 字符串
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            // 2. 缓存中没有，从数据库中查询
            Map<String, List<Catalog2Vo>> catalogJSONFromDB = getCatalogJSONFromDBWithRedissonLock();
            return catalogJSONFromDB;
        }

        // 转为我们指定的对象
        Map<String, List<Catalog2Vo>> res = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
        });

        return res;
    }

    /*
从数据库 查询并封装分类数据
 */
    public Map<String, List<Catalog2Vo>> getCatalogJSONFromDBWithLocalLock() {

        /**
         * 优化：
         * 1. 将数据库的多次查询变为一次
         */

        /*
        加锁：
        只要是同一把锁，就能锁住需要这个锁的所有线程
        synchronized (this): springboot 所有的组件在容器中都是单例的
         */
        synchronized (this) {

            // 得到锁之后，英国再去缓存中确定一次，缓存中没有才需要继续查询
            return getDataFromDB();
        }
    }

    /**
     * 缓存里面的数据如何和数据库保持一致？
     * 缓存数据一致性
     * <p>
     * 1. 双写模式
     * 2. 失效模式
     *
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJSONFromDBWithRedissonLock() {

        // 1. 锁的名字， 锁的粒度越细越快
        // 锁的粒度：具体缓存的是某个数据，11号商品 product-11-lock
        RLock lock = redissonClient.getLock("catalogJson-lock");

        lock.lock();

        Map<String, List<Catalog2Vo>> dataFromDB;
        try {
            dataFromDB = getDataFromDB();
        } finally {
            lock.unlock();
        }

        return dataFromDB;
    }

    public Map<String, List<Catalog2Vo>> getCatalogJSONFromDBWithRedisLock() {

        /**
         * 1. 占分布式锁，去 redis 占坑
         */
        String uuid = UUID.randomUUID().toString();

        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            // 加锁成功... 执行业务
            /**
             * 2. 设置锁的过期时间
             * 为什么？ 因为如果业务 getDataFromDB 出错崩了
             * 就无法执行 stringRedisTemplate.delete("lock");
             * 锁会一直存在，无法被别的进程占用
             *
             * 不能 stringRedisTemplate.expire("lock", 30, TimeUnit.SECONDS); 这么写
             * 因为不是原子的
             * 从 if (lock) 到 stringRedisTemplate.expire("lock", 30, TimeUnit.SECONDS);
             * 期间如果出了问题，也会造成永久死锁情况
             *
             * 设置过期时间必须和加锁是同步的，原子的
             * set ex nx
             */
            Map<String, List<Catalog2Vo>> dataFromDB;
            try {
                dataFromDB = getDataFromDB();
            } finally {
                /**
                 * 执行过后， 释放删除锁
                 * stringRedisTemplate.delete("lock");
                 * 不能直接这么写
                 * 如果业务时间过长，锁自己过期了，我们直接删除，有可能把别人正在持有的锁删除了
                 *
                 * 解决方法：占锁的时候，加上 Uuid，每个人匹配是自己的锁才删除
                 *
                 * 获取值对比+对比成功删除 = 原子操作
                 * LUA 脚本解锁
                 */
//            String lockValue = stringRedisTemplate.opsForValue().get("lock");
//            if (uuid.equals(lockValue)) {
//                // 删除我自己的锁
//                stringRedisTemplate.delete("lock");
//            }

                String luaScript = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";

                // 删除锁
                Long lock1 = stringRedisTemplate.execute(new DefaultRedisScript<Long>(luaScript, Long.class), Arrays.asList("lock"), uuid);
            }

            return dataFromDB;

        } else {
            // 加锁失败... 重试.
            // 休眠 100 ms 再重试
            try {
                Thread.sleep(200);
            } catch (Exception e) {

            }
            return getCatalogJSONFromDBWithRedisLock(); // 自旋的方式
        }

    }

    private Map<String, List<Catalog2Vo>> getDataFromDB() {
        // 得到锁之后，应该再去缓存中确定一次，缓存中没有才需要继续查询
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            // 缓存不为空，直接返回
            Map<String, List<Catalog2Vo>> res = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });

            return res;
        }

        // querywrapper 为 null，查询所有数据
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        // 1. 查出所有1级分类
        List<CategoryEntity> level1Categories = getParent_cid(selectList, 0L);

        // 2. 封装数据
        Map<String, List<Catalog2Vo>> parent_cid = level1Categories.stream().collect(Collectors.toMap(key -> key.getCatId().toString(), value -> {
            // 1. 每一个的一级分类， 查询这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, value.getCatId());
            List<Catalog2Vo> catalog2Vos = null;

            // 2. 封装上面的结果
            if (categoryEntities != null) {
                catalog2Vos = categoryEntities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(value.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());

                    // 1. 找当前二级分类的三级分类，封装成 vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catalog2Vo.Catalog3Vo> collect = level3Catelog.stream().map(l3 -> {
                            // 2. 封装成指定格式
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return catalog3Vo;
                        }).collect(Collectors.toList());

                        catalog2Vo.setCatalog3List(collect);
                    }

                    return catalog2Vo;
                }).collect(Collectors.toList());
            }

            return catalog2Vos;
        }));

        // 3. 查到的数据放入缓存, 将对象转换成 json 放在缓存中
        String s = JSON.toJSONString(parent_cid);
        stringRedisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);

        return parent_cid;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
//        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", value.getCatId()));
        List<CategoryEntity> collect = selectList.stream().filter(item -> {
            return item.getParentCid() == parent_cid;
        }).collect(Collectors.toList());

        return collect;
    }

    private List<Long> findParentPath(Long catelogId, List<Long> path) {
        // 1.收集当前节点id
        path.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);

        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), path);
        }

        return path;
    }


    /**
     * 递归查找所有菜单的子菜单
     *
     * @param root：当前菜单
     * @param all：所有菜单
     * @return
     */
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            // 递归找到子菜单
            categoryEntity.setChildren(getChildren(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            // 菜单的排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());


        return children;
    }

}