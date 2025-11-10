import java.sql.*;
import java.util.*;

public class OrderDao {

    public long insert(Order o) {
        String sql = "INSERT INTO orders(customer_name,item,quantity,status,created_at_epoch) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = DB.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, o.getCustomerName());
            ps.setString(2, o.getItem());
            ps.setInt(3, o.getQuantity());
            ps.setString(4, o.getStatus().name());
            ps.setLong(5, o.getCreatedAtEpoch());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    DB.get().commit();
                    return id;
                }
            }
            DB.get().commit();
            return -1;
        } catch (SQLException e) {
            try { DB.get().rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException(e);
        }
    }

    public java.util.List<Order> findAll() {
        String sql = "SELECT id, customer_name, item, quantity, status, created_at_epoch FROM orders ORDER BY id DESC";
        try (PreparedStatement ps = DB.get().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            java.util.List<Order> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Order> findNextNew() {
        String sql = "SELECT id, customer_name, item, quantity, status, created_at_epoch " +
                     "FROM orders WHERE status='NEW' ORDER BY created_at_epoch ASC LIMIT 1";
        try (PreparedStatement ps = DB.get().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return Optional.of(map(rs));
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateStatus(long id, OrderStatus s) {
        String sql = "UPDATE orders SET status=? WHERE id=?";
        try (PreparedStatement ps = DB.get().prepareStatement(sql)) {
            ps.setString(1, s.name());
            ps.setLong(2, id);
            ps.executeUpdate();
            DB.get().commit();
        } catch (SQLException e) {
            try { DB.get().rollback(); } catch (SQLException ignored) {}
            throw new RuntimeException(e);
        }
    }

    private Order map(ResultSet rs) throws SQLException {
        return new Order(
                rs.getLong("id"),
                rs.getString("customer_name"),
                rs.getString("item"),
                rs.getInt("quantity"),
                OrderStatus.valueOf(rs.getString("status")),
                rs.getLong("created_at_epoch")
        );
    }
}
