package by.shakhau.streamapibusinessmetrics.util;

import by.shakhau.streamapibusinessmetrics.dto.Customer;
import by.shakhau.streamapibusinessmetrics.dto.Order;
import by.shakhau.streamapibusinessmetrics.dto.OrderItem;
import by.shakhau.streamapibusinessmetrics.dto.OrderStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OrderMetrics {

    public static Set<String> uniqueCities(List<Order> orders) {
        return orders.stream()
                .map(o -> o.getCustomer().getCity())
                .collect(Collectors.toSet());
    }

    public static double totalDeliveredIncome(List<Order> orders) {
        return orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .flatMap(o -> o.getItems().stream())
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
    }

    public static String mostPopularProduct(List<Order> orders) {
        return orders.stream()
                .flatMap(o -> o.getItems().stream())
                .collect(Collectors.groupingBy(
                        OrderItem::getProductName,
                        Collectors.summarizingInt(OrderItem::getQuantity))
                )
                .entrySet().stream()
                .max(Comparator.comparing(kv -> kv.getValue().getSum()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public static double averageDeliveredCheck(List<Order> orders) {
        return orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .mapToDouble(o -> o.getItems().stream()
                        .mapToDouble(OrderItem::getTotalPrice)
                        .sum()
                )
                .average()
                .orElse(0.0D);
    }

    public static Set<Customer> customersWithMoreThanOrders(List<Order> orders, int threshold) {
        return orders.stream()
                .collect(Collectors.groupingBy(Order::getCustomer, Collectors.counting()))
                .entrySet().stream()
                .filter(kv -> kv.getValue() > threshold)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
