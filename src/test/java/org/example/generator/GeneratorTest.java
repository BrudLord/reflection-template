package org.example.generator;

import org.example.classes.BinaryTreeNode;
import org.example.classes.Cart;
import org.example.classes.Example;
import org.example.classes.Product;
import org.example.classes.Rectangle;
import org.example.classes.Shape;
import org.example.classes.Triangle;
import org.example.exeptions.GenerationException;
import org.example.generator.classes.AbstractExample;
import org.example.generator.classes.ChildExample;
import org.example.generator.classes.Color;
import org.example.generator.classes.ConcreteExample;
import org.example.generator.classes.EmptyEnum;
import org.example.generator.classes.MapExample;
import org.example.generator.classes.ParentExample;
import org.example.generator.classes.ThrowingExample;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorTest {
    private final Generator generator = new Generator();

    @Test
    void generatesScalarTest() {
        List<Class<?>> types = List.of(
                Byte.class,
                Short.class,
                Integer.class,
                Long.class,
                Float.class,
                Double.class,
                Boolean.class,
                Character.class,
                String.class
        );
        for (Class<?> type : types) {
            assertInstanceOf(type, generator.generateValueOfType(type));
        }
    }

    @Test
    void generatesPrimitivesTest() {
        Map<Class<?>, Class<?>> types = Map.of(
                byte.class, Byte.class,
                short.class, Short.class,
                int.class, Integer.class,
                long.class, Long.class,
                float.class, Float.class,
                double.class, Double.class,
                boolean.class, Boolean.class,
                char.class, Character.class
        );

        for (var type : types.entrySet()) {
            assertInstanceOf(
                    type.getValue(),
                    generator.generateValueOfType(type.getKey())
            );
        }
    }

    @Test
    void generatesArrayTest() {
        List<Class<?>> types = List.of(
                int[].class,
                String[].class,
                Color[].class,
                Rectangle[].class
        );
        for (Class<?> type : types) {
            Object result = assertInstanceOf(type, generator.generateValueOfType(type));
            if (result instanceof Object[] array) {
                for (Object item : array) {
                    assertNotNull(item);
                }
            }
        }
    }

    @Test
    void generatesNestedArrayTest() {
        int[][] result = assertInstanceOf(
                int[][].class,
                generator.generateValueOfType(int[][].class)
        );
        for (int[] array : result) {
            assertNotNull(array);
        }
    }

    @Test
    void generatesEnumTest() {
        assertInstanceOf(
                Color.class,
                generator.generateValueOfType(Color.class)
        );
    }

    @Test
    void rejectsEmptyEnumTest() {
        assertThrows(
                GenerationException.class,
                () -> generator.generateValueOfType(EmptyEnum.class)
        );
    }

    @Test
    void generatesListTest() {
        Cart cart = assertInstanceOf(
                Cart.class,
                generator.generateValueOfType(Cart.class)
        );
        List<?> items = cart.getItems();

        assertNotNull(items);
        assertFalse(items.isEmpty());
        for (Object item : items) {
            Product product = assertInstanceOf(Product.class, item);
            assertNotNull(product.getName());
        }
    }

    @Test
    void generatesMapTest() {
        MapExample example = assertInstanceOf(
                MapExample.class,
                generator.generateValueOfType(MapExample.class)
        );
        Map<?, ?> entries = example.getEntries();

        assertNotNull(entries);
        assertFalse(entries.isEmpty());
        for (var entry : entries.entrySet()) {
            assertInstanceOf(String.class, entry.getKey());
            assertInstanceOf(Integer.class, entry.getValue());
        }
    }

    @Test
    void generatesObjectsTest() {
        List<Class<?>> types = List.of(
                Example.class,
                Rectangle.class,
                Triangle.class
        );
        for (Class<?> type : types) {
            assertInstanceOf(type, generator.generateValueOfType(type));
        }
    }

    @Test
    void generatesShapeTest() {
        assertInstanceOf(
                Shape.class,
                generator.generateValueOfType(Shape.class)
        );
    }

    @Test
    void generatesAbstractClassTest() {
        assertInstanceOf(
                ConcreteExample.class,
                generator.generateValueOfType(AbstractExample.class)
        );
    }

    @Test
    void generatesSubtypeAfterParentFailureTest() {
        assertInstanceOf(
                ChildExample.class,
                generator.generateValueOfType(ParentExample.class)
        );
    }

    @Test
    void preservesConstructorCauseTest() {
        assertThrows(
                GenerationException.class,
                () -> generator.generateValueOfType(ThrowingExample.class)
        );
    }

    @Test
    void generatesRecursiveTest() {
        BinaryTreeNode root = assertInstanceOf(
                BinaryTreeNode.class,
                generator.generateValueOfType(BinaryTreeNode.class)
        );
        assertTreeDepth(root, 0);
    }

    private void assertTreeDepth(BinaryTreeNode node, int depth) {
        if (depth == Generator.MAX_DEPTH) {
            assertNull(node, "На глубине " + Generator.MAX_DEPTH + " узлов быть не должно");
            return;
        }
        assertNotNull(node);
        assertTreeDepth(node.getLeft(), depth + 1);
        assertTreeDepth(node.getRight(), depth + 1);
    }
}
