package com.ecommerce.modules.product.service.impl;

import com.ecommerce.common.to.SkuReductionTo;
import com.ecommerce.common.to.SpuBoundTo;
import com.ecommerce.common.utils.R;
import com.ecommerce.modules.product.entity.*;
import com.ecommerce.modules.product.feign.CouponFeignService;
import com.ecommerce.modules.product.service.*;
import com.ecommerce.modules.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.common.utils.PageUtils;
import com.ecommerce.common.utils.Query;

import com.ecommerce.modules.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService imagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();

        // 1. 保存 spu 基本信息: pms_spu_info
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());

        this.saveBaseSpuInfo(spuInfoEntity);

        // 2。 保存 spu 的描述图片: pms_spu_info_desc
        /*
        spu 的描述图片和 描述信息 用两个单独的 sql 表来储存
        使得两者和 spu 的基本信息分离开
        因为 图片和 描述信息 可能会很大，所以把大的字段单独提取出来
         */
        List<String> decripts = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());
        descEntity.setDecript(String.join(",", decripts));

        spuInfoDescService.saveSpuInfoDesc(descEntity);

        // 3. 保存 spu 的图片集: pms_spu_images
        List<String> images = vo.getImages();
        imagesService.saveImages(spuInfoEntity.getId(), images);

        // 4. 保存 spu 的规格参数：pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity id = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(id.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(spuInfoEntity.getId());

            return valueEntity;
        }).collect(Collectors.toList());

        productAttrValueService.saveProductAttr(collect);

        // 5. 保存 spu 的积分信息：sms_spu_bounds(积分表)
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());

        R r = couponFeignService.saveSpuBounds(spuBoundTo);

        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }

        // 6. 保存 spu 的对应的所有sku 信息:
        // 6.1  保存 sku 的基本信息： pms_sku_info
        List<Skus> skus = vo.getSkus();

        if (skus != null && skus.size() != 0) {
            skus.forEach(sku ->{
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);

                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                // 销量开始默认初始化为 0
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());

                // 找出默认图片
                String defaultImage = "";
                for (Images image : sku.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImage = image.getImgUrl();
                    }
                }

                skuInfoEntity.setSkuDefaultImg(defaultImage);
                skuInfoService.saveSkuInfo(skuInfoEntity);

                // 6.2  保存 sku 的图片信息： pms_sku_images
                // skuId: 自增主键
                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imageEntities = sku.getImages().stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(image.getImgUrl());
                    skuImagesEntity.setDefaultImg(image.getDefaultImg());

                    return skuImagesEntity;
                }).filter(entity -> {
                    // 返回 true 就是需要，false就是没有设置图片，不需要
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());

                skuImagesService.saveBatch(imageEntities);

                // 6.3  保存 sku 的销售属性信息： pms_sku_sale_attr_value
                List<Attr> attrs = sku.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map(attr -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);

                    return attrValueEntity;
                }).collect(Collectors.toList());

                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // 6.4  保存 sku 的优惠 减免等信息： (1) sms_sku_ladder
                // (2) sms_sku_full_reduction  (3) sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);

                /*
                这里数据判断
                FullCount 和 FullPrice 必须大于 0才行
                因为 FullPrice 的类型是 big decimal
                必须要用 compareTo(new BigDecimal("0")) == 1 形式进行判断大小
                而不能简单的 大于等于
                 */
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku 优惠积分信息失败");
                    }
                }
            } );
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity infoEntity) {
        this.baseMapper.insert(infoEntity);
    }


}