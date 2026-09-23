import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

// ==========================================
// 1. Entities
// ==========================================
class Seat {
    private String seatId;
    private boolean isBooked;

    public Seat(String seatId) { 
        this.seatId = seatId;
        this.isBooked = false; 
    }
    public String getId() { return seatId; }
    public boolean isAvailable() { return !isBooked; }
    public void book() { this.isBooked = true; }
}

class Screening {
    private List<Seat> seats;
    public Screening(List<Seat> seats) { this.seats = seats; }
    public List<Seat> getSeats() { return seats; }
}

// ==========================================
// 2. Central Manager with Locks
// ==========================================
class BookingSystem {
    private ReentrantLock lock;

    public BookingSystem() {
        this.lock = new ReentrantLock();
    }

    public boolean reserveSeat(Screening screening, String targetSeatId) {
        lock.lock(); // Secure the critical section
        try {
            for (Seat seat : screening.getSeats()) {
                if (seat.getId().equals(targetSeatId) && seat.isAvailable()) {
                    seat.book();
                    System.out.println("Successfully booked: " + targetSeatId);
                    return true;
                }
            }
            System.out.println("Failed: Seat taken.");
            return false; 
        } finally {
            lock.unlock(); // Guarantee release
        }
    }
}

// ==========================================
// 3. Main Execution
// ==========================================
public class Main {
    public static void main(String[] args) {
        List<Seat> seats = new ArrayList<>();
        seats.add(new Seat("A1"));
        
        Screening movie = new Screening(seats);
        BookingSystem system = new BookingSystem();
        
        system.reserveSeat(movie, "A1"); // First user gets it
        system.reserveSeat(movie, "A1"); // Second user fails safely
    }
}
