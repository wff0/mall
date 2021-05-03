package com.wff.mall.member.dao;

import com.wff.mall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author wangfengfan
 * @email 1098137961@qq.com
 * @date 2021-05-03 11:33:49
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
