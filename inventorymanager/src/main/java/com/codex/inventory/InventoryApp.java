package com.codex.inventory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Primary Swing UI for the grocery inventory manager.
 */
public final class InventoryApp extends JFrame {
    private final InventoryManager manager;
    private final InventoryTableModel tableModel = new InventoryTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> categoryFilter = new JComboBox<>();
    private final JCheckBox lowStockOnly = new JCheckBox("Low stock only");
    private final JSpinner lowStockSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 1000, 1));
    private final JLabel statusLabel = new JLabel("Ready");
    private final InventoryTableCellRenderer renderer = new InventoryTableCellRenderer(tableModel);
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);

    public InventoryApp() {
        super("Grocery Inventory Manager");
        this.manager = new InventoryManager(Paths.get("data", "inventory-data.csv"));
        buildUi();
        refreshCategoryFilter();
        refreshTable();
    }

    private void buildUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        setMinimumSize(new Dimension(960, 640));

        add(buildFilterPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        setJMenuBar(buildMenuBar());
        renderer.setLowStockThreshold((Integer) lowStockSpinner.getValue());
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem export = new JMenuItem("Export visible rows…");
        export.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        export.addActionListener(e -> exportVisibleRows());
        JMenuItem quit = new JMenuItem("Exit");
        quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        quit.addActionListener(e -> dispatchEvent(new java.awt.event.WindowEvent(this, java.awt.event.WindowEvent.WINDOW_CLOSING)));
        file.add(export);
        file.addSeparator();
        file.add(quit);
        bar.add(file);
        return bar;
    }

    private JPanel buildFilterPanel() {
        JPanel container = new JPanel();
        container.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        container.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        container.add(new JLabel("Search"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        container.add(searchField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        container.add(new JLabel("Category"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        container.add(categoryFilter, gbc);

        JPanel lowStockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        lowStockPanel.add(lowStockOnly);
        lowStockPanel.add(new JLabel("threshold"));
        lowStockPanel.add(lowStockSpinner);

        gbc.gridx = 0;
        gbc.gridy = 2;
        container.add(new JLabel("Stock health"), gbc);
        gbc.gridx = 1;
        container.add(lowStockPanel, gbc);

        attachFilterListeners();
        return container;
    }

    private void attachFilterListeners() {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshTable();
            }
        };
        searchField.getDocument().addDocumentListener(listener);
        categoryFilter.addActionListener(e -> refreshTable());
        lowStockOnly.addActionListener(e -> refreshTable());
        lowStockSpinner.addChangeListener(e -> {
            renderer.setLowStockThreshold((Integer) lowStockSpinner.getValue());
            refreshTable();
        });
    }

    private JPanel buildTablePanel() {
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        TableRowSorter<InventoryTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setComparator(2, Comparator.comparingInt(value -> (Integer) value));
        sorter.setComparator(4, Comparator.comparingDouble(value -> (Double) value));
        table.setRowSorter(sorter);

        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(String.class, renderer);
        table.setDefaultRenderer(Integer.class, renderer);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(createButton("Add", e -> onAdd()));
        toolbar.add(createButton("Edit", e -> onEdit()));
        toolbar.add(createButton("Restock", e -> onRestock()));
        toolbar.add(createButton("Remove", e -> onRemove()));
        toolbar.addSeparator();
        toolbar.add(createButton("Export CSV", e -> exportVisibleRows()));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        wrapper.add(toolbar, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        footer.add(statusLabel, BorderLayout.WEST);
        return footer;
    }

    private JButton createButton(String label, java.awt.event.ActionListener listener) {
        JButton button = new JButton(label);
        button.addActionListener(listener);
        return button;
    }

    private void onAdd() {
        InventoryFormDialog dialog = new InventoryFormDialog(this, "Add Item");
        InventoryFormDialog.FormData data = dialog.showDialog(null);
        if (data == null) {
            return;
        }
        InventoryItem item = InventoryItem.create(
                data.name(),
                data.category(),
                data.quantity(),
                data.unit(),
                data.price(),
                data.expiration()
        );
        manager.add(item);
        refreshCategoryFilter();
        refreshTable();
        setStatus("Added " + item.getName());
    }

    private void onEdit() {
        InventoryItem selected = getSelectedItem();
        if (selected == null) {
            return;
        }
        InventoryFormDialog dialog = new InventoryFormDialog(this, "Edit Item");
        InventoryFormDialog.FormData data = dialog.showDialog(selected);
        if (data == null) {
            return;
        }
        InventoryItem updated = selected.update(
                data.name(),
                data.category(),
                data.quantity(),
                data.unit(),
                data.price(),
                data.expiration()
        );
        manager.update(selected.getId(), updated);
        refreshCategoryFilter();
        refreshTable();
        setStatus("Updated " + updated.getName());
    }

    private void onRestock() {
        InventoryItem selected = getSelectedItem();
        if (selected == null) {
            return;
        }
        String input = JOptionPane.showInputDialog(this, "Add units to " + selected.getName(), "Restock", JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.isBlank()) {
            return;
        }
        try {
            int amount = Integer.parseInt(input.trim());
            if (amount <= 0) {
                throw new NumberFormatException();
            }
            InventoryItem restocked = manager.restock(selected.getId(), amount);
            refreshTable();
            setStatus("Restocked " + restocked.getName() + " by +" + amount);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a positive whole number.", "Invalid amount", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onRemove() {
        InventoryItem selected = getSelectedItem();
        if (selected == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Remove " + selected.getName() + "?",
                "Delete Item",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            manager.remove(selected.getId());
            refreshCategoryFilter();
            refreshTable();
            setStatus("Removed " + selected.getName());
        }
    }

    private InventoryItem getSelectedItem() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a row first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return tableModel.getItemAt(modelRow);
    }

    private void exportVisibleRows() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("inventory-export.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path target = chooser.getSelectedFile().toPath();
        try {
            List<String> lines = buildExportLines();
            Files.write(target, lines, StandardCharsets.UTF_8);
            JOptionPane.showMessageDialog(this, "Exported " + (lines.size() - 1) + " rows to " + target, "Export complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to export: " + e.getMessage(), "Export error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<String> buildExportLines() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter updatedFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        List<String> lines = new ArrayList<>();
        lines.add("Name,Category,Quantity,Unit,Price,Expiration,Last Updated");
        for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            InventoryItem item = tableModel.getItemAt(modelRow);
            String expiration = item.getExpirationDate() == null ? "" : dateFormatter.format(item.getExpirationDate());
            lines.add(String.join(",",
                    escapeCsv(item.getName()),
                    escapeCsv(item.getCategory()),
                    Integer.toString(item.getQuantity()),
                    escapeCsv(item.getUnit()),
                    String.format(Locale.US, "%.2f", item.getPrice()),
                    expiration,
                    escapeCsv(updatedFormatter.format(item.getUpdatedAt()))
            ));
        }
        return lines;
    }

    private String escapeCsv(String value) {
        String safe = value == null ? "" : value;
        if (safe.contains(",") || safe.contains("\"") || safe.contains("\n")) {
            safe = safe.replace("\"", "\"\"");
            return "\"" + safe + "\"";
        }
        return safe;
    }

    private void refreshTable() {
        List<InventoryItem> filtered = applyFilters(manager.getItems());
        tableModel.setItems(filtered);
        double totalValue = filtered.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        statusLabel.setText(String.format("%d items • %s on hand", filtered.size(), currency.format(totalValue)));
    }

    private List<InventoryItem> applyFilters(List<InventoryItem> items) {
        String search = searchField.getText().strip().toLowerCase(Locale.ROOT);
        String chosenCategory = Optional.ofNullable((String) categoryFilter.getSelectedItem())
                .filter(s -> !s.equalsIgnoreCase("All categories"))
                .orElse(null);
        boolean onlyLowStock = lowStockOnly.isSelected();
        int threshold = (Integer) lowStockSpinner.getValue();

        return items.stream()
                .filter(item -> search.isBlank()
                        || item.getName().toLowerCase(Locale.ROOT).contains(search)
                        || item.getCategory().toLowerCase(Locale.ROOT).contains(search))
                .filter(item -> chosenCategory == null || item.getCategory().equalsIgnoreCase(chosenCategory))
                .filter(item -> !onlyLowStock || item.getQuantity() <= threshold)
                .sorted(Comparator.comparing(InventoryItem::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    private void refreshCategoryFilter() {
        String previous = (String) categoryFilter.getSelectedItem();
        categoryFilter.removeAllItems();
        categoryFilter.addItem("All categories");
        for (String category : manager.getCategories()) {
            categoryFilter.addItem(category);
        }
        if (previous != null) {
            categoryFilter.setSelectedItem(previous);
        }
        if (categoryFilter.getSelectedIndex() == -1) {
            categoryFilter.setSelectedIndex(0);
        }
    }

    private void setStatus(String message) {
        statusLabel.setText(message + " • " + currency.format(tableModel.getTotalValue()) + " in stock");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            InventoryApp app = new InventoryApp();
            app.setVisible(true);
        });
    }
}
