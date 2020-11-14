package top.chengdongqing.common.payment.wxpay.v3.callback;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.chengdongqing.common.kit.Ret;
import top.chengdongqing.common.payment.entity.PayResEntity;
import top.chengdongqing.common.payment.entity.RefundResEntity;
import top.chengdongqing.common.payment.wxpay.WxPayHelper;
import top.chengdongqing.common.payment.wxpay.WxStatus;
import top.chengdongqing.common.payment.wxpay.v3.WxV3Constants;
import top.chengdongqing.common.payment.wxpay.v3.WxV3Helper;
import top.chengdongqing.common.payment.wxpay.v3.callback.entity.CallbackEntity;
import top.chengdongqing.common.payment.wxpay.v3.callback.entity.PayCallbackEntity;
import top.chengdongqing.common.payment.wxpay.v3.callback.entity.RefundCallbackEntity;

/**
 * 回调处理器
 *
 * @author Luyao
 */
@Slf4j
@Component
public class CallbackHandler implements ICallbackHandler {

    @Autowired
    private WxV3Constants v3Constants;
    @Autowired
    private WxV3Helper helper;

    @Override
    public Ret handlePayCallback(CallbackEntity callback) {
        // 验证签名
        boolean verify = helper.verify(callback.getSerialNo(),
                v3Constants.getPublicKey(),
                callback.getSign(),
                callback.getTimestamp(),
                callback.getNonceStr(),
                callback.getBody());
        if (!verify) return WxV3Helper.buildFailRes("验签失败");

        // 解密数据
        PayCallbackEntity payCallback = WxV3Helper.decryptData(callback.getBody(), v3Constants.getSecretKey(), PayCallbackEntity.class);
        log.info("支付回调解密后的数据：{}", payCallback);

        // 判断支付结果
        if (!payCallback.isTradeSuccess()) return WxV3Helper.buildFailRes("交易失败");

        // 封装支付信息
        PayResEntity payResEntity = PayResEntity.builder()
                .orderNo(payCallback.getOutTradeNo())
                .paymentNo(payCallback.getTransactionId())
                // 将单位从分转为元
                .paymentAmount(WxPayHelper.convertAmount(payCallback.getAmount().getPayerTotal()))
                // 转换支付时间
                .paymentTime(WxV3Helper.convertTime(payCallback.getSuccessTime()))
                .build();
        // 返回回调结果
        return Ret.ok(PayCallbackResEntity.builder()
                .response(WxV3Helper.buildSuccessMsg())
                .payResEntity(payResEntity)
                .build());
    }

    @Override
    public Ret handleRefundCallback(CallbackEntity callback) {
        // 验证签名
        boolean verify = helper.verify(callback.getSerialNo(),
                v3Constants.getPublicKey(),
                callback.getSign(),
                callback.getTimestamp(),
                callback.getNonceStr(),
                callback.getBody());
        if (!verify) return WxV3Helper.buildFailRes("验签失败");

        // 解密数据
        RefundCallbackEntity refundCallback = WxV3Helper.decryptData(callback.getBody(), v3Constants.getSecretKey(), RefundCallbackEntity.class);
        log.info("退款回调解密后的数据：{}", refundCallback);

        // 封装退款信息
        RefundResEntity refundResEntity = RefundResEntity.builder()
                .orderNo(refundCallback.getOutTradeNo())
                .refundNo(refundCallback.getOutRefundNo())
                .refundId(refundCallback.getRefundId())
                .refundAmount(WxPayHelper.convertAmount(refundCallback.getAmount().getRefund()))
                .refundTime(WxV3Helper.convertTime(refundCallback.getSuccessTime()))
                .success(WxStatus.isOk(refundCallback.getRefundStatus().name()))
                .build();
        return Ret.ok(RefundCallbackResEntity.builder()
                .response(WxV3Helper.buildSuccessMsg())
                .refundResEntity(refundResEntity)
                .build());
    }

    /**
     * 支付回调响应实体
     */
    @Data
    @Builder
    public static class PayCallbackResEntity {
        /**
         * 响应数据
         */
        private String response;
        /**
         * 支付详情
         */
        private PayResEntity payResEntity;
    }

    /**
     * 退款回调响应实体
     */
    @Data
    @Builder
    public static class RefundCallbackResEntity {
        /**
         * 响应数据
         */
        private String response;
        /**
         * 退款详情
         */
        private RefundResEntity refundResEntity;
    }
}
