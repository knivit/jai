package com.tsoft.jai.utils;

import com.tsoft.jai.tokio.Time;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@Accessors(chain = true)
public class AbortSignal {

    private AtomicBoolean ctrlc = new AtomicBoolean(false);
    private AtomicBoolean ctrld = new AtomicBoolean(false);

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
}
