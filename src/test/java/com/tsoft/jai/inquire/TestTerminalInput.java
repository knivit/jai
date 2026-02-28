package com.tsoft.jai.inquire;

import java.util.ArrayList;
import java.util.List;

public class TestTerminalInput {

    private final List<TestInput> inputs = new ArrayList<>();
    private int index = 0;

    public void add(TestInput input) {
        inputs.add(input);
    }

    public TestInput get() {
        if (index >= inputs.size()) {
            return TestInput.CtrlD();       // End of file
        }

        TestInput input = inputs.get(index);
        index ++;

        return input;
    }
}
