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
        // Khởi tạo MimeMessage là nội dung email gửi đi
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");

        // Set tiêu đề email
        helper.setSubject("THÔNG BÁO TẠO TÀI KHOẢN NGÀY " + sdf1.format(new Date()));

        // Set email người nhận
        helper.setTo(police.getEmail());

        // Tìm kiếm thông tin nội dung email gửi đi đã được thiết kế sẵn và lưu trong bảng email_contents theo mã Account created
        EmailContent emailContent = emailContentRepository.findByType("Account created");

        // Lấy ra nội dung gửi đi được lưu ở cột content
        String content = emailContent.getContent();
        content = content.replace("%name%", police.getFullName());                      // Họ tên cảnh sát được tạo
        content = content.replace("%time%", sdf2.format(police.getCreatedAt()));        // Thời gian tạo tài khoản
        content = content.replace("%creator%", police.getCreatedBy().getFullName());    // Họ tên người tạo
        content = content.replace("%username%", police.getIdentifyNumber());            // Số CCCD cảnh sát được tạo
        content = content.replace("%password%", password);                              // Mật khẩu tài khoản cảnh sát được tạo

        // Set thông tin nội dung gửi đi ở dạng HTML
        message.setContent(content, "text/html; charset=UTF-8");

        // Sử dụng Java Mail Sender để gửi email đi
        javaMailSender.send(message);
    }

    @SneakyThrows
    public void sendMailPoliceForgetPassword(Police sheriff, Police police, String password) {
        // Khởi tạo MimeMessage là nội dung email gửi đi
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");

        // Set tiêu đề email
        helper.setSubject("THÔNG BÁO XỬ LÝ YÊU CẦU QUÊN MẬT KHẨU TÀI KHOẢN " + police.getIdentifyNumber() + " NGÀY " + sdf1.format(new Date()));

        // Set email người nhận
        helper.setTo(sheriff.getEmail());

        // Tìm kiếm thông tin nội dung email gửi đi đã được thiết kế sẵn và lưu trong bảng email_contents theo mã Police forget password
        EmailContent emailContent = emailContentRepository.findByType("Police forget password");

        // Lấy ra nội dung gửi đi được lưu ở cột content
        String content = emailContent.getContent();
        content = content.replace("%name%", sheriff.getFullName());             // Họ tên cảnh sát trưởng quản lý trực tiếp
        content = content.replace("%time%", sdf2.format(new Date()));           // Thời gian yêu cầu
        content = content.replace("%police%", police.getFullName());            // Họ tên cảnh sát quên mật khẩu
        content = content.replace("%username%", police.getIdentifyNumber());    // Số CCCD cảnh sát quên mật khẩu
        content = content.replace("%password%", password);                      // Mật khẩu mới của tài khoản

        // Set thông tin nội dung gửi đi ở dạng HTML
        message.setContent(content, "text/html; charset=UTF-8");

        // Sử dụng Java Mail Sender để gửi email đi
        javaMailSender.send(message);
    }

    @SneakyThrows
    public void sendMailSheriffForgetPassword(Police sheriff, String password) {
        // Khởi tạo MimeMessage là nội dung email gửi đi
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");

        // Set tiêu đề email
        helper.setSubject("THÔNG BÁO XỬ LÝ YÊU CẦU QUÊN MẬT KHẨU TÀI KHOẢN " + sheriff.getIdentifyNumber() + " NGÀY " + sdf1.format(new Date()));

        // Set email người nhận
        helper.setTo(sheriff.getEmail());

        // Tìm kiếm thông tin nội dung email gửi đi đã được thiết kế sẵn và lưu trong bảng email_contents theo mã Sheriff forget password
        EmailContent emailContent = emailContentRepository.findByType("Sheriff forget password");

        // Lấy ra nội dung gửi đi được lưu ở cột content
        String content = emailContent.getContent();
        content = content.replace("%name%", sheriff.getFullName());             // Họ tên cảnh sát trưởng quên mật khẩu
        content = content.replace("%time%", sdf2.format(new Date()));           // Thời gian yêu cầu
        content = content.replace("%username%", sheriff.getIdentifyNumber());   // Số CCCD cảnh sát trưởng quên mật khẩu
        content = content.replace("%password%", password);                      // Mật khẩu mới của tài khoản

        // Set thông tin nội dung gửi đi ở dạng HTML
        message.setContent(content, "text/html; charset=UTF-8");

        // Sử dụng Java Mail Sender để gửi email đi
        javaMailSender.send(message);
    }
}
