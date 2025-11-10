public class Order {
    private long id;
    private String customerName;
    private String item;
    private int quantity;
    private OrderStatus status;
    private long createdAtEpoch;

    public Order() {}

    public Order(long id, String customerName, String item, int quantity,
                 OrderStatus status, long createdAtEpoch) {
        this.id = id;
        this.customerName = customerName;
        this.item = item;
        this.quantity = quantity;
        this.status = status;
        this.createdAtEpoch = createdAtEpoch;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public long getCreatedAtEpoch() { return createdAtEpoch; }
    public void setCreatedAtEpoch(long createdAtEpoch) { this.createdAtEpoch = createdAtEpoch; }
}
