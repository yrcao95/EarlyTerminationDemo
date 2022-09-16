package com.yirui.processor;

import lombok.Getter;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

@Component
public class Generator {

    Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    ApplicationContext applicationContext;

    public Generator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Callable<Report> generateLocalCallable() {
        return () -> {
            LOGGER.info("Current thread: {}", Thread.currentThread().getName());
            Random rd = new Random();
            Integer delay = rd.nextInt(20);
            Integer score = rd.nextInt(10);
//            LOGGER.info("Delay is {}, score is {}", delay, score);
            Thread.sleep(delay * 1000);
            Report report = new Report(delay, score, applicationContext.getId());
            return report;
        };
    }
}
