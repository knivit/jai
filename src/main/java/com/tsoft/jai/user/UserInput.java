package com.tsoft.jai.user;

import com.tsoft.jai.std.Result;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;

import static com.tsoft.jai.std.Result.Err;
import static com.tsoft.jai.std.Result.Ok;

@RequiredArgsConstructor
public class UserInput {

    public enum UserInputEnum {
        Message,
        List,
        CtrlC,
        CtrlD
    }

    @Getter
    private final UserInputEnum type;

    private String message;
    private Collection<String> list;

    public static UserInput Message(String message) {
        UserInput res = new UserInput(UserInputEnum.Message);
        res.message = (message == null) ? "" : message;
        return res;
    }

    public static UserInput List(Collection<String> list) {
        UserInput res = new UserInput(UserInputEnum.List);
        res.list = (list == null) ? Collections.emptyList() : list;
        return res;
    }

    public static UserInput CtrlC() {
        return new UserInput(UserInputEnum.CtrlC);
    }

    public static UserInput CtrlD() {
        return new UserInput(UserInputEnum.CtrlD);
    }

    public Result<String> getMessage() {
        return switch (type) {
            case Message -> Ok(message);
            default -> Err("No user input.");
        };
    }

    public Result<Collection<String>> getList() {
        return switch (type) {
            case List -> Ok(list);
            default -> Err("No user input.");
        };
    }
}
