package com.jalen.ismael.beans.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.jalen.ismael.utils.ReflectUtils;

public class BasicStructure<T> implements Structure<T> { 
    private final Class<?> structureClass;
    private final Map<String, T> data;

    public BasicStructure(Class<?> structureClass) {
        this.structureClass = structureClass;
        data = new HashMap<>(8);
    }
    
    public static boolean isEmpty(Object object) {
        if (object == null) {
            return true;
        }
        if (object instanceof Collection) {
            return ((Collection<?>) object).isEmpty();
        }
        if (object instanceof Map) {
            return ((Map<?, ?>) object).isEmpty();
        }
        if (object instanceof Structure) {
            return ((Structure<?>) object).toMap().isEmpty();
        }
        return true;
    }

    @Override
    public void put(String key, T value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addAll(Object structure) {
        if (structure instanceof Collection) {
            Iterator<?> iterable = ((Collection<?>) structure).iterator();
            while (iterable.hasNext()) {
                data.put(String.valueOf(data.size()), (T) iterable.next());
            }
        } else if (structure instanceof Map) {
            data.putAll((Map<String, T>) structure);
        } else if (structure instanceof Structure) {
            data.putAll(((Structure<T>) structure).toMap());
        }
    }

    @Override
    public Map<String, T> toMap() {
        return data;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        if (isSupport(structureClass)) {
            Object result;
            try {
                result = ReflectUtils.newInstance(structureClass);
            } catch (Exception e) {
                if (Map.class.isAssignableFrom(structureClass)) {
                    return data;
                } else {
                    result = new ArrayList<>();
                }
            }
            if (Collection.class.isAssignableFrom(structureClass)) {
                ((Collection<Object>) result).addAll(data.values());
            } else {
                ((Map<String, Object>) result).putAll(data);
            }
            return result;
        } else {
            return this;
        }
    }

    public static boolean isSupport(Class<?> structureClass) {
        return Collection.class.isAssignableFrom(structureClass) || Map.class.isAssignableFrom(structureClass);
    }
}
