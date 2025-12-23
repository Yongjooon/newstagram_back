package com.ssafy.newstagram.api.auth.model.service;

import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.exception.SolapiUnknownException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {
    private final DefaultMessageService messageService;

    @Value("${solapi.from-number}")
    private String fromNumber;

    public void send(String to, String text){
        Message message = new Message();
        message.setFrom(fromNumber);
        message.setTo(to);
        message.setText(text); // SMS는 한글 45자, 영자 90자까지 입력할 수 있습니다.

        try {
            messageService.send(message);
            log.info("[SMS] Send success(to={})", to);
        } catch (SolapiMessageNotReceivedException | SolapiUnknownException exception) {
            log.error("[SMS] Send failed by Solapi exception(to={}): error={}", to, exception.toString());
        } catch (Exception exception) {
            log.error("[SMS] Send failed by general exception(to={}): error={}", to, exception.toString());
        }
    }
}
