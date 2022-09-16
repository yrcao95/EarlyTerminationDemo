package com.yirui.engineserver2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class Controller {
    private final ApplicationContext applicationContext;

    Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public Controller(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @GetMapping("/report/fetch")
    public ResponseEntity<Report> getDelayedReport() throws InterruptedException {
        Random rd = new Random();
        Integer delay = rd.nextInt(20);
        Integer score = rd.nextInt(10);
        LOGGER.info("Delay is {}, score is {}", delay, score);
        Thread.sleep(delay * 1000);
        Report report = new Report(delay, score, applicationContext.getId());
        return ResponseEntity.ok(report);
    }
}
