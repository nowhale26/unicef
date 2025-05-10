package com.unicef.common;

import com.unicef.common.exception.BotException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class ErrorApplier {
    private ErrorApplier() {}

    public static Mono<? extends Throwable> applyError(ClientResponse response) {
        logResponse(response);
        return response.bodyToMono(String.class)
                .flatMap(error -> Mono.error(
                        new BotException(error, response.statusCode().toString(), response.logPrefix())));
    }

    public static void logResponse(ClientResponse response) {
        if (log.isErrorEnabled()) {
            log.info("Response status: {}", response.statusCode());
            log.info("Response headers: {}", response.headers().asHttpHeaders());
            response.bodyToMono(String.class)
                    .publishOn(Schedulers.boundedElastic())
                    .subscribe(body -> log.error("Response body: {}", body));
        }
    }
}
