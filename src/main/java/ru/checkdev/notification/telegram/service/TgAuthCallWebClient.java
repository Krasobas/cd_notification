package ru.checkdev.notification.telegram.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.Profile;
import ru.checkdev.notification.retry.Retry;
import ru.checkdev.notification.service.EurekaUriProvider;

/**
 * Класс реализует методы get и post для отправки сообщений через WebClient
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 12.09.2023
 */
@org.springframework.context.annotation.Profile("default")
@Service
@AllArgsConstructor
@Slf4j
public class TgAuthCallWebClient implements TgCall {
    private final EurekaUriProvider uriProvider;
    private static final String SERVICE_ID = "auth";

    /**
     * Метод get
     *
     * @param url URL http
     * @return Mono<Person>
     */
    @Override
    public Mono<Profile> doGet(String url) {
        var retry = new Retry(3, 1000);
        return retry.exec(() ->
            WebClient.create(uriProvider.getUri(SERVICE_ID))
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(Profile.class)
                .doOnError(err -> log.error("API not found: {}", err.getMessage())),
            Mono.error(new RuntimeException("All retry attempts failed"))
        );
    }

    /**
     * Метод POST
     *
     * @param url     URL http
     * @param profile Body PersonDTO.class
     * @return Mono<Person>
     */
    @Override
    public Mono<Object> doPost(String url, Profile profile) {
        var retry = new Retry(3, 1000);
        return retry.exec(() ->
            WebClient.create(uriProvider.getUri(SERVICE_ID))
                .post()
                .uri(url)
                .bodyValue(profile)
                .retrieve()
                .bodyToMono(Object.class)
                .doOnError(err -> log.error("API not found: {}", err.getMessage())),
            Mono.error(new RuntimeException("All retry attempts failed"))
        );
    }

    @Override
    public Mono<Object> doPost(String url) {
        var retry = new Retry(3, 1000);
        return retry.exec(() ->
                WebClient.create(uriProvider.getUri(SERVICE_ID))
                .post()
                .uri(url)
                .retrieve()
                .bodyToMono(Object.class)
                .doOnError(err -> log.error("API not found: {}", err.getMessage())),
        Mono.error(new RuntimeException("All retry attempts failed"))
        );
    }
}
