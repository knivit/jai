package com.tsoft.jai.tokio.time;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public class Time {

    public static void sleep(Duration duration) {
        sleep((int)duration.toMillis());
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ex) {
            log.warn(ex.getMessage());
        }
    }
}
