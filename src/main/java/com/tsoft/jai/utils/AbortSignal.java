package com.tsoft.jai.utils;

import com.tsoft.jai.anyhow.Result;
import com.tsoft.jai.inquire.event.Event;
import com.tsoft.jai.inquire.event.EventKey;
import com.tsoft.jai.inquire.event.KeyModifiers;
import com.tsoft.jai.tokio.time.Time;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.tsoft.jai.anyhow.Result.Ok;
import static com.tsoft.jai.anyhow.Result.isOk;

public class AbortSignal {

    private final AtomicBoolean ctrlc = new AtomicBoolean(false);
    private final AtomicBoolean ctrld = new AtomicBoolean(false);

    public static AbortSignal createAbortSignal() {
        return new AbortSignal();
    }

    // pub async fn wait_abort_signal(abort_signal: &AbortSignal) {
    //    loop {
    //        if abort_signal.aborted() {
    //            break;
    //        }
    //        tokio::time::sleep(std::time::Duration::from_millis(25)).await;
    //    }
    // }
    public static void waitAbortSignal(AbortSignal abortSignal) {
        while (true) {
            if (abortSignal.aborted()) {
                break;
            }
            Time.sleep(Duration.ofMillis(25));
        }
    }

    // pub fn aborted(&self) -> bool {
    //     if self.aborted_ctrlc() {
    //         return true;
    //     }
    //     if self.aborted_ctrld() {
    //         return true;
    //     }
    //     false
    // }
    public boolean aborted() {
        if (abortedCtrlC()) {
            return true;
        }
        if (abortedCtrlD()) {
            return true;
        }
        return false;
    }

    // pub fn aborted_ctrlc(&self) -> bool {
    //     self.ctrlc.load(Ordering::SeqCst)
    // }
    public boolean abortedCtrlC() {
        return ctrlc.get();
    }

    // pub fn aborted_ctrld(&self) -> bool {
    //     self.ctrld.load(Ordering::SeqCst)
    // }
    public boolean abortedCtrlD() {
        return ctrld.get();
    }

    // pub fn reset(&self) {
    //    self.ctrlc.store(false, Ordering::SeqCst);
    //    self.ctrld.store(false, Ordering::SeqCst);
    // }
    public void reset() {
        ctrlc.set(false);
        ctrld.set(false);
    }

    // pub fn set_ctrlc(&self) {
    //     self.ctrlc.store(true, Ordering::SeqCst);
    // }
    public void setCtrlC() {
        ctrlc.set(true);
    }

    // pub fn set_ctrld(&self) {
    //     self.ctrld.store(true, Ordering::SeqCst);
    // }
    public void setCtrlD() {
        ctrld.set(true);
    }

    // pub fn poll_abort_signal(abort_signal: &AbortSignal) -> Result<bool> {
    //    if crossterm::event::poll(Duration::from_millis(25))? {
    //        if let Event::Key(key) = event::read()? {
    //            match key.code {
    //                KeyCode::Char('c') if key.modifiers == KeyModifiers::CONTROL => {
    //                    abort_signal.set_ctrlc();
    //                    return Ok(true);
    //                }
    //                KeyCode::Char('d') if key.modifiers == KeyModifiers::CONTROL => {
    //                    abort_signal.set_ctrld();
    //                    return Ok(true);
    //                }
    //                _ => {}
    //            }
    //        }
    //    }
    //    Ok(false)
    // }
    public static Result<Boolean> pollAbortSignal(AbortSignal abortSignal) {
        Result<Boolean> res = Event.poll(Duration.ofMillis(25));
        if (isOk(res)) {
            Result<EventKey> evt = Event.read();
            if (isOk(evt)) {
                EventKey key = evt.getValue();
                if ('c' == key.getCode()) {
                    if (KeyModifiers.CONTROL.equals(key.getKeyModifiers())) {
                        abortSignal.setCtrlC();
                        return Ok(true);
                    }
                } else if ('d' == key.getCode()) {
                    if (KeyModifiers.CONTROL.equals(key.getKeyModifiers())) {
                        abortSignal.setCtrlD();
                        return Ok(true);
                    }
                }
            }
        }
        return Ok(false);
    }
}
