package com.codex.inventory;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Table model backing the inventory JTable.
 */
public final class InventoryTableModel extends AbstractTableModel {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, h:mm a");
    private final String[] columns = {"Name", "Category", "Quantity", "Unit", "Price", "Expires", "Updated"};
    private final List<InventoryItem> rows = new ArrayList<>();

    public void setItems(List<InventoryItem> items) {
        rows.clear();
        rows.addAll(items);
        fireTableDataChanged();
    }

    public InventoryItem getItemAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            return null;
        }
        return rows.get(rowIndex);
    }

    public List<InventoryItem> getItems() {
        return Collections.unmodifiableList(rows);
    }

    public double getTotalValue() {
        return rows.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        InventoryItem item = rows.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return item.getName();
            case 1:
                return item.getCategory();
            case 2:
                return item.getQuantity();
            case 3:
                return item.getUnit();
            case 4:
                return item.getPrice();
            case 5:
                return item.getExpirationDate() == null ? "—" : DATE_FORMAT.format(item.getExpirationDate());
            case 6:
                return DATE_TIME_FORMAT.format(item.getUpdatedAt());
            default:
                return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 2:
                return Integer.class;
            case 4:
                return Double.class;
            default:
                return String.class;
        }
    }
}
