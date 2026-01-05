package com.tsoft.jai.tokio.sync.mpsc;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UnboundedSender<T> {

    public void send(T value) {

    }
}
