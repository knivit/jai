package com.tsoft.jai.tokio;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Time {

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ex) {
            log.warn(ex.getMessage());
        }
    }
}
