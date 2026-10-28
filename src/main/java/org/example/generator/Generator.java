package org.example.generator;

import org.example.annotation.Generatable;
import org.example.exeptions.GenerationException;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class Generator {
    public static final int MAX_DEPTH = 10;
    public static final int MAX_SIZE = 20;

    private static final Random random = new Random();

    public Object generateValueOfType(Class<?> type) {
        return generate(type, 0);
    }

    private Object generate(Type type, int depth) {
        boolean primitive = type instanceof Class<?> && ((Class<?>) type).isPrimitive();
        if (depth >= MAX_DEPTH && !primitive) {
            return null;
        }

        if (type instanceof ParameterizedType parameterizedType) {
            if (parameterizedType.getRawType() == List.class) {
                return generateList(parameterizedType.getActualTypeArguments()[0], depth);
            }
            if (parameterizedType.getRawType() == Map.class) {
                return generateMap(
                        parameterizedType.getActualTypeArguments()[0],
                        parameterizedType.getActualTypeArguments()[1],
                        depth);
            }
        }

        if (type instanceof Class<?> clazz) {
            if (clazz.isEnum()) {
                return generateEnum(clazz);
            }
            if (clazz.isArray()) {
                return generateArray(clazz.componentType(), depth);
            }
            if (isScalar(clazz)) {
                return generateScalar(clazz);
            }
            return generateObject(clazz, depth);
        }

        throw new UnsupportedOperationException("Не смогли определить генератор " + type);
    }

    private boolean isScalar(Class<?> type) {
        return type.isPrimitive()
                || type == Byte.class
                || type == Short.class
                || type == Integer.class
                || type == Long.class
                || type == Float.class
                || type == Double.class
                || type == Boolean.class
                || type == Character.class
                || type == String.class;
    }

    private Object generateScalar(Class<?> type) {
        if (type == int.class || type == Integer.class) {
            return random.nextInt();
        }
        if (type == double.class || type == Double.class) {
            return random.nextDouble();
        }
        if (type == long.class || type == Long.class) {
            return random.nextLong();
        }
        if (type == float.class || type == Float.class) {
            return random.nextFloat();
        }
        if (type == boolean.class || type == Boolean.class) {
            return random.nextBoolean();
        }
        if (type == byte.class || type == Byte.class) {
            return (byte) random.nextInt(128);
        }
        if (type == char.class || type == Character.class) {
            return (char) random.nextInt(65535);
        }
        if (type == short.class || type == Short.class) {
            return (short) random.nextInt(32000);
        }
        if (type == String.class) {
            int size = random.nextInt(MAX_SIZE);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < size; i++) {
                stringBuilder.append(generateScalar(Character.class));
            }
            return stringBuilder.toString();
        }
        throw new GenerationException("Неизвестный примитивный тип: " + type);
    }

    private Object generateEnum(Class<?> type) {
        var constants = type.getEnumConstants();
        if (constants.length == 0) {
            throw new GenerationException("Нельзя создать пустой enum");
        }
        return constants[random.nextInt(constants.length)];
    }

    private Object generateArray(Class<?> componentType, int depth) {
        var size = random.nextInt(MAX_SIZE);
        var result = Array.newInstance(componentType, size);
        for (int i = 0; i < size; i++) {
            Array.set(result, i, generate(componentType, depth + 1));
        }
        return result;
    }

    private Object generateList(Type elementType, int depth) {
        var size = 1 + random.nextInt(MAX_SIZE);
        var result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(generate(elementType, depth + 1));
        }
        return result;
    }

    private Object generateMap(Type keyType, Type valueType, int depth) {
        var size = 1 + random.nextInt(MAX_SIZE);
        var result = new HashMap<>();
        for (int i = 0; i < size; i++) {
            result.put(generate(keyType, depth + 1), generate(valueType, depth + 1));
        }
        return result;
    }

    private Object generateObject(Class<?> type, int depth) {
        GenerationException exception = new GenerationException("Не получилось создать объект " + type);
        for (var constructor : type.getConstructors()) {
            try {
                Type[] paramTypes = constructor.getGenericParameterTypes();
                Object[] params = new Object[paramTypes.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    params[i] = generate(paramTypes[i], depth + 1);
                }
                return constructor.newInstance(params);
            } catch (Exception e) {
                exception.addSuppressed(e);
            }
        }
        if (type.getAnnotation(Generatable.class) != null) {
            try {
                return generateSubtype(type, depth);
            } catch (Exception e) {
                exception.addSuppressed(e);
            }
        }
        throw exception;
    }

    private Object generateSubtype(Class<?> type, int depth) {
        GenerationException exception = new GenerationException("Не получилось создать через аннотацию " + type);
        var subtypes = type.getAnnotation(Generatable.class).subtypes();
        for (var subtype : subtypes) {
            if (!type.isAssignableFrom(subtype)) {
                continue;
            }
            try {
                return generate(subtype, depth + 1);
            } catch (Exception e) {
                exception.addSuppressed(e);
            }

        }
        throw exception;
    }
}
