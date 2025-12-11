package core;

public class Node {
    public final Invocation invoc;
    public final Consensus<Node> decideNext;
    public volatile Node next; 
    public volatile int seq; 

    public Node(Invocation invoc) {
        this.invoc = invoc;
        this.decideNext = new Consensus<>();
        this.seq = 0; // 0 means not yet part of the log
        this.next = null;
    }

    // Helper to find the node with the highest sequence number from an array
    public static Node max(Node[] heads) {
        Node maxNode = heads[0];
        for (Node n : heads) {
            if (n != null && n.seq > maxNode.seq) {
                maxNode = n;
            }
        }
        return maxNode;
    }
}