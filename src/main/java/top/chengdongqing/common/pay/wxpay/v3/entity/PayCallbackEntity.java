package top.chengdongqing.common.pay.wxpay.v3.entity;

import lombok.Getter;
import lombok.Setter;
import top.chengdongqing.common.pay.wxpay.WxpayStatus;

/**
 * 支付回调数据实体
 *
 * @author Luyao
 */
@Getter
@Setter
public class PayCallbackEntity {

    /**
     * 微信平台的交易编号
     */
    private String transactionId;
    /**
     * 交易状态
     */
    private String tradeState;
    /**
     * 支付成功时间
     */
    private String successTime;
    /**
     * 订单号
     */
    private String outTradeNo;
    /**
     * 交易类型
     */
    private String tradeType;
    /**
     * 交易金额
     */
    private Amount amount;

    @Getter
    @Setter
    public static class Amount {
        /**
         * 用户实际支付总金额
         */
        private Integer payerTotal;
        /**
         * 订单总金额
         */
        private Integer total;
    }

    /**
     * 是否交易成功
     */
    public boolean isTradeSuccess() {
        return WxpayStatus.isOk(this.getTradeState());
    }
}
