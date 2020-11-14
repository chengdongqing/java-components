package top.chengdongqing.common.payment.wxpay;

import java.math.BigDecimal;

/**
 * 微信支付工具类
 *
 * @author Luyao
 */
public class WxPayHelper {

    /**
     * 转换金额，从元转为分
     *
     * @param amount 金额，单位元
     * @return 金额，单位分
     */
    public static int convertAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).intValue();
    }

    /**
     * 转换金额，从分转为元
     *
     * @param amount 金额，单位分
     * @return 金额，单位元
     */
    public static BigDecimal convertAmount(int amount) {
        return BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(100));
    }

    /**
     * 获取时间戳
     * 即从1970年1月1日00:00:00至今的秒数
     *
     * @return 时间戳
     */
    public static String getTimestamp() {
        return System.currentTimeMillis() / 1000 + "";
    }
}
