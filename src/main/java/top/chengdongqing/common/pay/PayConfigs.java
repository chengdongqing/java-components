package top.chengdongqing.common.pay;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 支付相关公共配置
 *
 * @author Luyao
 */
@Getter
@Component
public class PayConfigs {

    /**
     * 支付超时配置，单位：分钟
     */
    @Value("${timeout:30}")
    private Long timeout;

    /**
     * 网站标题
     */
    private String webTitle;
    /**
     * 网站域名
     */
    private String webDomain;
}