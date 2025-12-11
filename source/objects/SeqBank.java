package objects;
import core.*;
import java.util.HashMap;
import java.util.Map;

public class SeqBank implements SeqObject {
    private Map<String, Integer> accounts;

    public SeqBank() { this.accounts = new HashMap<>(); }
    // Private constructor for deep copies
    private SeqBank(Map<String, Integer> accs) { this.accounts = new HashMap<>(accs); }

    @Override
    public Object apply(Invocation invoc) {
        String method = invoc.method;
        Object[] args = invoc.args;

        if (method.equals("deposit")) {
            String user = (String) args[0];
            int amount = (Integer) args[1];
            accounts.put(user, accounts.getOrDefault(user, 0) + amount);
            return "Deposited " + amount;
        } 
        else if (method.equals("transfer")) {
            // This would be HARD with locks (deadlock risk), but easy here!
            String from = (String) args[0];
            String to = (String) args[1];
            int amount = (Integer) args[2];

            int balFrom = accounts.getOrDefault(from, 0);
            if (balFrom < amount) return "Insufficient Funds";

            accounts.put(from, balFrom - amount);
            accounts.put(to, accounts.getOrDefault(to, 0) + amount);
            return "Transferred " + amount;
        }
        else if (method.equals("balance")) {
            return accounts.getOrDefault((String) args[0], 0);
        }
        return null;
    }

    @Override
    public SeqObject deepCopy() {
        return new SeqBank(this.accounts);
    }
}