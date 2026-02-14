package com.tsoft.jai.tokio.sync.oneshot;

import com.tsoft.jai.utils.base.Tuple;

public class Oneshot {

    // A single-use communication channel with a sender and receiver:
    //   done_tx - sender, is used by the producer to send the value
    //   done_rx - receiver, is used by the consumer to receive the value
    // let (done_tx, done_rx) = oneshot::channel();
    public static <T> Tuple<Sender<T>, Receiver<T>> channel() {
        OneshotChannel<T> channel = new OneshotChannel<>();

        Sender<T> sender = new Sender<>(channel);
        Receiver<T> receiver = new Receiver<>(channel);
        return new Tuple<>(sender, receiver);
    }
}
