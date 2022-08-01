package com.ecommerce.modules.product.service.impl;

import com.ecommerce.modules.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

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
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updatecCategory(category.getCatId(), category.getName());

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