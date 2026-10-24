import java.util.ArrayList;
public class SidePot {
    private int amount; 
    private ArrayList<Player> eligiblePlayers;
    private boolean refund;

    public SidePot(int amount, ArrayList<Player> eligiblePlayers) {
        this.amount = amount;
        this.eligiblePlayers = new ArrayList<>(eligiblePlayers);
        this.refund = false;
    }

    public SidePot(int amount, ArrayList<Player> eligiblePlayers, boolean refund) {
        this.amount = amount;
        this.eligiblePlayers = new ArrayList<>(eligiblePlayers);
        this.refund = refund;
    }

    public int getAmount() {
        return amount;
    }

    public ArrayList<Player> getEligiblePlayers() {
        return new ArrayList<>(eligiblePlayers);
    }

    public boolean isRefund() {
        return refund;
    }
}