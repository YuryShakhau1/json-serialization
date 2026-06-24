package by.shakhau.streamapibusinessmetrics.util;

import by.shakhau.streamapibusinessmetrics.dto.Category;
import by.shakhau.streamapibusinessmetrics.dto.Customer;
import by.shakhau.streamapibusinessmetrics.dto.Order;
import by.shakhau.streamapibusinessmetrics.dto.OrderItem;
import by.shakhau.streamapibusinessmetrics.dto.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderMetricsTest {

    private List<Order> sampleOrders;

    private Customer alice;
    private Customer bob;
    private Customer carl;

    @BeforeEach
    void setup() {

        // Customers
        alice = new Customer("Customer1", "Alice", "a@mail.com", LocalDateTime.now(), 30, "New York");
        bob = new Customer("Customer2", "Bob", "b@mail.com", LocalDateTime.now(), 25, "Berlin");
        carl = new Customer("Customer3", "Carl", "c@mail.com", LocalDateTime.now(), 40, "Paris");

        // Orders
        sampleOrders = List.of(

                // Alice has 6 orders. It necessary for metric #5
                new Order("Order1", LocalDateTime.now(), alice,
                        List.of(
                                new OrderItem("iPhone", 1, 1000, Category.ELECTRONICS)
                        ),
                        OrderStatus.DELIVERED),

                new Order("Order2", LocalDateTime.now(), alice,
                        List.of(
                                new OrderItem("Book", 2, 20, Category.BOOKS)
                        ),
                        OrderStatus.DELIVERED),

                new Order("Order3", LocalDateTime.now(), alice,
                        List.of(
                                new OrderItem("Laptop", 1, 1500, Category.ELECTRONICS)
                        ),
                        OrderStatus.CANCELLED),

                new Order("Order4", LocalDateTime.now(), alice,
                        List.of(
                                new OrderItem("Toy", 3, 15, Category.TOYS)
                        ),
                        OrderStatus.SHIPPED),

                new Order("Order5", LocalDateTime.now(), alice,
                        List.of(
                                new OrderItem("Headphones", 1, 200, Category.ELECTRONICS)
                        ),
                        OrderStatus.DELIVERED),

                new Order("Order6", LocalDateTime.now(), alice,
                        List.of(
                                new OrderItem("Mouse", 2, 50, Category.ELECTRONICS)
                        ),
                        OrderStatus.DELIVERED),


                // Bob has 4 orders
                new Order("Order7", LocalDateTime.now(), bob,
                        List.of(
                                new OrderItem("Shirt", 2, 40, Category.CLOTHING)
                        ),
                        OrderStatus.DELIVERED),

                new Order("Order8", LocalDateTime.now(), bob,
                        List.of(
                                new OrderItem("Book", 1, 30, Category.BOOKS)
                        ),
                        OrderStatus.DELIVERED),

                new Order("Order9", LocalDateTime.now(), bob,
                        List.of(
                                new OrderItem("Shoes", 1, 100, Category.CLOTHING)
                        ),
                        OrderStatus.CANCELLED),

                new Order("Order10", LocalDateTime.now(), bob,
                        List.of(
                                new OrderItem("Hat", 2, 25, Category.CLOTHING)
                        ),
                        OrderStatus.PROCESSING),


                // Carl has 3 orders
                new Order("Order11", LocalDateTime.now(), carl,
                        List.of(
                                new OrderItem("Tablet", 1, 600, Category.ELECTRONICS)
                        ),
                        OrderStatus.DELIVERED),

                new Order("Order12", LocalDateTime.now(), carl,
                        List.of(
                                new OrderItem("Book", 3, 20, Category.BOOKS)
                        ),
                        OrderStatus.DELIVERED),

                new Order("Order13", LocalDateTime.now(), carl,
                        List.of(
                                new OrderItem("Perfume", 1, 80, Category.BEAUTY)
                        ),
                        OrderStatus.SHIPPED)
        );
    }

    @Test
    void shouldReturnCorrectUniqueCities() {
        Set<String> result = OrderMetrics.uniqueCities(sampleOrders);

        assertEquals(3, result.size());
        assertTrue(result.contains("New York"));
        assertTrue(result.contains("Berlin"));
        assertTrue(result.contains("Paris"));
    }

    @Test
    void shouldCalculateTotalDeliveredIncome() {
        double result = OrderMetrics.totalDeliveredIncome(sampleOrders);

        assertEquals(2110.0, result, 0.01);
    }

    @Test
    void shouldReturnMostPopularProduct() {
        String result = OrderMetrics.mostPopularProduct(sampleOrders);

        assertEquals("Book", result);
    }

    @Test
    void shouldCalculateAverageDeliveredCheck() {
        double result = OrderMetrics.averageDeliveredCheck(sampleOrders);

        assertEquals(263.75, result);
    }

    @Test
    void shouldReturnCustomersWithMoreThanFiveOrders() {
        Set<Customer> result = OrderMetrics.customersWithMoreThanOrders(sampleOrders, 5);

        assertEquals(1, result.size());
        assertTrue(result.contains(alice));
    }
}
