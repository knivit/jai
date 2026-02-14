package com.tsoft.jai.tokio.sync;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(chain = true)
@RequiredArgsConstructor
public class TryRecvError {

    public enum TryRecvErrorEnum {
        Empty,
        Closed,
        Disconnected
    }

    private final TryRecvErrorEnum type;

    public static TryRecvError Empty() {
        return new TryRecvError(TryRecvErrorEnum.Empty);
    }

    public static TryRecvError Closed() {
        return new TryRecvError(TryRecvErrorEnum.Closed);
    }

    public static boolean isClosed(com.tsoft.jai.anyhow.Error<?> err) {
        return (err != null) && (err.getErrValue() instanceof TryRecvError tre) && (TryRecvErrorEnum.Closed.equals(tre.getType()));
    }
}
