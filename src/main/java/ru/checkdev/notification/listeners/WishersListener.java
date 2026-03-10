package ru.checkdev.notification.listeners;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.checkdev.notification.domain.InnerMessage;
import ru.checkdev.notification.dto.FeedbackNotificationDTO;
import ru.checkdev.notification.dto.WisherApprovedDTO;
import ru.checkdev.notification.service.InnerMessageService;
import ru.checkdev.notification.service.NotificationMessagesService;

@Slf4j
@Component
@AllArgsConstructor
@KafkaListener(topics = "notification-wishers", groupId = "notification-group")
public class WishersListener {

    private final NotificationMessagesService notificationMessagesService;

    @KafkaHandler
    public void handleApprovedWisher(WisherApprovedDTO wisherApprovedNotifyDTO) {
        notificationMessagesService.sendApprovedNotification(wisherApprovedNotifyDTO);
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknownEvent(Object unknownEvent) {
        log.warn("Получен неизвестный тип события: {}", unknownEvent.getClass().getName());
    }
}