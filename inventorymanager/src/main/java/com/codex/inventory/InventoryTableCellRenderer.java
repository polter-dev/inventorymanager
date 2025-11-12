package com.codex.inventory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Lightweight row-highlighting rules for the inventory table.
 */
public final class InventoryTableCellRenderer extends DefaultTableCellRenderer {
    private final InventoryTableModel model;
    private int lowStockThreshold = 5;
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);

    public InventoryTableCellRenderer(InventoryTableModel model) {
        this.model = model;
        setOpaque(true);
    }

    public void setLowStockThreshold(int threshold) {
        lowStockThreshold = Math.max(1, threshold);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
    ) {
        JLabel component = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (column == 4 && value instanceof Number) {
            Number number = (Number) value;
            component.setText(currency.format(number.doubleValue()));
            component.setHorizontalAlignment(SwingConstants.RIGHT);
        } else if (column == 2 && value instanceof Number) {
            component.setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            component.setHorizontalAlignment(SwingConstants.LEFT);
        }
        if (isSelected) {
            return component;
        }
        int modelRow = table.convertRowIndexToModel(row);
        InventoryItem item = model.getItemAt(modelRow);
        if (item == null) {
            return component;
        }
        component.setForeground(Color.DARK_GRAY);
        LocalDate today = LocalDate.now();
        boolean expired = item.getExpirationDate() != null && item.getExpirationDate().isBefore(today);
        boolean expiringSoon = item.getExpirationDate() != null && !expired
                && !item.getExpirationDate().isAfter(today.plusDays(3));
        boolean lowStock = item.getQuantity() <= lowStockThreshold;

        if (expired) {
            component.setBackground(new Color(244, 204, 204));
        } else if (expiringSoon) {
            component.setBackground(new Color(255, 243, 205));
        } else if (lowStock) {
            component.setBackground(new Color(226, 239, 218));
        } else {
            component.setBackground(Color.WHITE);
        }
        return component;
    }
}
