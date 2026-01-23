package com.tsoft.jai.utils;

import java.util.*;

public class TupleN {

    private enum TupleNType {
        MAP,
        LIST,
        ARRAY
    }

    private TupleNType type;
    private Map<String, Object> mapValues;
    private List<?> listValues;
    private Object[] arrayValues;
    private int arrayIndex = 0;
    private int listIndex;
    private Iterator<?> mapIterator;

    public static TupleN asArray(Object ... values) {
        TupleN tupleN = new TupleN();
        tupleN.arrayValues = values;
        tupleN.type = TupleNType.ARRAY;
        tupleN.arrayIndex = 0;
        return tupleN;
    }

    public static TupleN asList(Object ... values) {
        List<Object> list = (values == null) ? Collections.emptyList() : Arrays.asList(values);

        TupleN tupleN = new TupleN();
        tupleN.listValues = list;
        tupleN.type = TupleNType.LIST;
        tupleN.listIndex = 0;
        return tupleN;
    }

    public static TupleN asList(List<?> list) {
        TupleN tupleN = new TupleN();
        tupleN.listValues = list;
        tupleN.type = TupleNType.LIST;
        tupleN.listIndex = 0;
        return tupleN;
    }

    public static TupleN asLinkedMap(Object ... kvs) {
        Map<String, Object> map = Collections.emptyMap();
        if (kvs != null) {
            map = new LinkedHashMap<>();
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
            case LIST -> (T)listValues.get(listIndex ++);
            case MAP -> (T)mapIterator.next();
        };
    }

    public <T> T get(String key) {
        return switch (type) {
            case ARRAY, LIST -> throw new IllegalStateException("Not a map");
            case MAP -> (T)mapValues.get(key);
        };
    }
}
