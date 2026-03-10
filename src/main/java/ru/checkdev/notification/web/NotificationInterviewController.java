package ru.checkdev.notification.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.checkdev.notification.domain.InnerMessage;
import ru.checkdev.notification.domain.UserTelegram;
import ru.checkdev.notification.dto.CancelInterviewNotificationDTO;
import ru.checkdev.notification.dto.InterviewNotifyDTO;
import ru.checkdev.notification.dto.WisherDismissedDTO;
import ru.checkdev.notification.dto.WisherNotifyDTO;
import ru.checkdev.notification.service.InnerMessageService;
import ru.checkdev.notification.service.MessagesGenerator;
import ru.checkdev.notification.service.NotificationMessage;
import ru.checkdev.notification.service.UserTelegramService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Dmitry Stepanov, user Dmitry
 * @since 17.11.2023
 */
@Tag(name = "NotificationInterviewController", description = "NotificationTopic REST API")
@RestController
@RequestMapping("/notification")
@AllArgsConstructor
public class NotificationInterviewController {
    private final UserTelegramService userTelegramService;
    private final NotificationMessage<UserTelegram, String, InnerMessage> notificationMessage;
    private final MessagesGenerator messagesGenerator;

    /**
     * Метод обрабатывает пост запрос для рассылки уведомлений
     * подписчикам на тему.
     *
     * @param interviewNotifyDTO InterviewNotifyDTO
     * @return ResponseEntity<List < InnerMessage>>
     */
    @PostMapping("/topic/")
    public ResponseEntity<List<InnerMessage>> sendMessageSubscribeTopic(@RequestBody InterviewNotifyDTO interviewNotifyDTO) {
        List<UserTelegram> usersTopic = userTelegramService
                .findAllByTopicIdAndUserIdNot(interviewNotifyDTO.getTopicId(),
                        interviewNotifyDTO.getSubmitterId());
        var message = messagesGenerator.getMessageSubscribeTopic(interviewNotifyDTO);
        List<InnerMessage> result = notificationMessage.sendMessage(usersTopic, message);
        return ResponseEntity.ok(result);
    }
}
