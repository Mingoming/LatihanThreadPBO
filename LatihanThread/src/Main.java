import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            OrderDao orderDao = new OrderDao();
            OrderPanel panel = new OrderPanel(orderDao);

            KitchenService kitchen = new KitchenService(orderDao);
            RefreshService refresh = new RefreshService(
                    orderDao::findAll,
                    panel::setData,
                    2 
            );

            JFrame f = new JFrame("Cafe Order Tracker — (Sederhana, Tanpa Maven)");
            f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            f.setSize(900, 540);
            f.setLocationRelativeTo(null);
            f.setLayout(new BorderLayout());
            f.add(panel, BorderLayout.CENTER);

            kitchen.start();
            refresh.start();

            f.addWindowListener(new WindowAdapter() {
                @Override public void windowClosed(WindowEvent e) {
                    refresh.stop();
                    kitchen.stop();
                    panel.shutdown();
                    try { if (DB.get() != null) DB.get().close(); } catch (Exception ignored) {}
                }
            });

            f.setVisible(true);
        });
    }
}