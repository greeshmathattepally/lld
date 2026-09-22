import java.util.ArrayList;
import java.util.List;

// ==========================================
// 1. ENUMS & CORE ENTITIES
// ==========================================
enum VehicleType { CAR, MOTORCYCLE }
enum SpotStatus { FREE, OCCUPIED }

abstract class Vehicle {
    private String licensePlate;
    private VehicleType type;

    public Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = licensePlate;
        this.type = type;
    }
    public VehicleType getType() { return type; }
    public String getLicensePlate() { return licensePlate; }
}

class Car extends Vehicle {
    public Car(String licensePlate) { super(licensePlate, VehicleType.CAR); }
}

class Motorcycle extends Vehicle {
    public Motorcycle(String licensePlate) { super(licensePlate, VehicleType.MOTORCYCLE); }
}

// INTERVIEW FLEX (SRP): "By giving ParkingSpot its own class, I adhere to the 
// Single Responsibility Principle. A spot only cares about its own state."
class ParkingSpot {
    private String spotId;
    private VehicleType typeSupported;
    private SpotStatus status;
    private Vehicle parkedVehicle;

    public ParkingSpot(String spotId, VehicleType typeSupported) {
        this.spotId = spotId;
        this.typeSupported = typeSupported;
        this.status = SpotStatus.FREE;
    }

    public boolean isAvailableFor(VehicleType type) {
        return status == SpotStatus.FREE && typeSupported == type;
    }

    public void parkVehicle(Vehicle vehicle) {
        this.parkedVehicle = vehicle;
        this.status = SpotStatus.OCCUPIED;
    }

    public void removeVehicle() {
        this.parkedVehicle = null;
        this.status = SpotStatus.FREE;
    }

    public String getSpotId() { return spotId; }
}

// RESTORED HIERARCHY: Useful if they ask for floor-specific display boards later.
class ParkingFloor {
    private int floorLevel;
    private List<ParkingSpot> spots;
    
    public ParkingFloor(int floorLevel, List<ParkingSpot> spots) {
        this.floorLevel = floorLevel;
        this.spots = spots;
    }
    
    public List<ParkingSpot> getSpots() { return spots; }
    public int getFloorLevel() { return floorLevel; }
}

class Ticket {
    private String ticketId;
    private ParkingSpot allocatedSpot;
    private long entryTimeMillis;

    public Ticket(String ticketId, ParkingSpot spot) {
        this.ticketId = ticketId;
        this.allocatedSpot = spot;
        this.entryTimeMillis = System.currentTimeMillis();
    }
    public ParkingSpot getAllocatedSpot() { return allocatedSpot; }
    public long getEntryTimeMillis() { return entryTimeMillis; }
}

// ==========================================
// 2. THE STRATEGY PATTERN (PRICING)
// ==========================================
// INTERVIEW FLEX (OCP): "I am using the Strategy Pattern here to protect the Open/Closed Principle. 
// If management adds 'Festival Pricing' tomorrow, I don't touch the core logic. 
// I just create a new class that implements IPricingStrategy."
interface IPricingStrategy {
    double calculatePrice(Ticket ticket);
}

class StandardHourlyPricing implements IPricingStrategy {
    public double calculatePrice(Ticket ticket) {
        long durationHours = (System.currentTimeMillis() - ticket.getEntryTimeMillis()) / 3600000;
        durationHours = durationHours == 0 ? 1 : durationHours; // 1 hour minimum
        
        if (ticket.getAllocatedSpot().isAvailableFor(VehicleType.CAR)) {
            return durationHours * 50.0; // ₹50/hr for cars
        } else {
            return durationHours * 20.0; // ₹20/hr for bikes
        }
    }
}

class WeekendPricing implements IPricingStrategy {
    public double calculatePrice(Ticket ticket) {
        return new StandardHourlyPricing().calculatePrice(ticket) * 2; // Double price
    }
}

// ==========================================
// 3. THE CONCURRENCY CONTROLLER (WITH FLOORS)
// ==========================================
class ParkingLot {
    private List<ParkingFloor> floors;

    public ParkingLot(List<ParkingFloor> floors) {
        this.floors = floors;
    }

    // CRITICAL SECTION: Multiple Entry Gates will call this simultaneously.
    // INTERVIEW FLEX: "I am using 'synchronized' here for whiteboard speed. 
    // It locks the entire ParkingLot so no two gates assign the same spot. 
    // In production, I would swap this for a ReentrantLock to enable timeouts."
    public synchronized ParkingSpot assignSpot(Vehicle vehicle) {
        // Nested loop to traverse floors, then spots
        for (ParkingFloor floor : floors) {
            for (ParkingSpot spot : floor.getSpots()) {
                if (spot.isAvailableFor(vehicle.getType())) {
                    spot.parkVehicle(vehicle);
                    return spot; 
                }
            }
        }
        return null; // Lot is completely Full
    }
}

// ==========================================
// 4. THE ENTRY / EXIT GATES
// ==========================================
class EntryGate {
    private String gateId;
    private ParkingLot lot;

    public EntryGate(String gateId, ParkingLot lot) {
        this.gateId = gateId;
        this.lot = lot;
    }

    public Ticket generateTicket(Vehicle vehicle) {
        ParkingSpot spot = lot.assignSpot(vehicle);
        if (spot == null) {
            System.out.println("Gate " + gateId + ": Sorry, Lot is Full.");
            return null;
        }
        String ticketId = "TKT-" + System.currentTimeMillis();
        System.out.println("Gate " + gateId + ": Spot " + spot.getSpotId() + " allocated.");
        return new Ticket(ticketId, spot);
    }
}

// INTERVIEW FLEX (Dependency Injection): "The ExitGate receives its Pricing Strategy via the constructor. 
// This allows Gate 1 to have standard pricing while Gate 2 has VIP pricing."
class ExitGate {
    private String gateId;
    private IPricingStrategy pricingStrategy;

    public ExitGate(String gateId, IPricingStrategy pricingStrategy) {
        this.gateId = gateId;
        this.pricingStrategy = pricingStrategy;
    }

    public void processExit(Ticket ticket) {
        double amount = pricingStrategy.calculatePrice(ticket);
        System.out.println("Gate " + gateId + ": Please pay ₹" + amount);
        
        ticket.getAllocatedSpot().removeVehicle();
        System.out.println("Spot " + ticket.getAllocatedSpot().getSpotId() + " is now free.");
    }
}

// ==========================================
// 5. THE MAIN EXECUTION (Proof of Concept)
// ==========================================
public class Main {
    public static void main(String[] args) {
        // 1. Setup Infrastructure (With Floors)
        List<ParkingSpot> floor1Spots = new ArrayList<>();
        floor1Spots.add(new ParkingSpot("A1", VehicleType.CAR));
        floor1Spots.add(new ParkingSpot("A2", VehicleType.MOTORCYCLE));
        
        List<ParkingFloor> floors = new ArrayList<>();
        floors.add(new ParkingFloor(1, floor1Spots)); // Adding Floor 1
        
        ParkingLot centralLot = new ParkingLot(floors);

        // 2. Setup Gates and Strategies
        EntryGate gate1 = new EntryGate("Gate-1", centralLot);
        EntryGate gate2 = new EntryGate("Gate-2", centralLot);
        
        ExitGate exitGate = new ExitGate("Exit-1", new StandardHourlyPricing());

        // 3. Simulate Concurrent Vehicles Arriving
        Vehicle myRenaultTriber = new Car("TS09-TRBR");
        Vehicle myBike = new Motorcycle("TS09-BIKE");
        Vehicle randomCar = new Car("TS09-9999");

        System.out.println("--- Arriving at Gates ---");
        Ticket ticket1 = gate1.generateTicket(myRenaultTriber); 
        Ticket ticket2 = gate2.generateTicket(myBike);
        
        // The lot only has 1 car spot. If a second car arrives, it is denied.
        Ticket ticket3 = gate1.generateTicket(randomCar); 

        System.out.println("\n--- Leaving the Lot ---");
        exitGate.processExit(ticket1);
    }
}
