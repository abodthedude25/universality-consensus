package core;

public interface SeqObject {
    // Applies the invocation and returns the result (Response)
    Object apply(Invocation invoc);
    
    // Crucial: Must allow creating a fresh copy of the initial state
    SeqObject deepCopy();
}