package ru.checkdev.notification.listeners;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.checkdev.notification.domain.InnerMessage;
import ru.checkdev.notification.domain.SubscribeCategory;
import ru.checkdev.notification.domain.SubscribeTopic;
import ru.checkdev.notification.domain.UserTelegram;
import ru.checkdev.notification.dto.CancelInterviewNotificationDTO;
import ru.checkdev.notification.dto.CategoryWithTopicDTO;
import ru.checkdev.notification.dto.WisherDismissedDTO;
import ru.checkdev.notification.dto.WisherNotifyDTO;
import ru.checkdev.notification.events.SubscribeEvent;
import ru.checkdev.notification.service.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@AllArgsConstructor
@KafkaListener(topics = "notification-interviews", groupId = "notification-group")
public class InterviewListener {

    private final InnerMessageService messageService;
    private final SubscribeCategoryService categoryService;
    private final SubscribeTopicService topicService;
    private final NotificationMessagesService notificationMessagesService;
    private final UserTelegramService userTelegramService;
    private final InnerMessageService innerMessageService;
    private final NotificationMessage<UserTelegram, String, InnerMessage> notificationMessage;
    private final MessagesGenerator messagesGenerator;

    @KafkaHandler
    public void handleInterviewCreationEvent(CategoryWithTopicDTO categoryWithTopicDTO) {
        List<Integer> categorySubscribersIds =
            categoryService.findUserIdsByCategoryIdExcludeCurrent(
                categoryWithTopicDTO.getCategoryId(),
                categoryWithTopicDTO.getSubmitterId());

        List<Integer> topicSubscribersIds =
            topicService.findUserIdsByTopicIdExcludeCurrent(
                categoryWithTopicDTO.getTopicId(),
                categoryWithTopicDTO.getSubmitterId());

        messageService.saveMessagesForSubscribers(
            categoryWithTopicDTO,
            categorySubscribersIds, topicSubscribersIds);

        notificationMessagesService.sendMessagesToCategorySubscribers(
            categorySubscribersIds,
            categoryWithTopicDTO);
    }

    @KafkaHandler
    public void handleSubscribeTopicEvent(InnerMessage innerMessage) {
        messageService.send(innerMessage);
    }

    /**
     * Метод обрабатывает пост запрос для отправки уведомления автору собеседования,
     * о том что добавился участник собеседования.
     *
     * @param wisherNotifyDTO WisherNotifyDTO
     */
    @KafkaHandler
    public void handleParticipateAuthorEvent(WisherNotifyDTO wisherNotifyDTO) {
        var message = messagesGenerator.getMessageParticipateWisher(wisherNotifyDTO);
        InnerMessage innerMessage = InnerMessage.of()
            .userId(wisherNotifyDTO.getSubmitterId())
            .text(message)
            .created(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)))
            .read(false)
            .interviewId(wisherNotifyDTO.getInterviewId())
            .build();
        innerMessageService.saveMessage(innerMessage);
        userTelegramService
            .findByUserId(wisherNotifyDTO.getSubmitterId())
            .ifPresent(
                tg -> notificationMessage.sendMessage(tg, message)
            );
    }

    /**
     * Метод обрабатывает пост запрос для отправки уведомления участнику собеседования,
     * о том что автор собеседования отменил его.
     *
     * @param cancelInterviewDTO CancelInterviewNotificationDTO
     */
    @KafkaHandler
    public void handleParticipateCancelInterview(CancelInterviewNotificationDTO cancelInterviewDTO) {
        var message = messagesGenerator.getMessageCancelInterview(cancelInterviewDTO);
        InnerMessage innerMessage = InnerMessage.of()
            .userId(cancelInterviewDTO.getUserId())
            .text(message)
            .created(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)))
            .read(false)
            .interviewId(cancelInterviewDTO.getInterviewId())
            .build();
        CompletableFuture.supplyAsync(() -> innerMessageService.saveMessage(innerMessage));
        userTelegramService
            .findByUserId(cancelInterviewDTO.getUserId())
            .ifPresent(
                tg -> notificationMessage.sendMessage(tg, message)
            );
    }



    /**
     * Метод обрабатывает пост запрос для отправки уведомления участнику собеседования,
     * о том что автор собеседования одобрил другого участника.
     *
     * @param wisherDtoList List<WisherDto>
     */
    @KafkaHandler
    public void handleParticipantIsDismissed(List<WisherDismissedDTO> wisherDtoList) {
        List<InnerMessage> innerMessageList = new ArrayList<>();
        wisherDtoList.parallelStream().forEach(wisher -> {
                var message = messagesGenerator.getMessageDismissedWisher(wisher);
                InnerMessage innerMessage = InnerMessage.of()
                    .userId(wisher.getUserId())
                    .text(message)
                    .created(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)))
                    .read(false)
                    .interviewId(wisher.getInterviewId())
                    .build();
                CompletableFuture.supplyAsync(() -> innerMessageService.saveMessage(innerMessage));
                userTelegramService
                    .findByUserId(wisher.getUserId())
                    .ifPresent(
                        tg -> notificationMessage.sendMessage(tg, message)
                    );
            }
        );
    }

    @KafkaHandler(isDefault = true)
    public void handleUnknownEvent(Object unknownEvent) {
        log.warn("Получен неизвестный тип события: {}", unknownEvent.getClass().getName());
    }
}