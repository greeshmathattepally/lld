// ==========================================
// 1. External Service Interface & Mock Class
// ==========================================
interface IBankService {
    boolean validatePin(String cardNumber, int pin);
    boolean withdrawFunds(String cardNumber, int amount);
}

class StandardBankService implements IBankService {
    public boolean validatePin(String card, int pin) { 
        return pin == 1234; 
    }
    public boolean withdrawFunds(String card, int amount) { 
        return amount <= 5000; 
    }
}

// ==========================================
// 2. The ATM State Interface
// ==========================================
interface IATMState {
    void insertCard(String card);
    void enterPin(int pin);
    void requestCash(int amount);
}

// ==========================================
// 3. Concrete States
// ==========================================
class IdleState implements IATMState {
    private ATM machine;
    public IdleState(ATM machine) { this.machine = machine; }
    
    public void insertCard(String card) {
        System.out.println("Card inserted.");
        machine.setCardNumber(card);
        machine.setState(machine.getHasCardState());
    }
    public void enterPin(int pin) { System.out.println("Error: Insert card first."); }
    public void requestCash(int amount) { System.out.println("Error: Insert card first."); }
}

class HasCardState implements IATMState {
    private ATM machine;
    public HasCardState(ATM machine) { this.machine = machine; }
    
    public void insertCard(String card) { System.out.println("Error: Card already in."); }
    public void enterPin(int pin) {
        // INTERVIEW POINT: Delegating validation to the external bank service
        if (machine.getBankService().validatePin(machine.getCardNumber(), pin)) {
            System.out.println("PIN Accepted.");
            machine.setState(machine.getAuthenticatedState());
        } else {
            System.out.println("Invalid PIN. Ejecting.");
            machine.setState(machine.getIdleState());
        }
    }
    public void requestCash(int amount) { System.out.println("Error: Enter PIN first."); }
}

class AuthenticatedState implements IATMState {
    private ATM machine;
    public AuthenticatedState(ATM machine) { this.machine = machine; }
    
    public void insertCard(String card) { System.out.println("Error: Card already in."); }
    public void enterPin(int pin) { System.out.println("Error: Already authenticated."); }
    public void requestCash(int amount) {
        // INTERVIEW POINT: Processing the actual transaction securely
        if (machine.getBankService().withdrawFunds(machine.getCardNumber(), amount)) {
            System.out.println("Dispensing ₹" + amount);
        } else {
            System.out.println("Insufficient funds.");
        }
        machine.setState(machine.getIdleState()); // Reset back to Idle after transaction
    }
}

// ==========================================
// 4. The ATM Context
// ==========================================
class ATM {
    private IATMState idleState = new IdleState(this);
    private IATMState hasCardState = new HasCardState(this);
    private IATMState authenticatedState = new AuthenticatedState(this);
    
    private IATMState currentState = idleState;
    
    private IBankService bankService; 
    private String currentCardNumber; 
    
    public ATM(IBankService bankService) { this.bankService = bankService; }
    
    public void setState(IATMState state) { this.currentState = state; }
    public IATMState getIdleState() { return idleState; }
    public IATMState getHasCardState() { return hasCardState; }
    public IATMState getAuthenticatedState() { return authenticatedState; }
    
    public void setCardNumber(String card) { this.currentCardNumber = card; }
    public String getCardNumber() { return currentCardNumber; }
    public IBankService getBankService() { return bankService; }
    
    // External inputs are blindly delegated to the current state
    public void insertCard(String card) { currentState.insertCard(card); }
    public void enterPin(int pin) { currentState.enterPin(pin); }
    public void requestCash(int amount) { currentState.requestCash(amount); }
}

// ==========================================
// 5. Main Execution
// ==========================================
public class Main {
    public static void main(String[] args) {
        // Inject the mock bank service into the ATM
        IBankService bank = new StandardBankService(); 
        ATM atm = new ATM(bank);
        
        // Simulating the user flow
        atm.insertCard("CARD-999");
        atm.enterPin(1234);
        atm.requestCash(2000); 
    }
}
