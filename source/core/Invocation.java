package core;

public class Invocation {
    public final String method;
    public final Object[] args;

    public Invocation(String method, Object[] args) {
        this.method = method;
        this.args = args;
    }
}