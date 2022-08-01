package com.ecommerce.modules.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class AttrRespVo extends AttrVo {
    private String catelogName;
    private String groupName;
}
