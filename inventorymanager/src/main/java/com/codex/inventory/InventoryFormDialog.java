package com.codex.inventory;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Modal dialog for creating and editing inventory entries.
 */
public final class InventoryFormDialog extends JDialog {
    private final JTextField nameField = new JTextField(20);
    private final JTextField categoryField = new JTextField(20);
    private final JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 0, 10_000, 1));
    private final JTextField unitField = new JTextField(10);
    private final JSpinner priceSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10_000.0, 0.25));
    private final JTextField expirationField = new JTextField(10);
    private FormData result;

    public InventoryFormDialog(JFrame owner, String title) {
        super(owner, title, true);
        buildUi();
        setResizable(false);
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUi() {
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;

        addRow(formPanel, gbc, "Name", nameField);
        addRow(formPanel, gbc, "Category", categoryField);
        addRow(formPanel, gbc, "Quantity", quantitySpinner);
        addRow(formPanel, gbc, "Unit", unitField);
        addRow(formPanel, gbc, "Price (per unit)", priceSpinner);
        expirationField.setToolTipText("yyyy-MM-dd");
        addRow(formPanel, gbc, "Expiration (yyyy-MM-dd)", expirationField);

        JLabel helper = new JLabel("Leave expiration blank for shelf-stable goods.");
        helper.setFont(helper.getFont().deriveFont(Font.ITALIC, helper.getFont().getSize2D() - 1));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(helper, gbc);

        content.add(formPanel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> {
            result = null;
            setVisible(false);
        });
        JButton save = new JButton("Save");
        save.addActionListener(e -> onSave());
        buttons.add(cancel);
        buttons.add(save);

        content.add(buttons, BorderLayout.SOUTH);
        setContentPane(content);
        getRootPane().setDefaultButton(save);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String label, JComponent component) {
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
        gbc.gridy++;
    }

    private void onSave() {
        String name = nameField.getText().strip();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String category = categoryField.getText().strip();
        String unit = unitField.getText().strip();
        int quantity = (Integer) quantitySpinner.getValue();
        double price = ((Number) priceSpinner.getValue()).doubleValue();
        LocalDate expiration = null;
        String expirationText = expirationField.getText().strip();
        if (!expirationText.isEmpty()) {
            try {
                expiration = LocalDate.parse(expirationText);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Expiration must be yyyy-MM-dd", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        result = new FormData(name, category, quantity, unit, price, expiration);
        setVisible(false);
    }

    public FormData showDialog(InventoryItem prefill) {
        if (prefill != null) {
            nameField.setText(prefill.getName());
            categoryField.setText(prefill.getCategory());
            quantitySpinner.setValue(prefill.getQuantity());
            unitField.setText(prefill.getUnit());
            priceSpinner.setValue(prefill.getPrice());
            expirationField.setText(prefill.getExpirationDate() == null ? "" : prefill.getExpirationDate().toString());
        } else {
            nameField.setText("");
            categoryField.setText("");
            quantitySpinner.setValue(1);
            unitField.setText("");
            priceSpinner.setValue(0.0);
            expirationField.setText("");
        }
        result = null;
        setVisible(true);
        return result;
    }

    public static final class FormData {
        private final String name;
        private final String category;
        private final int quantity;
        private final String unit;
        private final double price;
        private final LocalDate expiration;

        public FormData(String name, String category, int quantity, String unit, double price, LocalDate expiration) {
            this.name = name;
            this.category = category;
            this.quantity = quantity;
            this.unit = unit;
            this.price = price;
            this.expiration = expiration;
        }

        public String name() {
            return name;
        }

        public String category() {
            return category;
        }

        public int quantity() {
            return quantity;
        }

        public String unit() {
            return unit;
        }

        public double price() {
            return price;
        }

        public LocalDate expiration() {
            return expiration;
        }
    }
}
