import java.util.ArrayList;
import java.util.List;

// ==========================================
// 1. The Shared Interface
// ==========================================
interface ITaskNode {
    boolean isComplete();
}

// ==========================================
// 2. The Leaf Node (Single Task)
// ==========================================
class SubTask implements ITaskNode {
    private boolean status;
    public SubTask(boolean status) { this.status = status; }
    
    public void setStatus(boolean status) { this.status = status; }
    public boolean isComplete() { return status; }
}

// ==========================================
// 3. The Container Node (Holds Children)
// ==========================================
class Epic implements ITaskNode {
    private List<ITaskNode> children = new ArrayList<>();

    public void addTask(ITaskNode task) { children.add(task); }

    // Recursively evaluates all children
    public boolean isComplete() {
        if (children.isEmpty()) return false;
        
        for (ITaskNode child : children) {
            if (!child.isComplete()) {
                return false; 
            }
        }
        return true; 
    }
}

// ==========================================
// 4. Main Execution
// ==========================================
public class Main {
    public static void main(String[] args) {
        SubTask apiTask = new SubTask(true);
        SubTask dbTask = new SubTask(false);
        
        Epic backendEpic = new Epic();
        backendEpic.addTask(apiTask);
        backendEpic.addTask(dbTask);
        
        System.out.println("Epic Complete? " + backendEpic.isComplete()); // False
        
        dbTask.setStatus(true); 
        System.out.println("Epic Complete? " + backendEpic.isComplete()); // True
    }
}
