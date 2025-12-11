package constructions;

import core.*;

public class WaitFreeUniversal implements Universal {
    private final Node[] head;
    private final Node[] announce;
    private final Node tail;
    private final int n;
    private final ThreadLocal<Integer> threadID;

    public WaitFreeUniversal(int n) {
        this.n = n;
        tail = new Node(null);
        tail.seq = 1;
        head = new Node[n];
        announce = new Node[n];
        for (int i = 0; i < n; i++) {
            head[i] = tail;
            announce[i] = tail;
        }
        threadID = ThreadLocal.withInitial(() -> (int) (Thread.currentThread().getId() % n));
    }

    @Override
    public Object apply(Invocation invoc) {
        int i = threadID.get();
        Node myNode = new Node(invoc);
        announce[i] = myNode; // Announce intent [cite: 64]
        
        head[i] = Node.max(head); // Find local max

        while (announce[i].seq == 0) {
            Node before = head[i];
            Node help = announce[(before.seq + 1) % n]; // Cyclic helping strategy [cite: 69]
            Node prefer;
            
            if (help.seq == 0) // Does neighbor need help?
                prefer = help;
            else
                prefer = announce[i]; // No help needed, propose mine

            Node after = before.decideNext.decide(prefer);
            before.next = after;
            after.seq = before.seq + 1;
            head[i] = after;
        }

        // Replay history
        SeqObject myObject = getFreshObject();
        Node current = tail.next;
        while (current != announce[i]) {
            myObject.apply(current.invoc);
            current = current.next;
        }
        
        // Cleanup announce
        announce[i] = tail; // Reset to sentinel or dummy
        return myObject.apply(current.invoc);
    }

    protected SeqObject getFreshObject() {
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