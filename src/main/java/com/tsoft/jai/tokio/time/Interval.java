package com.tsoft.jai.tokio.time;

import com.tsoft.jai.anyhow.Result;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;

import static com.tsoft.jai.anyhow.Result.Err;
import static com.tsoft.jai.anyhow.Result.Ok;

@RequiredArgsConstructor
public class Interval {

    private final Duration duration;

    public Result<Instant> tick() {
        try {
            Thread.sleep(duration.toMillis());
            return Ok(Instant.now());
        } catch (Exception ex) {
            return Err();
        }
    }
}
