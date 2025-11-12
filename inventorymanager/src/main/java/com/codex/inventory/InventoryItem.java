package com.codex.inventory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable representation of a grocery inventory record.
 */
public final class InventoryItem {
    private final UUID id;
    private final String name;
    private final String category;
    private final int quantity;
    private final String unit;
    private final double price;
    private final LocalDate expirationDate;
    private final LocalDateTime updatedAt;

    InventoryItem(
            UUID id,
            String name,
            String category,
            int quantity,
            String unit,
            double price,
            LocalDate expirationDate,
            LocalDateTime updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = name.strip();
        this.category = category.strip();
        this.quantity = Math.max(0, quantity);
        this.unit = unit.strip();
        this.price = Math.max(0, price);
        this.expirationDate = expirationDate;
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static InventoryItem create(
            String name,
            String category,
            int quantity,
            String unit,
            double price,
            LocalDate expirationDate
    ) {
        return new InventoryItem(
                UUID.randomUUID(),
                sanitize(name),
                sanitize(category),
                quantity,
                sanitize(unit),
                price,
                expirationDate,
                LocalDateTime.now()
        );
    }

    public InventoryItem update(
            String name,
            String category,
            int quantity,
            String unit,
            double price,
            LocalDate expirationDate
    ) {
        return new InventoryItem(
                id,
                sanitize(name),
                sanitize(category),
                quantity,
                sanitize(unit),
                price,
                expirationDate,
                LocalDateTime.now()
        );
    }

    public InventoryItem restock(int amount) {
        return new InventoryItem(
                id,
                name,
                category,
                Math.max(0, quantity + amount),
                unit,
                price,
                expirationDate,
                LocalDateTime.now()
        );
    }

    private static String sanitize(String value) {
        return value == null ? "" : value.strip();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public double getPrice() {
        return price;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
