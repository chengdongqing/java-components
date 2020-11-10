package top.chengdongqing.common.jwt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import top.chengdongqing.common.signature.DigitalSigner;
import top.chengdongqing.common.signature.SignatureAlgorithm;
import top.chengdongqing.common.transformer.StrToBytes;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

/**
 * token生成与验证工具
 *
 * @author Luyao
 */
@Component
public class JwtOperator {

    @Autowired
    private JwtConstants constants;
    /**
     * 签名算法
     */
    private static final SignatureAlgorithm ALGORITHM = SignatureAlgorithm.EdDSA_ED25519;

    /**
     * 生成token
     * token结构：base64(headers).base64(payloads).base64(signature)
     * 待签名内容：signature(base64(headers.toJson()).base64(payloads.toJson()))
     *
     * @param payloads 有效载荷
     * @return token对象
     */
    public JwtInfo generate(JSONObject payloads) {
        if (payloads == null || payloads.isEmpty()) throw new IllegalArgumentException("The payloads cannot be empty.");

        // 头部信息
        JwtHeader header = new JwtHeader();
        // 签名算法
        header.setAlgorithm(ALGORITHM.getAlgorithm());
        // 签发时间
        Instant now = Instant.now();
        header.setIssueTime(now.toEpochMilli());
        // 过期时间
        Instant expiryTime = now.plus(constants.getDuration(), ChronoUnit.MINUTES);
        header.setExpiryTime(expiryTime.toEpochMilli());
        // 拼接待签名内容
        Base64.Encoder encoder = Base64.getUrlEncoder();
        String content = encoder.encodeToString(header.toJson()) + "." + encoder.encodeToString(JSON.toJSONBytes(payloads));
        // 执行签名
        String signature = DigitalSigner.signature(ALGORITHM, content,
                StrToBytes.of(constants.getPrivateKey()).fromBase64()).toBase64();
        content += "." + signature;
        // 返回token详情
        return JwtInfo.builder()
                .token(content)
                .algorithm(ALGORITHM)
                .issueTime(now.atZone(ZoneId.systemDefault()).toLocalDateTime())
                .expiryTime(expiryTime.atZone(ZoneId.systemDefault()).toLocalDateTime())
                .payloads(payloads)
                .signature(signature)
                .build();
    }

    /**
     * 验证token是否有效
     *
     * @param token token
     * @return 是否有效
     */
    public boolean verify(String token) {
        String[] parts = getParts(token);

        // 获取被签名的数据
        String content = parts[0] + "." + parts[1];

        // 验签
        boolean verified = DigitalSigner.verify(ALGORITHM, content,
                StrToBytes.of(constants.getPublicKey()).fromBase64(),
                StrToBytes.of(parts[2]).fromBase64());
        if (!verified) return false;

        // 将头部解码并转JwtHeader对象
        JwtHeader header = JSON.parseObject(Base64.getUrlDecoder().decode(parts[0]), JwtHeader.class);

        // 获取过期时间并判断是否过期
        return Instant.ofEpochMilli(header.getExpiryTime()).isAfter(Instant.now());
    }

    /**
     * 将token根据点分成3部分
     */
    private String[] getParts(String token) {
        if (StringUtils.isBlank(token)) throw new IllegalArgumentException("The token cannot be blank");
        String[] parts = token.split("\\.");
        if (parts.length != 3) throw new IllegalArgumentException("The token is wrong.");
        return parts;
    }

    /**
     * 从token中获取有效载荷
     *
     * @param token token
     * @return 有效载荷
     */
    public JSONObject getPayloads(String token) {
        String[] parts = getParts(token);
        JSONObject object = JSON.parseObject(new String(Base64.getUrlDecoder().decode(parts[1])));
        if (object == null || object.isEmpty()) throw new IllegalStateException("token解析失败");
        return object;
    }
}

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "jwt")
class JwtConstants {

    /**
     * 公钥，验签用
     */
    private String publicKey;
    /**
     * 私钥，签名用
     */
    private String privateKey;
    /**
     * 有效时长，单位：分钟
     */
    private Long duration;
}

@Data
class JwtHeader {

    /**
     * 签名算法
     */
    private String algorithm;
    /**
     * 签发时间
     */
    private Long issueTime;
    /**
     * 过期时间
     */
    private Long expiryTime;

    /**
     * 转JSON字节数组
     *
     * @return 当前对象的JSON字节数组
     */
    public byte[] toJson() {
        return JSON.toJSONBytes(this);
    }
}
