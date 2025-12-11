package core;

import java.util.concurrent.atomic.AtomicReference;

public class Consensus<T> {
    private final AtomicReference<T> decision = new AtomicReference<>(null);

    // Propose a value. Returns the decided value (yours or someone else's).
    public T decide(T value) {
        decision.compareAndSet(null, value);
        return decision.get();
    }
    
    public boolean isDecided() {
        return decision.get() != null;
    }
}