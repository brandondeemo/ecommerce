/**
  * Copyright 2022 json.cn 
  */
package com.ecommerce.modules.product.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Auto-generated: 2022-11-26 21:37:58
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */

@Data
public class MemberPrice {

    // id: 会员等级 id
    private Long id;
    private String name;
    private BigDecimal price;

}