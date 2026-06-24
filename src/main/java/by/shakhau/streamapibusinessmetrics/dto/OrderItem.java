package by.shakhau.streamapibusinessmetrics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderItem {
    private String productName;
    private int quantity;
    private double price;
    private Category category;

    public double getTotalPrice() {
        return quantity * price;
    }
}
