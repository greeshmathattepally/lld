import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// ==========================================
// 1. The Shared Interface
// ==========================================
interface IRateLimiter {
    boolean grantAccess();
}

// ==========================================
// 2. The Core Engine (Token Bucket)
// ==========================================
class TokenBucketRateLimiter implements IRateLimiter {
    private final long maxCapacity;
    private final long refillRatePerSecond;
    
    private double currentTokens;
    private long lastRefillTimestamp;

    public TokenBucketRateLimiter(long maxCapacity, long refillRatePerSecond) {
        this.maxCapacity = maxCapacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.currentTokens = maxCapacity; // Bucket starts full
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    // INTERVIEW POINT: synchronized prevents two threads from stealing the exact same token
    public synchronized boolean grantAccess() {
        refillTokens();
        
        if (currentTokens >= 1.0) {
            currentTokens -= 1.0; // Consume 1 token
            return true;          // Allow request
        }
        return false;             // Drop request (Rate Limited)
    }

    // INTERVIEW POINT: We don't use a background thread to refill. 
    // We calculate refill math dynamically exactly at the moment a request arrives.
    private void refillTokens() {
        long now = System.currentTimeMillis();
        long elapsedTimeInMillis = now - lastRefillTimestamp;
        
        // Calculate how many tokens generate in that elapsed time
        double tokensToAdd = (elapsedTimeInMillis / 1000.0) * refillRatePerSecond;

        if (tokensToAdd > 0) {
            // Add tokens, but never exceed max capacity
            currentTokens = Math.min(currentTokens + tokensToAdd, maxCapacity);
            lastRefillTimestamp = now;
        }
    }
}

// ==========================================
// 3. Central Manager (Handles Multiple Users)
// ==========================================
class RateLimiterManager {
    // ConcurrentHashMap safely handles multiple threads creating users
    private Map<String, IRateLimiter> userLimits = new ConcurrentHashMap<>();

    public boolean isAllowed(String userId) {
        // If user doesn't exist, give them a bucket (e.g., max 2 tokens, refills 1 per sec)
        userLimits.putIfAbsent(userId, new TokenBucketRateLimiter(2, 1));
        
        return userLimits.get(userId).grantAccess();
    }
}

// ==========================================
// 4. Main Execution
// ==========================================
public class Main {
    public static void main(String[] args) throws InterruptedException {
        RateLimiterManager apiGateway = new RateLimiterManager();
        String user = "User_Alice";

        // Alice's bucket holds max 2 tokens.
        System.out.println("Req 1: " + apiGateway.isAllowed(user)); // true
        System.out.println("Req 2: " + apiGateway.isAllowed(user)); // true
        
        // Bucket is now empty!
        System.out.println("Req 3: " + apiGateway.isAllowed(user)); // false (Rate Limited)
        
        // Wait for 1 second so the bucket refills by 1 token...
        System.out.println("Waiting 1 second for refill...");
        Thread.sleep(1000); 
        
        // Should succeed now
        System.out.println("Req 4: " + apiGateway.isAllowed(user)); // true
    }
}
