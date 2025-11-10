import java.util.Optional;
import java.util.concurrent.*;

public class KitchenService {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "KitchenService");
        t.setDaemon(true);
        return t;
    });
    private final OrderDao orderDao;
    private ScheduledFuture<?> task;

    public KitchenService(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    // Tiap 3 detik: NEW → IN_PROGRESS; jika tak ada NEW, IN_PROGRESS tertua → DONE
    public void start() {
        if (task != null && !task.isCancelled()) return;

        task = scheduler.scheduleAtFixedRate(() -> {
            try {
                Optional<Order> maybe = orderDao.findNextNew();
                if (maybe.isPresent()) {
                    orderDao.updateStatus(maybe.get().getId(), OrderStatus.IN_PROGRESS);
                    return;
                }
                orderDao.findAll().stream()
                        .filter(o -> o.getStatus() == OrderStatus.IN_PROGRESS)
                        .sorted((a, b) -> Long.compare(a.getCreatedAtEpoch(), b.getCreatedAtEpoch()))
                        .findFirst()
                        .ifPresent(o -> orderDao.updateStatus(o.getId(), OrderStatus.DONE));
            } catch (Exception e) {
                System.err.println("[KitchenService] " + e.getMessage());
            }
        }, 2, 3, TimeUnit.SECONDS);
    }

    public void stop() {
        if (task != null) task.cancel(true);
        scheduler.shutdownNow();
    }
}
