package com.yirui.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
public class Controller {
    private final WebClient webClient;

    private Generator generator;

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public Controller(WebClient webClient, Generator generator) {
        this.webClient = webClient;
        this.generator = generator;
    }

    @GetMapping("/judge")
    public ResponseEntity<Verdict> waitJudgement() {
        ObjectMapper objectMapper = new ObjectMapper();
        LOGGER.info("Current thread: {}", Thread.currentThread().getName());
        Scheduler scheduler = Schedulers.fromExecutor(new ForkJoinPool());
        AtomicBoolean engine1Complete = new AtomicBoolean(false);
        AtomicBoolean engine2Complete = new AtomicBoolean(false);
        AtomicBoolean engine3Complete = new AtomicBoolean(false);
        AtomicBoolean engine4Complete = new AtomicBoolean(false);

        Mono<Report> engine1Mono = constructMono(1, objectMapper);
        Mono<Report> engine2Mono = constructMono(2, objectMapper);
        Mono<Report> engine3Mono = constructMono(3, objectMapper);
        Mono<Report> engine4Mono = Mono.fromCallable(generator.generateLocalCallable()).subscribeOn(scheduler);
        Verdict verdict = new Verdict();
        engine1Mono.subscribe(r -> {
            try {
                sideEffect(r, engine1Complete, verdict);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        engine2Mono.subscribe(r -> {
            try {
                sideEffect(r, engine2Complete, verdict);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        engine3Mono.subscribe(r -> {
            try {
                sideEffect(r, engine3Complete, verdict);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        engine4Mono.subscribe(r -> {
            try {
                sideEffect(r, engine4Complete, verdict);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
//        Mono.zip(engine1Mono, engine2Mono, engine3Mono, )
        while (true) {
            if (engine1Complete.get() && engine2Complete.get() && engine3Complete.get()) break;
            if (verdict.isLarge()) return ResponseEntity.ok(verdict);
        }
        return ResponseEntity.ok(verdict);
    }

    private Mono<Report> constructMono(Integer num, ObjectMapper objectMapper) {
        String url = String.format("localhost:%s/report/fetch", 8000 + num);
        LOGGER.info("Sent a request to {}", 8000 + num);
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(s -> {
                    try {
                        return objectMapper.readValue(s, Report.class);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return null;
                    }
                });
    }

    private void sideEffect(Report r, AtomicBoolean completion, Verdict verdict) throws InterruptedException {
        if (r != null) {
            LOGGER.info(r.toString());
            completion.set(true);
            verdict.setScore(verdict.getScore() + r.getScore());
            verdict.getAuthors().add(r.getAuthor());
        }
    }

}
