package top.chengdongqing.common.transformer;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 字符串转字节数组
 *
 * @author Luyao
 */
public record StrToBytes(String str) {

    /**
     * 静态工厂方法
     */
    public static StrToBytes of(String str) {
        return new StrToBytes(str);
    }

    /**
     * 从16进制字符串转2进制字节数组
     */
    public byte[] fromHex() {
        String hex = str.length() % 2 != 0 ? '0' + str : str;
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(hex.substring(index, index + 2), 16);
            bytes[i] = (byte) v;
        }
        return bytes;
    }

    /**
     * 从base64字符串转字节数组
     */
    public byte[] fromBase64() {
        return Base64.getDecoder().decode(str);
    }

    /**
     * 从URL安全的base64字符串转字节数组
     */
    public byte[] fromURLBase64() {
        return Base64.getUrlDecoder().decode(str);
    }

    /**
     * 从普通文本转字节数组
     */
    public byte[] fromText() {
        return str.getBytes(StandardCharsets.UTF_8);
    }
}
