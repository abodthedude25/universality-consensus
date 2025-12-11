package objects;
import core.*;
import java.util.LinkedList;
import java.util.Queue;

public class SeqQueue implements SeqObject {
    private Queue<Integer> queue;

    public SeqQueue() { this.queue = new LinkedList<>(); }
    private SeqQueue(Queue<Integer> q) { this.queue = new LinkedList<>(q); }

    @Override
    public Object apply(Invocation invoc) {
        if (invoc.method.equals("enq")) {
            queue.add((Integer) invoc.args[0]);
            return null;
        } else if (invoc.method.equals("deq")) {
            if (queue.isEmpty()) return -1;
            return queue.remove();
        }
        return null;
    }

    @Override
    public SeqObject deepCopy() {
        return new SeqQueue(this.queue);
    }
}