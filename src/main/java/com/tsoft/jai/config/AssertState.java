package com.tsoft.jai.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(chain = true)
@RequiredArgsConstructor
public class AssertState {

    public enum AssertStateEnum {
        True,
        False,
        TrueFalse,
        Equal
    }

    private final AssertStateEnum type;

    private int stateFlags;
    private int trueFlags;
    private int falseFlags;

    public static AssertState True(int stateFlags) {
        return new AssertState(AssertStateEnum.True).setStateFlags(stateFlags);
    }

    public static AssertState False(int stateFlags) {
        return new AssertState(AssertStateEnum.False).setStateFlags(stateFlags);
    }

    public static AssertState TrueFalse(int trueFlags, int falseFlags){
        return new AssertState(AssertStateEnum.TrueFalse).setTrueFlags(trueFlags).setFalseFlags(falseFlags);
    }

    public static AssertState Equal(int stateFlags) {
        return new AssertState(AssertStateEnum.Equal).setStateFlags(stateFlags);
    }

    // pub fn pass() -> Self {
    //    AssertState::False(StateFlags::empty())
    // }
    public static AssertState pass() {
        return False(0);
    }

    // pub fn bare() -> Self {
    //    AssertState::Equal(StateFlags::empty())
    // }
    public static AssertState bare() {
        return Equal(0);
    }

    // pub fn assert(self, flags: StateFlags) -> bool {
    //     match self {
    //         AssertState::True(true_flags) => true_flags & flags != StateFlags::empty(),
    //         AssertState::False(false_flags) => false_flags & flags == StateFlags::empty(),
    //         AssertState::TrueFalse(true_flags, false_flags) => {
    //             (true_flags & flags != StateFlags::empty())
    //                 && (false_flags & flags == StateFlags::empty())
    //         }
    //         AssertState::Equal(check_flags) => check_flags == flags,
    //     }
    // }
    public boolean asserts(int flags) {
        return switch (type) {
            case True -> flags != 0;
            case False -> flags == 0;
            case TrueFalse -> ((trueFlags & flags) != 0) && ((falseFlags & flags) == 0);
            case Equal -> stateFlags == flags;
        };
    }
}
