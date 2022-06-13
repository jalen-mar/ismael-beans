package com.jalen.ismael.beans.data;

import java.util.Map;

public interface Structure<T> extends Cloneable { 
    void put(String key, T value);

    void addAll(Object structure);

    Map<String, T> toMap();

    Object clone();
}
