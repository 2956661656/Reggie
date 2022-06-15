package com.itheima.reggie.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailUtil {
    private static final String emailName = "jeentVIP@yeah.net";
    private static final String PWD = "CBGKFYNRLDWNNQON";

    public static void sendEmail(String receive, String subject, String content, String code) throws MessagingException {

        final Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.host", "smtp.yeah.net");

        properties.put("mail.user", emailName);
        properties.put("mail.password", PWD);

        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailName, PWD);
            }
        };

        //创建邮箱会话
        Session session = Session.getInstance(properties, authenticator);
        MimeMessage message = new MimeMessage(session);

        //设置邮箱发送者
        String username = properties.getProperty("mail.user");
        InternetAddress from = new InternetAddress(username);
        InternetAddress[] fromAddress = new InternetAddress[]{from};
        message.addFrom(fromAddress);

        //设置邮件接收方
        InternetAddress to = new InternetAddress(receive);
        InternetAddress[] toAddresses = new InternetAddress[]{to};
        message.setRecipients(Message.RecipientType.TO, toAddresses);

        //设置主题和内容
        message.setSubject(subject);
        message.setContent(content + code,"text/html;charset=UTF-8");

        //发送
        Transport.send(message);

    }

}
