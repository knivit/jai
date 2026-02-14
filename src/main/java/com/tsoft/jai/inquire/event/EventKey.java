package com.tsoft.jai.inquire.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EventKey {

    private final int code;
    private final KeyModifiers keyModifiers;
}
