package constructions;

import core.*;

public class LockFreeUniversal implements Universal {
    private final Node[] head;
    private final Node tail;
    private final ThreadLocal<Integer> threadID;

    public LockFreeUniversal(int n) {
        tail = new Node(null); // Sentinel
        tail.seq = 1;
        head = new Node[n];
        for (int i = 0; i < n; i++) {
            head[i] = tail;
        }
        // Simple mechanism to assign IDs 0 to n-1 to threads
        threadID = ThreadLocal.withInitial(() -> (int) (Thread.currentThread().getId() % n));
    }

    @Override
    public Object apply(Invocation invoc) {
        int i = threadID.get();
        Node prefer = new Node(invoc);

        while (prefer.seq == 0) {
            Node before = Node.max(head);
            Node after = before.decideNext.decide(prefer);
            before.next = after;
            after.seq = before.seq + 1;
            head[i] = after; // Update my view of the head
        }

        // Compute response by replaying history
        SeqObject myObject = getFreshObject(); // You must implement a factory or pass generic
        Node current = tail.next;
        
        // Skip ahead to our operation
        while (current != prefer) {
            myObject.apply(current.invoc);
            current = current.next;
        }
        return myObject.apply(current.invoc);
    }

    // In a real generic impl, this would be passed in constructor
    protected SeqObject getFreshObject() {
        // Placeholder: See Benchmark logic for injection
        throw new UnsupportedOperationException("Must override getFreshObject");
    }

    public int getLogLength() {
        int count = 0;
        Node current = tail.next; // Start after sentinel
        while (current != null) {
            count++;
            current = current.next;
        }
        return count;
    }
}