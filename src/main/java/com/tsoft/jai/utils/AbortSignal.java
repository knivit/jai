package com.tsoft.jai.utils;

import com.tsoft.jai.tokio.Time;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

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

    //  pub fn set_ctrlc(&self) {
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
}
