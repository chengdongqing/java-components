package top.chengdongqing.common.pay.wxpay.v3.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.chengdongqing.common.kit.Ret;
import top.chengdongqing.common.pay.entities.PayResEntity;
import top.chengdongqing.common.pay.entities.RefundResEntity;
import top.chengdongqing.common.pay.wxpay.WxpayHelper;
import top.chengdongqing.common.pay.wxpay.WxpayStatus;
import top.chengdongqing.common.pay.wxpay.v3.WxpayConfigsV3;
import top.chengdongqing.common.pay.wxpay.v3.WxpayHelperV3;
import top.chengdongqing.common.pay.wxpay.v3.callback.entities.CallbackEntity;
import top.chengdongqing.common.pay.wxpay.v3.callback.entities.PayCallbackEntity;
import top.chengdongqing.common.pay.wxpay.v3.callback.entities.RefundCallbackEntity;

/**
 * 回调处理器
 *
 * @author Luyao
 */
@Slf4j
@Component
public class CallbackHandler implements ICallbackHandler {

    @Autowired
    private WxpayConfigsV3 v3Configs;
    @Autowired
    private WxpayHelperV3 helper;

    @Override
    public Ret<PayResEntity> handlePayCallback(CallbackEntity callback) {
        // 验证签名
        boolean verify = helper.verify(callback.getSerialNo(),
                v3Configs.getPublicKey(),
                callback.getSign(),
                callback.getTimestamp(),
                callback.getNonceStr(),
                callback.getBody());
        if (!verify) return WxpayHelperV3.buildFailCallback("验签失败");

        // 解密数据
        PayCallbackEntity payCallback = WxpayHelperV3.decryptData(callback.getBody(), v3Configs.getSecretKey(), PayCallbackEntity.class);
        log.info("支付回调解密后的数据：{}", payCallback);

        // 判断支付结果
        if (!payCallback.isTradeSuccess()) return WxpayHelperV3.buildFailCallback("交易失败");

        // 封装支付信息
        PayResEntity payResEntity = PayResEntity.builder()
                .orderNo(payCallback.getOutTradeNo())
                .paymentNo(payCallback.getTransactionId())
                // 将单位从分转为元
                .paymentAmount(WxpayHelper.convertAmount(payCallback.getAmount().getPayerTotal()))
                // 转换支付时间
                .paymentTime(WxpayHelperV3.convertTime(payCallback.getSuccessTime()))
                .build();
        // 返回回调结果
        return Ret.ok(payResEntity, WxpayHelperV3.getSuccessCallback());
    }

    @Override
    public Ret<RefundResEntity> handleRefundCallback(CallbackEntity callback) {
        // 验证签名
        boolean verify = helper.verify(callback.getSerialNo(),
                v3Configs.getPublicKey(),
                callback.getSign(),
                callback.getTimestamp(),
                callback.getNonceStr(),
                callback.getBody());
        if (!verify) return WxpayHelperV3.buildFailCallback("验签失败");

        // 解密数据
        RefundCallbackEntity refundCallback = WxpayHelperV3.decryptData(callback.getBody(), v3Configs.getSecretKey(), RefundCallbackEntity.class);
        log.info("退款回调解密后的数据：{}", refundCallback);

        // 封装退款信息
        RefundResEntity refundResEntity = RefundResEntity.builder()
                .orderNo(refundCallback.getOutTradeNo())
                .refundNo(refundCallback.getOutRefundNo())
                .refundId(refundCallback.getRefundId())
                .refundAmount(WxpayHelper.convertAmount(refundCallback.getAmount().getRefund()))
                .refundTime(WxpayHelperV3.convertTime(refundCallback.getSuccessTime()))
                .success(WxpayStatus.isOk(refundCallback.getRefundStatus().name()))
                .build();
        return Ret.ok(refundResEntity, WxpayHelperV3.getSuccessCallback());
    }
}