package xyz.mobi.notificationservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MailSenderConfig {

    private final OutlookMailProperties properties;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(properties.getHost());
        mailSender.setPort(properties.getPort());
        mailSender.setDefaultEncoding(properties.getDefaultEncoding());

        if (properties.getUsername() != null && !properties.getUsername().isBlank()) {
            mailSender.setUsername(properties.getUsername());
        }

        if (properties.getPassword() != null && !properties.getPassword().isBlank()) {
            mailSender.setPassword(properties.getPassword());
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(properties.isAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(properties.getStarttls().isEnable()));
        props.put("mail.smtp.starttls.required", String.valueOf(properties.getStarttls().isRequired()));
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");

        // Network Timeouts
        props.put("mail.smtp.connectiontimeout", String.valueOf(properties.getConnectionTimeout()));
        props.put("mail.smtp.timeout", String.valueOf(properties.getReadTimeout()));
        props.put("mail.smtp.writetimeout", String.valueOf(properties.getWriteTimeout()));

        log.info("Configured Outlook JavaMailSender: host={}:{}, auth={}, starttls={}",
                properties.getHost(), properties.getPort(), properties.isAuth(), properties.getStarttls().isEnable());

        return mailSender;
    }
}
