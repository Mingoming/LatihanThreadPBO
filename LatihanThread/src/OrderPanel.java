import java.awt.*;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class OrderPanel extends JPanel {
    private final OrderDao orderDao;
    private final ExecutorService ioPool = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "IOPool");
        t.setDaemon(true);
        return t;
    });

    private final JTextField nameField = new JTextField();

    private static final String[] MENU_ITEMS = new String[] {
            "Nasi Goreng",
            "Sate Ayam",
            "Rendang",
            "Soto Ayam",
            "Gado-Gado",
            "Bakso",
            "Mie Ayam",
            "Ayam Penyet",
            "Nasi Uduk",
            "Pempek"
    };
    private final JComboBox<String> itemCombo = new JComboBox<>(MENU_ITEMS);

    private final JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final JButton addBtn = new JButton("Tambah Pesanan");

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Customer", "Item", "Qty", "Status", "CreatedAt(ms)"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);

    public OrderPanel(OrderDao orderDao) {
        this.orderDao = orderDao;
        setOpaque(true);
        setBackground(Color.DARK_GRAY);
        setLayout(new BorderLayout(12,12));
        buildNorth();
        buildCenter();
        wireEvents();
    }

    private void buildNorth() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx=0; c.gridy=0; form.add(lbl("Nama Pelanggan"), c);
        c.gridx=1; c.gridy=0; nameField.setColumns(16); form.add(nameField, c);

        c.gridx=0; c.gridy=1; form.add(lbl("Menu / Item"), c);
        c.gridx=1; c.gridy=1;
        itemCombo.setSelectedIndex(0);
        itemCombo.setFocusable(false);
        form.add(itemCombo, c);

        c.gridx=0; c.gridy=2; form.add(lbl("Qty"), c);
        c.gridx=1; c.gridy=2; form.add(qtySpinner, c);

        c.gridx=1; c.gridy=3; addBtn.setFocusPainted(false); form.add(addBtn, c);

        add(form, BorderLayout.NORTH);
    }

    private JLabel lbl(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(Color.WHITE);
        return l;
    }

    private void buildCenter() {
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void wireEvents() {
        addBtn.addActionListener(e -> onAddOrder());
    }

    private void onAddOrder() {
        String name = nameField.getText().trim();
        String item = (String) itemCombo.getSelectedItem();
        int qty = (Integer) qtySpinner.getValue();

        if (name.isEmpty() || item == null || item.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama dan Item wajib diisi",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        addBtn.setEnabled(false);
        ioPool.submit(() -> {
            try {
                Order o = new Order();
                o.setCustomerName(name);
                o.setItem(item);
                o.setQuantity(qty);
                o.setStatus(OrderStatus.NEW);
                o.setCreatedAtEpoch(System.currentTimeMillis());
                orderDao.insert(o);

                SwingUtilities.invokeLater(() -> {
                    nameField.setText("");
                    itemCombo.setSelectedIndex(0);
                    qtySpinner.setValue(1);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE)
                );
            } finally {
                SwingUtilities.invokeLater(() -> addBtn.setEnabled(true));
            }
        });
    }

    public void setData(List<Order> list) {
        model.setRowCount(0);
        for (Order o : list) {
            model.addRow(new Object[]{
                    o.getId(), o.getCustomerName(), o.getItem(),
                    o.getQuantity(), o.getStatus().name(), o.getCreatedAtEpoch()
            });
        }
    }

    public void shutdown() {
        ioPool.shutdownNow();
    }
}
