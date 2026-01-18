package com.tsoft.jai.render.markdown;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RenderOptions {

    //private Theme theme;
    private String wrap;
    private boolean wrapCode;
    private boolean truecolor;
}
