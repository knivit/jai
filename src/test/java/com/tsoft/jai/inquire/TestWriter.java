package com.tsoft.jai.inquire;

import java.io.OutputStream;
import java.io.PrintWriter;

public class TestWriter extends PrintWriter {

    public TestWriter(OutputStream out) {
        super(out);
    }

    @Override
    public void print(String msg) {
        super.print(msg);
        System.out.print(msg);
    }

    @Override
    public void println(String msg) {
        super.println(msg);
        System.out.println(msg);
    }

    @Override
    public void println() {
        super.println();
        System.out.println();
    }
}
