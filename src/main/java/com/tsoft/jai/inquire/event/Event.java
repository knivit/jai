package com.tsoft.jai.inquire.event;

import com.tsoft.jai.anyhow.Result;

import java.time.Duration;

import static com.tsoft.jai.anyhow.Result.Err;
import static com.tsoft.jai.anyhow.Result.Ok;
import static com.tsoft.jai.inquire.Inquire.terminal;

public class Event {

    public static final int EOF_EVENT = -1;
    public static final int TIMEOUT_EVENT = -2;

    // Checks if there is an Event available.
    // Returns Ok(true) if an Event is available otherwise it returns Ok(false).
    // Ok(true) guarantees that subsequent call to the read function won't block.
    public static Result<Boolean> poll(Duration duration) {
        try {
            int res = terminal.reader().peek(duration.toMillis());
            return Ok(res != TIMEOUT_EVENT);
        } catch (Exception ex) {
            return Err();
        }
    }

    public static Result<EventKey> read() {
        try {
            int res = terminal.reader().read();
            if (res == EOF_EVENT || res == TIMEOUT_EVENT) {
                return Err();
            } else {
                KeyModifiers keyModifiers = (res & 0x1f) == 0 ? KeyModifiers.NONE : KeyModifiers.CONTROL;
                return Ok(new EventKey(res, keyModifiers));
            }
        } catch (Exception ex) {
            return Err();
        }
    }
}
