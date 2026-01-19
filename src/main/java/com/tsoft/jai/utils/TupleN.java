package com.tsoft.jai.utils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class TupleN {

    private enum TupleNType {
        MAP,
        ARRAY
    }

    private TupleNType type;
    private Map<String, Object> mapValues;
    private Object[] arrayValues;
    private int arrayIndex = 0;
    private Iterator<?> mapIterator;

    public static TupleN asArray(Object ... values) {
        TupleN tupleN = new TupleN();
        tupleN.arrayValues = values;
        tupleN.type = TupleNType.ARRAY;
        tupleN.arrayIndex = 0;
        return tupleN;
    }

    public static TupleN asMap(Object ... kvs) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (kvs != null) {
            for (int i = 0; i < kvs.length; i += 2) {
                map.put(kvs[i].toString(), kvs[i + 1]);
            }
        }

        TupleN tupleN = new TupleN();
        tupleN.mapValues = map;
        tupleN.type = TupleNType.MAP;
        tupleN.mapIterator = map.values().iterator();
        return tupleN;
    }

    public <T> T next() {
        return switch (type) {
            case ARRAY -> (T)arrayValues[arrayIndex ++];
            case MAP -> (T)mapIterator.next();
        };
    }

    public <T> T get(String key) {
        return switch (type) {
            case ARRAY -> throw new IllegalStateException("Not a map");
            case MAP -> (T)mapValues.get(key);
        };
    }
}
