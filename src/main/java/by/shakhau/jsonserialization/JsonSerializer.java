package by.shakhau.jsonserialization;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonSerializer {

    private static final Set<Class<?>> PRIMITIVE_TYPES = new HashSet<>(
            Arrays.asList(
                    Integer.class, Long.class, Double.class, Float.class,
                    Boolean.class, Byte.class, Short.class
            )
    );

    private boolean prettyPrint = false;

    public void setPrettyPrint(boolean value) {
        this.prettyPrint = value;
    }

    public String serialize(Object obj) {
        var sb = new StringBuilder();
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        writeValue(obj, sb, visited, 0);
        return sb.toString();
    }

    private void writeValue(Object obj, StringBuilder sb, Set<Object> visited, int indent) {
        if (obj == null) {
            sb.append("null");
            return;
        }

        Class<?> type = obj.getClass();

        if (isPrimitiveOrWrapper(type) || obj instanceof String || obj instanceof Character) {
            writePrimitive(obj, sb);
            return;
        }

        if (!visited.add(obj)) {
            throw new SerializationException("Circular reference detected: " + type.getName());
        }

        if (type.isArray()) {
            writeArray(obj, sb, visited, indent);
            return;
        }

        if (obj instanceof Collection<?> col) {
            writeCollection(col, sb, visited, indent);
            return;
        }

        if (obj instanceof Map<?, ?> map) {
            writeMap(map, sb, visited, indent);
            return;
        }

        writeObject(obj, sb, visited, indent);
    }

    private void writeObject(Object obj, StringBuilder sb, Set<Object> visited, int indent) {
        Field[] fields = getAllFields(obj.getClass());

        sb.append("{");

        if (prettyPrint && fields.length > 0) {
            sb.append("\n");
        }

        var first = true;
        for (Field field : fields) {
            field.setAccessible(true);

            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isTransient(mod)) continue;

            if (field.isAnnotationPresent(Exclude.class)) continue;

            Object value;
            try {
                value = field.get(obj);
            } catch (IllegalAccessException e) {
                continue;
            }

            String name = field.getName();
            JsonName jsonName = field.getAnnotation(JsonName.class);
            if (jsonName != null) {
                name = jsonName.value();
            }

            if (!first) {
                sb.append(",");
                if (prettyPrint) sb.append("\n");
            }
            first = false;

            if (prettyPrint) {
                sb.append("  ".repeat(indent + 1));
            }

            sb.append("\"").append(name).append("\": ");

            writeValue(value, sb, visited, indent + 1);
        }

        if (prettyPrint && fields.length > 0) {
            sb.append("\n");
            sb.append("  ".repeat(indent));
        }

        sb.append("}");
    }

    private void writeArray(Object array, StringBuilder sb, Set<Object> visited, int indent) {
        int len = Array.getLength(array);

        sb.append("[");

        if (len > 0) {
            if (prettyPrint) {
                sb.append("\n");
            }

            for (var i = 0; i < len; i++) {
                if (i > 0) {
                    sb.append(",");
                    if (prettyPrint) {
                        sb.append("\n");
                    }
                }

                if (prettyPrint) {
                    sb.append("  ".repeat(indent + 1));
                }

                writeValue(Array.get(array, i), sb, visited, indent + 1);
            }

            if (prettyPrint) {
                sb.append("\n");
                sb.append("  ".repeat(indent));
            }
        }

        sb.append("]");
    }

    private void writeCollection(Collection<?> collection, StringBuilder sb, Set<Object> visited, int indent) {
        sb.append("[");

        Iterator<?> it = collection.iterator();
        var i = 0;
        var collectionIsEmpty = collection.isEmpty();

        if (prettyPrint && !collectionIsEmpty) {
            sb.append("\n");
        }

        while (it.hasNext()) {
            if (i > 0) {
                sb.append(",");
                if (prettyPrint) sb.append("\n");
            }

            if (prettyPrint) {
                sb.append("  ".repeat(indent + 1));
            }

            writeValue(it.next(), sb, visited, indent + 1);

            i++;
        }

        if (prettyPrint && !collectionIsEmpty) {
            sb.append("\n");
            sb.append("  ".repeat(indent));
        }

        sb.append("]");
    }

    private void writeMap(Map<?, ?> map, StringBuilder sb, Set<Object> visited, int indent) {
        sb.append("{");

        if (prettyPrint && !map.isEmpty()) {
            sb.append("\n");
        }

        var first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
                if (prettyPrint) {
                    sb.append("\n");
                }
            }

            first = false;

            if (prettyPrint) {
                sb.append("  ".repeat(indent + 1));
            }

            sb.append("\"").append(entry.getKey()).append("\": ");

            writeValue(entry.getValue(), sb, visited, indent + 1);
        }

        if (prettyPrint && !map.isEmpty()) {
            sb.append("\n");
            sb.append("  ".repeat(indent));
        }

        sb.append("}");
    }

    private void writePrimitive(Object obj, StringBuilder sb) {
        if (obj instanceof String || obj instanceof Character) {
            sb.append("\"");
            escape(obj.toString(), sb);
            sb.append("\"");
        } else {
            sb.append(obj);
        }
    }

    private boolean isPrimitiveOrWrapper(Class<?> type) {
        return PRIMITIVE_TYPES.contains(type);
    }

    private Field[] getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();

        while (type != null) {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            type = type.getSuperclass();
        }

        return fields.toArray(new Field[0]);
    }

    private void escape(String value, StringBuilder sb) {
        for (var i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
    }
}
