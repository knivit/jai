package com.tsoft.jai.inquire;

import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;

import java.util.ArrayList;
import java.util.List;

public class TestTerminalInput {

    private final List<TestInput> inputs = new ArrayList<>();
    private int index = 0;

    public void add(TestInput input) {
        inputs.add(input);
    }

    public String get() {
        TestInput input;
        if (index >= inputs.size()) {
            input = TestInput.CtrlD();       // End of file
        } else {
            input = inputs.get(index);
            index++;
        }

        if (TestInput.is(input, TestInput.TextInputEnum.CtrlD)) {
            throw new EndOfFileException("");
        }

        if (TestInput.is(input, TestInput.TextInputEnum.CtrlC)) {
            throw new UserInterruptException("");
        }

        return input.getValue();
    }
}
