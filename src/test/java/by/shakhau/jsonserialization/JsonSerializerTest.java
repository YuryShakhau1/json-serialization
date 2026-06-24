package by.shakhau.jsonserialization;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonSerializerTest {

    static class SimplePojo {
        private String name = "Alex";
        private int age = 25;
        private boolean active = true;
    }

    static class Address {
        private String city = "Amsterdam";
        private String street = "Main St";
    }

    static class Person {
        private String name = "John";
        private Address address = new Address();
    }

    static class AnnotatedPojo {

        @JsonName("username")
        private String name = "Bob";

        @Exclude
        private String password = "secret";

        int age = 30;
    }

    static class Node {
        private String value;
        private Node next;
    }

    static class CollectionPojo {
        private List<String> list = Arrays.asList("A", "B", "C");
        private Map<String, Integer> map = new LinkedHashMap<>();

        CollectionPojo() {
            map.put("x", 1);
            map.put("y", 2);
        }
    }

    @Test
    void shouldSerializeSimplePojoWhenObjectHasPrimitiveFields() {
        JsonSerializer serializer = new JsonSerializer();
        SimplePojo obj = new SimplePojo();

        String json = serializer.serialize(obj);

        assertNotNull(json);
        assertTrue(json.startsWith("{"));
        assertTrue(json.endsWith("}"));

        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"Alex\""));
        assertTrue(json.contains("\"age\""));
        assertTrue(json.contains("25"));
        assertTrue(json.contains("\"active\""));
        assertTrue(json.contains("true"));
    }

    @Test
    void shouldSerializeNestedObjectWhenObjectContainsAnotherPojo() {
        JsonSerializer serializer = new JsonSerializer();
        Person p = new Person();

        String json = serializer.serialize(p);

        assertNotNull(json);

        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"John\""));

        assertTrue(json.contains("\"address\""));
        assertTrue(json.contains("\"city\""));
        assertTrue(json.contains("\"Amsterdam\""));
        assertTrue(json.contains("\"street\""));
        assertTrue(json.contains("\"Main St\""));
    }

    @Test
    void shouldSerializeCollectionsWhenObjectContainsListAndMap() {
        JsonSerializer serializer = new JsonSerializer();
        CollectionPojo obj = new CollectionPojo();

        String json = serializer.serialize(obj);

        assertNotNull(json);

        assertTrue(json.contains("\"list\""));
        assertTrue(json.contains("\"A\""));
        assertTrue(json.contains("\"B\""));
        assertTrue(json.contains("\"C\""));

        assertTrue(json.contains("\"map\""));
        assertTrue(json.contains("\"x\""));
        assertTrue(json.contains("1"));
        assertTrue(json.contains("\"y\""));
        assertTrue(json.contains("2"));
    }

    @Test
    void shouldRespectAnnotationsWhenJsonNameAndExcludeAreUsed() {
        JsonSerializer serializer = new JsonSerializer();
        AnnotatedPojo obj = new AnnotatedPojo();

        String json = serializer.serialize(obj);

        assertNotNull(json);

        assertTrue(json.contains("\"username\""));
        assertTrue(json.contains("\"Bob\""));

        assertFalse(json.contains("password"));
        assertFalse(json.contains("secret"));

        assertTrue(json.contains("\"age\""));
        assertTrue(json.contains("30"));
    }

    @Test
    void shouldThrowExceptionWhenCircularReferenceExists() {
        JsonSerializer serializer = new JsonSerializer();

        Node a = new Node();
        a.value = "A";

        Node b = new Node();
        b.value = "B";

        a.next = b;
        b.next = a;

        assertThrows(SerializationException.class, () -> serializer.serialize(a));
    }

    @Test
    void shouldFormatJsonWhenPrettyPrintIsEnabled() {
        JsonSerializer serializer = new JsonSerializer();
        serializer.setPrettyPrint(true);

        Person person = new Person();
        String json = serializer.serialize(person);

        assertTrue(json.contains("\n"));
        assertTrue(json.contains("    "));

        assertTrue(json.startsWith("{"));
        assertTrue(json.endsWith("}"));

        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"John\""));
    }

    @Test
    void shouldProduceCompactJsonWhenPrettyPrintIsDisabled() {
        JsonSerializer serializer = new JsonSerializer();
        serializer.setPrettyPrint(false);

        Person person = new Person();
        String json = serializer.serialize(person);

        assertFalse(json.contains("\n"));
        assertFalse(json.contains("  "));

        assertTrue(json.startsWith("{"));
        assertTrue(json.endsWith("}"));
    }
}
