public class Player {
    private Card card1;
    private Card card2;
    private int chips;
    private boolean folded = false;

    public Player(Card card1, Card card2, int chips) {
        this.card1 = card1;
        this.card2 = card2;
        this.chips = chips;
    }

    public Card getCard1() {
        return card1;
    }

    public Card getCard2() {
        return card2;
    }

    public int getChips() {
        return chips;
    }

    public void addChips(int amount) {
        if (amount <=0) {
            throw new IllegalArgumentException("Invalid amount");
        }
        chips += amount;
    }

    public void removeChips(int amount) {
        if (amount <= 0 || amount > chips) {
            throw new IllegalArgumentException("Invalid amount");
        }
        chips -= amount;
    }

    public boolean isFolded() {
        return folded;
    }

    public void fold() {
        folded = true;
    }

    public void printHand() {
        System.out.println(card1.getCardName());
        System.out.println(card2.getCardName());
    }
}