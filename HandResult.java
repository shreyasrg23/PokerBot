import java.util.ArrayList;
public class HandResult {
    private int rank; 
    private String description;
    private ArrayList<Integer> tieBreakers; 

    public HandResult(int rank, String description, ArrayList<Integer> tieBreakers) {
        this.rank = rank;
        this.description = description;
        this.tieBreakers = tieBreakers;
    }

    public int getRank() {
        return rank;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<Integer> getTieBreakers() {
        return tieBreakers;
    }

    public static int compareHands(HandResult hand1, HandResult hand2) {
        if (hand1.getRank() > hand2.getRank()) {
            return 1;
        }
        if (hand2.getRank() > hand1.getRank()) {
            return -1;
        }

        ArrayList<Integer> tieBreakers1 = hand1.getTieBreakers();
        ArrayList<Integer> tieBreakers2 = hand2.getTieBreakers();

        for (int i = 0; i < tieBreakers1.size(); i++) {
            if (tieBreakers1.get(i) > tieBreakers2.get(i)) {
                return 1;
            }

            if (tieBreakers2.get(i) > tieBreakers1.get(i)) {
                return -1;
            }
        }
        return 0;
    }
}