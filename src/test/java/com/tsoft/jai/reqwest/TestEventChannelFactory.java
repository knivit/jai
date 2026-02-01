package com.tsoft.jai.reqwest;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.BlockingQueue;

@RequiredArgsConstructor
public class TestEventChannelFactory implements EventSource.EventChannelFactory {

    private final BlockingQueue<String> eventChannel;

    @Override
    public BlockingQueue<String> build() {
        return eventChannel;
    }
}
