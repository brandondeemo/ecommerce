package com.ecommerce.modules.coupon.dao;

import com.ecommerce.modules.coupon.entity.HomeAdvEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 首页轮播广告
 * 
 * @author Wonpyeong Son
 * @email sonwonpyeong@yonsei.ac.kr
 * @date 2021-12-17 01:44:08
 */
@Mapper
public interface HomeAdvDao extends BaseMapper<HomeAdvEntity> {
	
}
