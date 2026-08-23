package com.aicompanyos.customerservice.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, length = 64)
    private String customerId;

    @Column(nullable = false, length = 255)
    private String product;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(length = 80)
    private String carrier;

    @Column(length = 80)
    private String trackingNumber;

    @Column(nullable = false, length = 32)
    private String estimatedDelivery;

    protected Order() {
    }

    public Order(String id, String customerId, String product, String status, String carrier, String trackingNumber, String estimatedDelivery) {
        this.id = id;
        this.customerId = customerId;
        this.product = product;
        this.status = status;
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.estimatedDelivery = estimatedDelivery;
    }

    public String id() { return id; }
    public String customerId() { return customerId; }
    public String product() { return product; }
    public String status() { return status; }
    public String carrier() { return carrier; }
    public String trackingNumber() { return trackingNumber; }
    public String estimatedDelivery() { return estimatedDelivery; }
}
