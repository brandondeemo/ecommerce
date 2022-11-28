package com.ecommerce.modules.product.controller;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.ecommerce.modules.product.entity.AttrEntity;
import com.ecommerce.modules.product.service.AttrAttrgroupRelationService;
import com.ecommerce.modules.product.service.AttrService;
import com.ecommerce.modules.product.service.CategoryService;
import com.ecommerce.modules.product.vo.AttrGroupRelationVo;
import com.ecommerce.modules.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.modules.product.entity.AttrGroupEntity;
import com.ecommerce.modules.product.service.AttrGroupService;
import com.ecommerce.common.utils.PageUtils;
import com.ecommerce.common.utils.R;


/**
 * 属性分组
 *
 * @author Wonpyeong Son
 * @email sonwonpyeong@yonsei.ac.kr
 * @date 2021-12-15 22:23:16
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    AttrService attrService;

    @Autowired
    AttrAttrgroupRelationService relationService;

    // /product/attrgroup/{attrgroupId}/attr/relation

    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId) {
        List<AttrEntity> entities = attrService.getRelationAttr(attrgroupId);

        return R.ok().put("data", entities);
    }

    // /product/attrgroup/{attrgroupId}/noattr/relation

    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId,
                            @RequestParam Map<String, Object> params) {

        PageUtils page = attrService.getNoRelationAttr(params, attrgroupId);

        return R.ok().put("page", page);
    }

    // /product/attrgroup/attr/relation

    /**
     * 新增关联关系
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos) {
        relationService.saveBatch(vos);

        return R.ok();
    }

    // 获取分类下所有分组&关联属性
    // /product/attrgroup/{catelogId}/withattr
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId")Long catelogId) {
        // 1. 查出当前分类下的所有属性分组
        // 2. 查处每个属性分组的所有属性

        List<AttrGroupWithAttrsVo> vos = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);

        return R.ok().put("data", vos);
    }


    /**
     * 删除
     *
     * @param vos
     * @return
     */
    // /product/attrgroup/attr/relation/delete
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos) {
        attrService.deleteRelation(vos);

        return R.ok();
    }


    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId") Long catelogId) {
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCategoryPath(catelogId);

        attrGroup.setCatelogPath(path);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
