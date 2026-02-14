package com.tsoft.jai.tokio.sync.mpsc;

import com.tsoft.jai.inquire.spinner.SpinnerEvent;
import com.tsoft.jai.utils.base.Tuple;

public class Unbounded {

    // /// Creates an unbounded mpsc channel for communicating between asynchronous
    // /// tasks without backpressure.
    // ///
    // /// A `send` on this channel will always succeed as long as the receive half has
    // /// not been closed. If the receiver falls behind, messages will be arbitrarily
    // /// buffered.
    // ///
    // /// **Note** that the amount of available system memory is an implicit bound to
    // /// the channel. Using an `unbounded` channel has the ability of causing the
    // /// process to run out of memory. In this case, the process will be aborted.
    // pub fn unbounded_channel<T>() -> (UnboundedSender<T>, UnboundedReceiver<T>) {
    //    let (tx, rx) = chan::channel(Semaphore(AtomicUsize::new(0)));
    //
    //    let tx = UnboundedSender::new(tx);
    //    let rx = UnboundedReceiver::new(rx);
    //
    //    (tx, rx)
    // }
    public static <T> Tuple<UnboundedSender<T>, UnboundedReceiver<T>> unboundedChannel() {
        UnboundedChannel<T> channel = new UnboundedChannel<>();

        UnboundedSender<T> tx = new UnboundedSender<>(channel);
        UnboundedReceiver<T> rx = new UnboundedReceiver<>(channel);

        return new Tuple<>(tx, rx);
    }
}
