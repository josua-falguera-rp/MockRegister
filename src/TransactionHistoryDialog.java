// src/TransactionHistoryDialog.java

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Modern Transaction History Dialog matching the Transaction Panel design.
 */
public class TransactionHistoryDialog extends JDialog {

    // Modern Color Scheme (matching TransactionPanel)
    private static final Color PRIMARY_BG = new Color(245, 247, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color ACCENT_GREEN = new Color(16, 185, 129);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);
    private static final Color ACCENT_RED = new Color(239, 68, 68);
    private static final Color ACCENT_ORANGE = new Color(251, 146, 60);
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color TABLE_HEADER = new Color(243, 244, 246);

    private final DecimalFormat df = new DecimalFormat("#,##0.00");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    private JTable historyTable;
    private DefaultTableModel tableModel;

    public TransactionHistoryDialog(Frame parent, List<Map<String, Object>> history) {
        super(parent, "Transaction History", true);

        setSize(1000, 600);
        setLocationRelativeTo(parent);

        initializeUI(history);
    }

    private void initializeUI(List<Map<String, Object>> history) {
        setLayout(new BorderLayout(20, 20));
        getContentPane().setBackground(PRIMARY_BG);

        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createTablePanel(history), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    // ==================== Header Panel ====================

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(PRIMARY_BG);

        // Title
        JLabel titleLabel = new JLabel("📋 Transaction History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);

        // Subtitle
        JLabel subtitleLabel = new JLabel("View all past transactions and their status");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);

        JPanel textPanel = new JPanel(new BorderLayout(5, 5));
        textPanel.setBackground(PRIMARY_BG);
        textPanel.add(titleLabel, BorderLayout.NORTH);
        textPanel.add(subtitleLabel, BorderLayout.CENTER);

        panel.add(textPanel, BorderLayout.CENTER);

        return panel;
    }

    // ==================== Table Panel ====================

    private JPanel createTablePanel(List<Map<String, Object>> history) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Create table model
        String[] columns = {"Transaction ID", "Date & Time", "Total Amount", "Payment Type", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Populate table
        for (Map<String, Object> trans : history) {
            Object[] row = new Object[5];
            row[0] = "#" + trans.get("id");
            row[1] = dateFormat.format(trans.get("date"));
            row[2] = "$" + df.format(trans.get("total"));
            row[3] = trans.get("payment_type") != null ? trans.get("payment_type") : "-";

            // Status with styling info
            String status;
            if ((Boolean) trans.get("is_voided")) {
                status = "VOIDED";
            } else if ((Boolean) trans.get("is_suspended") && !(Boolean) trans.get("is_resumed")) {
                status = "SUSPENDED";
            } else if ((Boolean) trans.get("is_completed")) {
                status = "COMPLETED";
            } else {
                status = "IN PROGRESS";
            }
            row[4] = status;

            tableModel.addRow(row);
        }

        // Create styled table
        historyTable = createStyledTable();

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getViewport().setBackground(CARD_BG);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JTable createStyledTable() {
        JTable table = new JTable(tableModel);

        // Modern table styling (matching TransactionPanel)
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(45);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(10, 0));
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setBackground(CARD_BG);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(TABLE_HEADER);
        header.setForeground(TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(header.getWidth(), 50));

        // Custom cell renderer for status column with badges
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    c.setBackground(CARD_BG);
                }

                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                // Style status column (column 4)
                if (column == 4 && value != null) {
                    String status = value.toString();
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    setHorizontalAlignment(SwingConstants.CENTER);

                    if (!isSelected) {
                        switch (status) {
                            case "COMPLETED":
                                setForeground(ACCENT_GREEN);
                                break;
                            case "VOIDED":
                                setForeground(ACCENT_RED);
                                break;
                            case "SUSPENDED":
                                setForeground(ACCENT_ORANGE);
                                break;
                            case "IN PROGRESS":
                                setForeground(ACCENT_BLUE);
                                break;
                            default:
                                setForeground(TEXT_SECONDARY);
                        }
                    }
                } else {
                    setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    setHorizontalAlignment(SwingConstants.LEFT);
                    if (!isSelected) {
                        setForeground(TEXT_PRIMARY);
                    }
                }

                return c;
            }
        });

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(120);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(200);  // Date
        table.getColumnModel().getColumn(2).setPreferredWidth(130);  // Total
        table.getColumnModel().getColumn(3).setPreferredWidth(130);  // Payment
        table.getColumnModel().getColumn(4).setPreferredWidth(130);  // Status

        return table;
    }

    // ==================== Footer Panel ====================

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panel.setBackground(PRIMARY_BG);

        JButton closeButton = createStyledButton("Close", ACCENT_BLUE);
        closeButton.addActionListener(e -> dispose());

        panel.add(closeButton);

        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 45));

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
            }
        });

        return btn;
    }
}