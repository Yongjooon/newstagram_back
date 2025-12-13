package com.ssafy.newstagram.api.auth.model.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private  final JavaMailSender mailSender;

    @Value("${base-url}")
    private String BASE_URI;

    public void sendPasswordResetEmail(String to, String token){
        String resetLink = BASE_URI + "/password-reset?token=" + token;

        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("[Newstagram] 비밀번호 재설정 안내");
            String html = """
                <p>비밀번호 재설정을 요청하셨습니다.</p>
                <p>아래 버튼을 클릭해 새로운 비밀번호로 변경하세요:</p>
                <p>
                    <a href="%s" target="_blank"
                       style="display:inline-block;
                              padding:10px 16px;
                              background-color:#4f46e5;
                              color:white;
                              text-decoration:none;
                              border-radius:6px;">
                        비밀번호 재설정
                    </a>
                </p>
                <p>링크는 <b>1시간</b> 동안 유효합니다.</p>
            """.formatted(resetLink);

            helper.setText(html, true);
            mailSender.send(message);

        } catch(Exception e){
            throw new RuntimeException("비밀번호 재설정 이메일 전송 실패", e);
        }
    }
}
