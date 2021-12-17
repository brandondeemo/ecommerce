package com.ecommerce.modules.member.dao;

import com.ecommerce.modules.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author Wonpyeong Son
 * @email sonwonpyeong@yonsei.ac.kr
 * @date 2021-12-17 01:52:24
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
