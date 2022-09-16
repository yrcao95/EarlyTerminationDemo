package com.yirui.processor;

import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Getter
@AllArgsConstructor
public class Verdict {
    private boolean large;
    private Integer score;
    private List<String> authors;

    public Verdict() {
        this.large = false;
        this.score = 0;
        this.authors = Collections.synchronizedList(new ArrayList<String>());
    }

    public synchronized void setLarge(boolean lar) {
        this.large = lar;
    }

    public synchronized void setScore(Integer score) throws InterruptedException {
        Thread.sleep(3000);
        this.score = score;
        if (this.score > 10) this.large = true;
    }
}
