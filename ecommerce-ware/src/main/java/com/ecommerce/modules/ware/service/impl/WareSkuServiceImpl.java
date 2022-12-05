package com.ecommerce.modules.ware.service.impl;

import com.ecommerce.common.utils.R;
import com.ecommerce.modules.ware.feign.ProductFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.common.utils.PageUtils;
import com.ecommerce.common.utils.Query;

import com.ecommerce.modules.ware.dao.WareSkuDao;
import com.ecommerce.modules.ware.entity.WareSkuEntity;
import com.ecommerce.modules.ware.service.WareSkuService;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /*
        增加查询条件：
        skuId:
        wareId:
         */
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();

        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 1. 判断如果还没有这个库存记录，则新增

        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            // 默认锁定库存设置为 0
            wareSkuEntity.setStockLocked(0);

            /*
            远程查询 sku 的名字，如果失败，整个事务无需回滚
            方法 1：
            自己 catch 异常，catch内什么也不用放
             */

            // TODO 还有什么方法让异常出现后不回滚？高级篇
            try {
                R info = productFeignService.info(skuId);
                if (info.getCode() == 0) { // 查询成功
                    Map<String, Object> map = (Map<String, Object>) info.get("skuInfo");

                    wareSkuEntity.setSkuName((String) map.get("skuName"));
                }
            } catch (Exception e) {

            }

            wareSkuDao.insert(wareSkuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }

    }

}