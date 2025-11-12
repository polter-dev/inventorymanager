package com.codex.inventory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Coordinates state changes while keeping persistence in sync.
 */
public final class InventoryManager {
    private final List<InventoryItem> items = new ArrayList<>();
    private final InventoryStorage storage;

    public InventoryManager(Path storagePath) {
        this.storage = new InventoryStorage(storagePath);
        this.items.addAll(storage.load());
    }

    public synchronized List<InventoryItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public synchronized List<String> getCategories() {
        return items.stream()
                .map(InventoryItem::getCategory)
                .filter(s -> !s.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    public synchronized InventoryItem add(InventoryItem item) {
        items.add(item);
        persist();
        return item;
    }

    public synchronized InventoryItem update(UUID id, InventoryItem updated) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(id)) {
                items.set(i, updated);
                persist();
                return updated;
            }
        }
        throw new IllegalArgumentException("Item not found: " + id);
    }

    public synchronized void remove(UUID id) {
        items.removeIf(item -> item.getId().equals(id));
        persist();
    }

    public synchronized InventoryItem restock(UUID id, int delta) {
        for (int i = 0; i < items.size(); i++) {
            InventoryItem item = items.get(i);
            if (item.getId().equals(id)) {
                InventoryItem restocked = item.restock(delta);
                items.set(i, restocked);
                persist();
                return restocked;
            }
        }
        throw new IllegalArgumentException("Item not found: " + id);
    }

    public synchronized Optional<InventoryItem> findById(UUID id) {
        return items.stream().filter(it -> it.getId().equals(id)).findFirst();
    }

    public synchronized void persist() {
        items.sort(Comparator.comparing(InventoryItem::getName, String.CASE_INSENSITIVE_ORDER));
        storage.save(items);
    }

    public InventoryStorage storage() {
        return storage;
    }
}
