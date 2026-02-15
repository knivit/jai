package com.tsoft.jai.inquire.spinner;

import com.tsoft.jai.anyhow.Result;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.tsoft.jai.anyhow.Result.isOk;

@Getter
@RequiredArgsConstructor
public class SpinnerEvent {

    public enum SpinnerEventEnum {
        SetMessage,
        Stop
    }

    private final SpinnerEventEnum type;
    private String message;

    public static SpinnerEvent SetMessage(String message) {
        SpinnerEvent se = new SpinnerEvent(SpinnerEventEnum.SetMessage);
        se.message = message;
        return se;
    }

    public static boolean isSetMessage(Result<?> value) {
        return isOk(value) && (value.getValue() instanceof SpinnerEvent se) && (SpinnerEventEnum.SetMessage.equals(se.getType()));
    }

    public static SpinnerEvent Stop() {
        return new SpinnerEvent(SpinnerEventEnum.Stop);
    }

    public static boolean isStop(Result<?> value) {
        return isOk(value) && (value.getValue() instanceof SpinnerEvent se) && (SpinnerEventEnum.Stop.equals(se.getType()));
    }
}
