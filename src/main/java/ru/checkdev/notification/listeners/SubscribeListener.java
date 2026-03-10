package ru.checkdev.notification.listeners;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.checkdev.notification.domain.SubscribeCategory;
import ru.checkdev.notification.domain.SubscribeTopic;
import ru.checkdev.notification.events.SubscribeEvent;
import ru.checkdev.notification.service.SubscribeCategoryService;
import ru.checkdev.notification.service.SubscribeTopicService;

@Slf4j
@Component
@AllArgsConstructor
@KafkaListener(topics = "notification-subscribe", groupId = "notification-group")
public class SubscribeListener {

    private final SubscribeCategoryService subscribeCategoryService;
    private final SubscribeTopicService subscribeTopicService;

    @KafkaHandler
    public void handleCategoryEvent(SubscribeEvent<SubscribeCategory> event) {
        var category = event.getObject();
        log.info("Обработка категории: action={}, userId={}, categoryId={}",
            event.getAction(),
            category.getUserId(),
            category.getCategoryId());

        switch (event.getAction()) {
            case ADD -> {
                subscribeCategoryService.save(category);
                log.debug("Подписка на категорию добавлена");
            }
            case DELETE -> {
                subscribeCategoryService.delete(category);
                log.debug("Подписка на категорию удалена");
            }
            default -> log.error("Неизвестная операция");
        }
    }

    @KafkaHandler
    public void handleTopicEvent(SubscribeEvent<SubscribeTopic> event) {
        var topic = event.getObject();
        log.info("Обработка топика: action={}, userId={}, topicId={}",
            event.getAction(),
            topic.getUserId(),
            topic.getTopicId());

        switch (event.getAction()) {
            case ADD -> {
                subscribeTopicService.save(topic);
                log.debug("Подписка на топик добавлена");
            }
            case DELETE -> {
                subscribeTopicService.delete(topic);
                log.debug("Подписка на топик удалена");
            }
            default -> log.error("Неизвестная операция");
        }
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknownEvent(Object unknownEvent) {
        log.warn("Получен неизвестный тип события: {}", unknownEvent.getClass().getName());
    }
}