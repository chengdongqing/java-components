package top.chengdongqing.common.sender.email;

import top.chengdongqing.common.constant.Regexp;
import top.chengdongqing.common.kit.StrKit;
import top.chengdongqing.common.sender.ISender;

import javax.mail.SendFailedException;
import java.util.regex.Pattern;

/**
 * 邮件发送器
 *
 * @author Luyao
 * @see ApacheEmailSender
 */
public abstract class EmailSender implements ISender<EmailEntity> {

    /**
     * 预编译邮箱校验正则，提高性能
     */
    private static final Pattern PATTERN = Pattern.compile(Regexp.EMAIL_ADDRESS.getValue());

    @Override
    public void send(EmailEntity entity) throws SendFailedException {
        if (StrKit.isAnyBlank(entity.getTo(), entity.getTitle(), entity.getContent())) {
            throw new IllegalArgumentException("The args can not be blank.");
        }
        if (!PATTERN.matcher(entity.getTo()).matches()) {
            throw new IllegalArgumentException("The email address is error.");
        }

        // 发送邮件
        sendEmail(entity);
    }

    /**
     * 发送邮件
     *
     * @param entity 参数实体
     */
    protected abstract void sendEmail(EmailEntity entity) throws SendFailedException;
}