package com.ecommerce.modules.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
// 二级分类 vo
public class Catalog2Vo {
    private String catalog1Id; // 1级父分类
    private List<Catalog3Vo> catalog3List; // 3级子分类
    private String id;
    private String name;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    // 三级分类 vo
    public static class Catalog3Vo {
        private String catalog2Id;
        private String id;
        private String name;
    }

}
