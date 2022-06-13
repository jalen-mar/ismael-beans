package com.jalen.ismael.beans.config;

import com.jalen.ismael.beans.factory.BeanFactory;

public class Arguments { 
    private final Class<?>[] argumentType;
    private final String[] argumentName;

    public Arguments(Class<?>[] argumentType, String[] argumentName) {
        this.argumentType = argumentType;
        this.argumentName = argumentName;
    }

    public Class<?>[] getArgumentType() {
        return argumentType;
    }

    public Object[] getArgumentObject(BeanFactory beanFactory, String[] args) {
        int count = argumentType.length;
        Object[] result = new Object[count];
        int index = 0;
        for (int i = 0; i < count; i++) {
            Class<?> argument = argumentType[i];
            if (argument.isPrimitive() || String.class.isAssignableFrom(argument)) {
                result[i] = parseObject(args[index++], argument);
            } else {
                if (argumentName == null || argumentName[i] == null) {
                    result[i] = beanFactory.getBean(argument);
                } else {
                    result[i] = beanFactory.getBean(argumentName[i]);
                }
            }
        }
        return result;
    }

    public boolean isReference(String[] args) {
        int index = 0;
        for (Class<?> argument : argumentType) {
            if (argument.isPrimitive() || String.class.isAssignableFrom(argument)) {
                try {
                    parseObject(args[index++], argument);
                } catch (ArrayIndexOutOfBoundsException e) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Object parseObject(String param, Class<?> cls) {
        if (cls == int.class) {
            return Integer.parseInt(param);
        }
        if (cls == String.class) {
            return param;
        }
        if (cls == boolean.class) {
            return Boolean.parseBoolean(param);
        }
        if (cls == float.class) {
            return Float.parseFloat(param);
        }
        if (cls == byte.class) {
            return Byte.parseByte(param);
        }
        if (cls == long.class) {
            return Long.parseLong(param);
        }
        if (cls == short.class) {
            return Short.parseShort(param);
        }
        if (cls == double.class) {
            return Double.parseDouble(param);
        }
        if (cls == char.class) {
            return Integer.parseInt(param);
        }
        throw new RuntimeException("For input string: \"" + param + "\"");
    }
}
