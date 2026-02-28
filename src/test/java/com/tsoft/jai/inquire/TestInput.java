package com.tsoft.jai.inquire;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public class TestInput {

    enum TextInputEnum {
        Text,
        CtrlC,
        CtrlD
    }

    private static final String USER_INTERRUPTION = "Ctrl+C";
    private static final String END_OF_FILE = "Ctrl+D";

    private final TextInputEnum type;
    private String text;

    public static TestInput Text(String text) {
        TestInput input = new TestInput(TextInputEnum.Text);
        input.text = text;
        return input;
    }

    public static TestInput CtrlC() {
        return new TestInput(TextInputEnum.CtrlC);
    }

    public static TestInput CtrlD() {
        return new TestInput(TextInputEnum.CtrlD);
    }

    public static boolean is(TestInput input, TextInputEnum type) {
        return (input != null && Objects.equals(input.type, type));
    }

    public String getValue() {
        return switch (type) {
            case Text -> text;
            case CtrlC -> USER_INTERRUPTION;
            case CtrlD -> END_OF_FILE;
        };
    }
}
