import java.util.ArrayList;
import java.util.List;

// ==========================================
// 1. The Listener Interface
// ==========================================
interface ISubscriber {
    void update(String orderId, String status);
}

// ==========================================
// 2. Concrete Listeners
// ==========================================
class EmailService implements ISubscriber {
    public void update(String orderId, String status) {
        System.out.println("Email Sent for " + orderId + ": " + status);
    }
}

class WarehouseSystem implements ISubscriber {
    public void update(String orderId, String status) {
        System.out.println("Warehouse Notified for " + orderId);
    }
}

// ==========================================
// 3. The Central Publisher
// ==========================================
class OrderPublisher {
    private List<ISubscriber> subscribers = new ArrayList<>();

    public void subscribe(ISubscriber s) { subscribers.add(s); }

    // Decoupled triggering
    public void updateOrderStatus(String orderId, String newStatus) {
        System.out.println("\n[SYSTEM] Order " + orderId + " changed to: " + newStatus);
        for (ISubscriber s : subscribers) {
            s.update(orderId, newStatus);
        }
    }
}

// ==========================================
// 4. Main Execution
// ==========================================
public class Main {
    public static void main(String[] args) {
        OrderPublisher system = new OrderPublisher();
        
        system.subscribe(new EmailService());
        system.subscribe(new WarehouseSystem());

        system.updateOrderStatus("ORD-1", "PAYMENT_SUCCESS");
    }
}
