package objects;
import core.*;
import java.util.Stack;

public class SeqStack implements SeqObject {
    private Stack<Integer> stack;

    public SeqStack() { this.stack = new Stack<>(); }
    private SeqStack(Stack<Integer> s) { 
        this.stack = new Stack<>();
        this.stack.addAll(s);
    }

    @Override
    public Object apply(Invocation invoc) {
        if (invoc.method.equals("push")) {
            stack.push((Integer) invoc.args[0]);
            return null;
        } else if (invoc.method.equals("pop")) {
            if (stack.isEmpty()) return -1;
            return stack.pop();
        }
        return null;
    }

    @Override
    public SeqObject deepCopy() {
        return new SeqStack(this.stack);
    }
}