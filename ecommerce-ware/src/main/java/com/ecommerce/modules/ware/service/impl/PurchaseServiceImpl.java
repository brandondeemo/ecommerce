package com.ecommerce.modules.ware.service.impl;

import com.ecommerce.common.constant.WareConstant;
import com.ecommerce.modules.ware.entity.PurchaseDetailEntity;
import com.ecommerce.modules.ware.service.PurchaseDetailService;
import com.ecommerce.modules.ware.service.WareSkuService;
import com.ecommerce.modules.ware.vo.MergeVo;
import com.ecommerce.modules.ware.vo.PurchaseDoneItemVo;
import com.ecommerce.modules.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ecommerce.common.utils.PageUtils;
import com.ecommerce.common.utils.Query;

import com.ecommerce.modules.ware.dao.PurchaseDao;
import com.ecommerce.modules.ware.entity.PurchaseEntity;
import com.ecommerce.modules.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceived(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();

        //
        if (purchaseId != null) {
            // 确认采购单状态为0或者1
            PurchaseEntity purchaseEntity = baseMapper.selectById(purchaseId);
            if (purchaseEntity.getStatus() != WareConstant.PurchaseStatusEnum.CREATED.getCode() || purchaseEntity.getStatus() != WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                log.error("采购单状态不对，无法合并");
                return;
            }
        } else {
            // 不存在采购单，新建采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());

            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());

            this.save(purchaseEntity);

            purchaseId = purchaseEntity.getId();
        }

        List<Long> items = mergeVo.getItems();
        /**
         * 这里必须复制一个 final，否则purchaseId 作为参数
         * 传入 setPurchaseId 会报错
         */
        Long finalPurchaseId = purchaseId;

        List<PurchaseDetailEntity> collect = items.stream().map(item -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();

            detailEntity.setId(item);
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());

            return detailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    /**
     *
     * @param ids: 采购单的 id 集合
     */
    @Override
    public void received(List<Long> ids) {
        // 1. 确认当前采购单是新增或者已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity purchaseEntity = this.getById(id);
            return purchaseEntity;
        }).filter(item -> {
            Integer status = item.getStatus();

            if (status == WareConstant.PurchaseStatusEnum.CREATED.getCode() ||
                    status == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            } else {
                return false;
            }
        }).map(item -> {
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        // 2. 改变采购单的状态
        this.updateBatchById(collect);

        // 3. 改变采购项的状态
        collect.forEach(item -> {
            List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchaseId(item.getId());

            // 获取采购单后，更改每一项的状态
            List<PurchaseDetailEntity> purchaseDetailEntities = entities.stream().map(entity -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(entity.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());

                return purchaseDetailEntity;
            }).collect(Collectors.toList());

            purchaseDetailService.updateBatchById(purchaseDetailEntities);
        });
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {

        // 1. 改变采购项的状态
        Boolean flag = true;
        List<PurchaseDoneItemVo> items = purchaseDoneVo.getItems();
        List<PurchaseDetailEntity> updates = new ArrayList<>();

        for (PurchaseDoneItemVo item : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();

            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag = false;
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode());
            } else {
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISHED.getCode());
                // 3. 将成功采购的进行入库
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                /**
                 * 把哪个商品 entity.getSkuId()
                 * 放到哪个仓库 entity.getWareId()
                 * 添加几个数量 entity.getSkuNum()
                 */
                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
            }

            purchaseDetailEntity.setId(item.getItemId());
            updates.add(purchaseDetailEntity);
        }

        purchaseDetailService.updateBatchById(updates);

        // 2. 改变采购单状态
        Long id = purchaseDoneVo.getId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseDetailStatusEnum.FINISHED.getCode() : WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());

        this.updateById(purchaseEntity);
    }

}