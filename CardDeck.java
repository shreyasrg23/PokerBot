import java.util.ArrayList;
import java.util.Collections;
public class CardDeck {
    ArrayList<Card> cards = new ArrayList<>();
    char[] suits = {'S', 'H', 'D', 'C'};
    
    public CardDeck() {
        for (int i = 2; i <= 14; i++) {
            for (int j = 0; j < suits.length; j++) {
                cards.add(new Card(suits[j], i));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card dealCard() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Deck is empty");
        }
        return cards.remove(0);
    }
}