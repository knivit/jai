package com.tsoft.jai.inquire.spinner;

import com.tsoft.jai.anyhow.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(chain = true)
@RequiredArgsConstructor
public class SpinnerEvent {

    public enum SpinnerEventEnum {
        SetMessage,
        Stop
    }

    private final SpinnerEventEnum type;
    private String message;

    public static SpinnerEvent SetMessage(String message) {
        return new SpinnerEvent(SpinnerEventEnum.SetMessage).setMessage(message);
    }

    public static boolean isSetMessage(Result<?> value) {
        return (value != null) && (value.getValue() instanceof SpinnerEvent se) && (SpinnerEventEnum.SetMessage.equals(se.getType()));
    }

    public static SpinnerEvent Stop() {
        return new SpinnerEvent(SpinnerEventEnum.Stop);
    }

    public static boolean isStop(Result<?> value) {
        return (value != null) && (value.getValue() instanceof SpinnerEvent se) && (SpinnerEventEnum.Stop.equals(se.getType()));
    }
}
