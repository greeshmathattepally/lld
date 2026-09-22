import java.util.ArrayList;
import java.util.List;

interface IFileSystemNode {
    String getName();
    int getSize();
    List<IFileSystemNode> search(String keyword);
}

class File implements IFileSystemNode {
    private final String name;
    private final int size;

    public File(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public String getName() { return name; }
    public int getSize() { return size; }

    public List<IFileSystemNode> search(String keyword) {
        List<IFileSystemNode> result = new ArrayList<>();
        if (name.contains(keyword)) result.add(this);
        return result;
    }
}

class Directory implements IFileSystemNode {
    private final String name;
    private final List<IFileSystemNode> children;

    public Directory(String name) {
        this.name = name;
        this.children = new ArrayList<>();
    }

    public synchronized void addNode(IFileSystemNode node) {
        children.add(node);
    }

    public String getName() { return name; }

    public int getSize() {
        int totalSize = 0;
        for (IFileSystemNode child : children) {
            totalSize += child.getSize(); 
        }
        return totalSize;
    }

    public List<IFileSystemNode> search(String keyword) {
        List<IFileSystemNode> results = new ArrayList<>();
        if (name.contains(keyword)) results.add(this);
        for (IFileSystemNode child : children) {
            results.addAll(child.search(keyword));
        }
        return results;
    }
}

public class Main {
    public static void main(String[] args) {
        Directory root = new Directory("root");
        Directory documentsFolder = new Directory("Documents");
        Directory workFolder = new Directory("Work");
        
        File resume = new File("resume.pdf", 500);
        File photo = new File("headshot.png", 2000);
        File report = new File("annual_report.pdf", 1000);

        // Build the tree
        documentsFolder.addNode(resume);
        documentsFolder.addNode(photo);
        workFolder.addNode(report);
        
        root.addNode(documentsFolder);
        root.addNode(workFolder);

        System.out.println("Total Root Size: " + root.getSize() + " bytes"); 

        System.out.println("Searching for 'report':");
        List<IFileSystemNode> searchResults = root.search("report");
        for (IFileSystemNode node : searchResults) {
            System.out.println("Found: " + node.getName());
        }
    }
}
