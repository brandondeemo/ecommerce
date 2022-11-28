package com.ecommerce.modules.coupon.service.impl;

import com.ecommerce.common.to.MemberPrice;
import com.ecommerce.common.to.SkuReductionTo;
import com.ecommerce.modules.coupon.entity.MemberPriceEntity;
import com.ecommerce.modules.coupon.entity.SkuLadderEntity;
import com.ecommerce.modules.coupon.service.MemberPriceService;
import com.ecommerce.modules.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.common.utils.PageUtils;
import com.ecommerce.common.utils.Query;

import com.ecommerce.modules.coupon.dao.SkuFullReductionDao;
import com.ecommerce.modules.coupon.entity.SkuFullReductionEntity;
import com.ecommerce.modules.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        // 保存满减打折，会员价
        // 保存 sku 的优惠 减免等信息： (1) sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());

        // 打折价大于 0，才做保存
        if (skuReductionTo.getFullCount() > 0) {
            skuLadderService.save(skuLadderEntity);
        }

        // (2) sms_sku_full_reduction
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, skuFullReductionEntity);

        if (skuFullReductionEntity.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
            this.save(skuFullReductionEntity);
        }

        // (3) sms_member_price
        List<MemberPrice> memberPrices = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrices.stream().map(item -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
            memberPriceEntity.setMemberLevelId(item.getId());
            memberPriceEntity.setMemberLevelName(item.getName());
            memberPriceEntity.setMemberPrice(item.getPrice());
            /*
            setAddOther 是否叠加其他优惠
            这里默认 1，叠加其他优惠
             */
            memberPriceEntity.setAddOther(1);

            return memberPriceEntity;
        }).filter(item -> {
            return item.getMemberPrice().compareTo(new BigDecimal("0")) == 1;
        }).collect(Collectors.toList());

        memberPriceService.saveBatch(collect);
    }

}