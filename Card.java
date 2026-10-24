public class Card {
    private char cardType;
    private int cardValue;

    public Card(char cardType, int cardValue) {
        this.cardType = cardType;
        this.cardValue = cardValue;
    }

    public int getCardValue() {
        return cardValue;
    }

    public char getCardType() {
        return cardType;
    }

    public String getCardName() {
        String valueName;
        String suitName;

        if (cardValue == 11) {
            valueName = "Jack";
        }

        else if (cardValue == 12) {
            valueName = "Queen";
        }

        else if (cardValue == 13) {
            valueName = "King";
        }

        else if (cardValue == 14) {
            valueName = "Ace";
        }

        else {
            valueName = String.valueOf(cardValue);
        }

        if (cardType == 'S') {
            suitName = "Spades";
        }

        else if (cardType == 'H') {
            suitName = "Hearts";
        }

        else if (cardType == 'D') {
            suitName = "Diamonds";
        }

        else {
            suitName = "Clubs";
        }

        return (valueName + " of " + suitName);
    }

    public static String valueIntoName(int value) {
        if (value == 11) {
            return "Jack";
        }

        else if (value == 12) {
            return "Queen";
        }

        else if (value == 13) {
            return "King";
        }

        else if (value == 14) {
            return "Ace";
        }

        else {
            return (String.valueOf(value));
        }
    }
}
