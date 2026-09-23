import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

// ==========================================
// 1. Entities (The Vehicles)
// ==========================================
enum VehicleType { SUV, SEDAN }

interface IVehicle {
    String getId();
    VehicleType getType();
    boolean isAvailable();
    void book();
}

class Car implements IVehicle {
    private String id;
    private VehicleType type;
    private boolean isBooked;

    public Car(String id, VehicleType type) {
        this.id = id;
        this.type = type;
        this.isBooked = false;
    }

    public String getId() { return id; }
    public VehicleType getType() { return type; }
    public boolean isAvailable() { return !isBooked; }
    public void book() { this.isBooked = true; }
}

// ==========================================
// 2. The Location Interface & Concrete Locations
// ==========================================
interface IRentalLocation {
    IVehicle rentVehicle(VehicleType type);
}

class HydRentalLocation implements IRentalLocation {
    private List<IVehicle> fleet;
    private ReentrantLock lock;

    public HydRentalLocation(List<IVehicle> fleet) {
        this.fleet = fleet;
        this.lock = new ReentrantLock(); // INTERVIEW POINT: Thread safety per location
    }

    public IVehicle rentVehicle(VehicleType targetType) {
        lock.lock(); // Secure the critical section
        try {
            for (IVehicle vehicle : fleet) {
                if (vehicle.getType() == targetType && vehicle.isAvailable()) {
                    vehicle.book();
                    return vehicle; // Successfully booked
                }
            }
            return null; // No available cars of that type
        } finally {
            lock.unlock(); // Guarantee release
        }
    }
}

class BlrRentalLocation implements IRentalLocation {
    private List<IVehicle> fleet;
    private ReentrantLock lock;

    public BlrRentalLocation(List<IVehicle> fleet) {
        this.fleet = fleet;
        this.lock = new ReentrantLock();
    }

    public IVehicle rentVehicle(VehicleType targetType) {
        lock.lock();
        try {
            for (IVehicle vehicle : fleet) {
                if (vehicle.getType() == targetType && vehicle.isAvailable()) {
                    vehicle.book();
                    return vehicle;
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
    }
}

// ==========================================
// 3. The Facade (CarRentalService)
// ==========================================
class CarRentalService {
    // Map acts as a router to the correct location
    private Map<String, IRentalLocation> locations = new HashMap<>();

    public void addLocation(String city, IRentalLocation location) {
        locations.put(city, location);
    }

    // Facade method hides all the mapping and locking logic from the user
    public void requestVehicle(String city, VehicleType type) {
        IRentalLocation location = locations.get(city);
        
        if (location == null) {
            System.out.println("Error: We do not operate in " + city);
            return;
        }

        IVehicle rentedCar = location.rentVehicle(type);
        
        if (rentedCar != null) {
            System.out.println("Success! You rented " + rentedCar.getType() + " (" + rentedCar.getId() + ") in " + city);
        } else {
            System.out.println("Failed: No " + type + " available in " + city);
        }
    }
}

// ==========================================
// 4. Main Execution
// ==========================================
public class Main {
    public static void main(String[] args) {
        // Setup Hyderabad Inventory
        List<IVehicle> hydFleet = new ArrayList<>();
        hydFleet.add(new Car("TS-09-1234", VehicleType.SUV));
        IRentalLocation hydLocation = new HydRentalLocation(hydFleet);

        // Setup Bangalore Inventory
        List<IVehicle> blrFleet = new ArrayList<>();
        blrFleet.add(new Car("KA-01-9999", VehicleType.SEDAN));
        IRentalLocation blrLocation = new BlrRentalLocation(blrFleet);

        // Setup the Central Service (Facade)
        CarRentalService rentalService = new CarRentalService();
        rentalService.addLocation("Hyderabad", hydLocation);
        rentalService.addLocation("Bangalore", blrLocation);

        // Users making requests
        rentalService.requestVehicle("Hyderabad", VehicleType.SUV);   // Success
        rentalService.requestVehicle("Hyderabad", VehicleType.SUV);   // Fails safely (already booked)
        rentalService.requestVehicle("Bangalore", VehicleType.SEDAN); // Success
    }
}
