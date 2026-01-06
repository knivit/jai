package com.tsoft.jai.function;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class Functions {

    private List<FunctionDeclaration> declarations;
}
