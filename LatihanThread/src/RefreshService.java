import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.SwingUtilities;

public class RefreshService {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "RefreshService");
        t.setDaemon(true);
        return t;
    });

    private ScheduledFuture<?> task;
    private final Supplier<List<Order>> loader;
    private final Consumer<List<Order>> onData;
    private final int periodSeconds;

    public RefreshService(Supplier<List<Order>> loader, Consumer<List<Order>> onData, int periodSeconds) {
        this.loader = loader;
        this.onData = onData;
        this.periodSeconds = periodSeconds;
    }

    public void start() {
        if (task != null && !task.isCancelled()) return;
        task = scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Order> data = loader.get();                   // off-EDT
                SwingUtilities.invokeLater(() -> onData.accept(data)); // back to EDT
            } catch (Exception e) {
                System.err.println("[RefreshService] " + e.getMessage());
            }
        }, 0, periodSeconds, TimeUnit.SECONDS);
    }

    public void stop() {
        if (task != null) task.cancel(true);
        scheduler.shutdownNow();
    }
}
