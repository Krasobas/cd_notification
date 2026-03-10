package ru.checkdev.notification.listeners;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.checkdev.notification.domain.InnerMessage;
import ru.checkdev.notification.dto.CategoryWithTopicDTO;
import ru.checkdev.notification.dto.FeedbackNotificationDTO;
import ru.checkdev.notification.service.InnerMessageService;
import ru.checkdev.notification.service.NotificationMessagesService;
import ru.checkdev.notification.service.SubscribeCategoryService;
import ru.checkdev.notification.service.SubscribeTopicService;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
@KafkaListener(topics = "notification-messages", groupId = "notification-group")
public class MessageListener {

    private final InnerMessageService messageService;
    private final NotificationMessagesService notificationMessagesService;



    @KafkaHandler
    public void handleMessageEvent(InnerMessage innerMessage) {
        messageService.send(innerMessage);
    }

    @KafkaHandler
    public void handleFeedbackEvent(FeedbackNotificationDTO feedbackNotification) {
        notificationMessagesService.sendFeedbackNotification(feedbackNotification);
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknownEvent(Object unknownEvent) {
        log.warn("Получен неизвестный тип события: {}", unknownEvent.getClass().getName());
    }
}