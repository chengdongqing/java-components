package top.chengdongqing.common.pay;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付相关公共配置
 *
 * @author Luyao
 */
@Getter
@Setter
@Component
@ConfigurationProperties("pay")
public class PayProps {

    /**
     * 支付超时配置，单位：分钟
     */
    private Long timeout;

    /**
     * 网站域名
     */
    private String webDomain;
}
