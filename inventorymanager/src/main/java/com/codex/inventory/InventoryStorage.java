package com.codex.inventory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Handles persistence of inventory data using a lightweight pipe-delimited format.
 */
public final class InventoryStorage {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final Path filePath;

    public InventoryStorage(Path filePath) {
        this.filePath = filePath;
    }

    public List<InventoryItem> load() {
        try {
            ensureFileExists();
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            List<InventoryItem> items = new ArrayList<>();
            for (String line : lines) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                parseLine(line).ifPresent(items::add);
            }
            if (items.isEmpty()) {
                items.addAll(defaultItems());
                save(items);
            }
            return items;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load inventory data", e);
        }
    }

    public void save(List<InventoryItem> items) {
        try {
            ensureFileExists();
            List<String> lines = new ArrayList<>();
            lines.add("# inventory-data v1");
            for (InventoryItem item : items) {
                lines.add(formatItem(item));
            }
            Files.write(
                    filePath,
                    lines,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save inventory data", e);
        }
    }

    private void ensureFileExists() throws IOException {
        Files.createDirectories(filePath.getParent());
        if (Files.notExists(filePath)) {
            Files.createFile(filePath);
        }
    }

    private String formatItem(InventoryItem item) {
        String expiration = item.getExpirationDate() == null ? "-" : DATE_FORMAT.format(item.getExpirationDate());
        return String.join("|",
                item.getId().toString(),
                encode(item.getName()),
                encode(item.getCategory()),
                Integer.toString(item.getQuantity()),
                encode(item.getUnit()),
                String.format("%.2f", item.getPrice()),
                expiration,
                DATE_TIME_FORMAT.format(item.getUpdatedAt())
        );
    }

    private java.util.Optional<InventoryItem> parseLine(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 8) {
            return java.util.Optional.empty();
        }
        try {
            UUID id = UUID.fromString(parts[0]);
            String name = decode(parts[1]);
            String category = decode(parts[2]);
            int quantity = Integer.parseInt(parts[3]);
            String unit = decode(parts[4]);
            double price = Double.parseDouble(parts[5]);
            LocalDate expiration = "-".equals(parts[6]) || parts[6].isBlank()
                    ? null
                    : LocalDate.parse(parts[6], DATE_FORMAT);
            LocalDateTime updatedAt = LocalDateTime.parse(parts[7], DATE_TIME_FORMAT);
            return java.util.Optional.of(new InventoryItem(
                    id,
                    name,
                    category,
                    quantity,
                    unit,
                    price,
                    expiration,
                    updatedAt
            ));
        } catch (Exception ex) {
            return java.util.Optional.empty();
        }
    }

    private List<InventoryItem> defaultItems() {
        List<InventoryItem> defaults = new ArrayList<>();
        defaults.add(InventoryItem.create("Gala Apples", "Produce", 40, "lbs", 1.69, LocalDate.now().plusDays(10)));
        defaults.add(InventoryItem.create("Organic Spinach", "Produce", 18, "bags", 3.99, LocalDate.now().plusDays(5)));
        defaults.add(InventoryItem.create("Whole Milk", "Dairy", 25, "gallons", 4.49, LocalDate.now().plusDays(7)));
        defaults.add(InventoryItem.create("Brown Eggs", "Dairy", 32, "dozens", 2.59, LocalDate.now().plusDays(14)));
        defaults.add(InventoryItem.create("Sourdough Bread", "Bakery", 12, "loaves", 5.25, LocalDate.now().plusDays(2)));
        defaults.add(InventoryItem.create("Ground Coffee", "Pantry", 20, "bags", 11.99, LocalDate.now().plusMonths(3)));
        return defaults;
    }

    private String encode(String value) {
        String safe = value == null ? "" : value;
        return Base64.getEncoder().encodeToString(safe.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return "";
        }
        return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    }

    public Path getFilePath() {
        return filePath;
    }
}
