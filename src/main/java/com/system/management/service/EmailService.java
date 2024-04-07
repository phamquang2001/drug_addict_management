package com.system.management.service;

import com.system.management.model.dto.PoliceDto;
import com.system.management.model.entity.EmailContent;
import com.system.management.model.entity.Police;
import com.system.management.repository.EmailContentRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    public static final SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    public static final SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");

    private final EmailContentRepository emailContentRepository;

    private final JavaMailSender javaMailSender;

    @SneakyThrows
    public void sendMailAccountCreated(PoliceDto police, String password) {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
        helper.setSubject("THÔNG BÁO TẠO TÀI KHOẢN NGÀY " + sdf1.format(new Date()));
        helper.setTo(police.getEmail());

        EmailContent emailContent = emailContentRepository.findByType("Account created");

        String content = emailContent.getContent();
        content = content.replace("%name%", police.getFullName());
        content = content.replace("%time%", sdf2.format(police.getCreatedAt()));
        content = content.replace("%creator%", police.getCreatedBy().getFullName());
        content = content.replace("%username%", police.getIdentifyNumber());
        content = content.replace("%password%", password);
        message.setContent(content, "text/html; charset=UTF-8");

        javaMailSender.send(message);
    }

    @SneakyThrows
    public void sendMailPoliceForgetPassword(Police sheriff, Police police, String password) {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
        helper.setSubject("THÔNG BÁO XỬ LÝ YÊU CẦU QUÊN MẬT KHẨU TÀI KHOẢN " + police.getIdentifyNumber() + " NGÀY " + sdf1.format(new Date()));
        helper.setTo(sheriff.getEmail());

        EmailContent emailContent = emailContentRepository.findByType("Police forget password");

        String content = emailContent.getContent();
        content = content.replace("%name%", sheriff.getFullName());
        content = content.replace("%time%", sdf2.format(new Date()));
        content = content.replace("%police%", police.getFullName());
        content = content.replace("%username%", police.getIdentifyNumber());
        content = content.replace("%password%", password);
        message.setContent(content, "text/html; charset=UTF-8");

        javaMailSender.send(message);
    }

    @SneakyThrows
    public void sendMailSheriffForgetPassword(Police sheriff, String password) {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
        helper.setSubject("THÔNG BÁO XỬ LÝ YÊU CẦU QUÊN MẬT KHẨU TÀI KHOẢN " + sheriff.getIdentifyNumber() + " NGÀY " + sdf1.format(new Date()));
        helper.setTo(sheriff.getEmail());

        EmailContent emailContent = emailContentRepository.findByType("Sheriff forget password");

        String content = emailContent.getContent();
        content = content.replace("%name%", sheriff.getFullName());
        content = content.replace("%time%", sdf2.format(new Date()));
        content = content.replace("%username%", sheriff.getIdentifyNumber());
        content = content.replace("%password%", password);
        message.setContent(content, "text/html; charset=UTF-8");

        javaMailSender.send(message);
    }
}
