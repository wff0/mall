package com.wff.common.to.mq;

import lombok.Data;

/**
 * @author wff
 * @email wff1128@foxmail.com
 * @date 2021/6/1 19:51
 */
@Data
public class StockLockedTo {
    /**
     * 库存工作单id
     */
    private Long id;

    /**
     * 工作详情id
     */
    private StockDetailTo detailTo;
}
