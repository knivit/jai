package com.tsoft.jai.tokio.sync.mpsc;

import com.tsoft.jai.anyhow.Result;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.tsoft.jai.anyhow.Result.Ok;

@Data
@Accessors(chain = true)
public class UnboundedSender<T> {

    public Result<?> send(T value) {
        return Ok();
    }
}
