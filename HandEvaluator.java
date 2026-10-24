
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class HandEvaluator {

    ArrayList<Card> evaluator = new ArrayList<>();

    public HandEvaluator(Player player, ArrayList<Card> communityCards) {
        evaluator.add(player.getCard1());
        evaluator.add(player.getCard2());
        evaluator.addAll(communityCards);
    }

    public Card isFlush() {
        Map<Character, Integer> suitMap = new HashMap<>();

        for (Card card : evaluator) {
            char suit = card.getCardType();

            if (!suitMap.containsKey(suit)) {
                suitMap.put(suit, 1);
            }
            else {
                suitMap.put(suit, suitMap.get(suit) + 1);
            }
        }

        char flushSuit = ' ';

        for (char suit : suitMap.keySet()) {
            if (suitMap.get(suit) >= 5) {
                flushSuit = suit;
            }
        }
        if (flushSuit == ' ') {
            return null;
        }

        Card highestCard = null;

        for (Card card : evaluator) {
            if (card.getCardType() == flushSuit) {
                if (highestCard == null || card.getCardValue() > highestCard.getCardValue()) {
                    highestCard = card;
                }
            }
        }
        return highestCard;
    }

    public int isStraight() {
        Set<Integer> valueSet = new HashSet<>();
        for (Card card : evaluator) {
            valueSet.add(card.getCardValue());
        }
        ArrayList<Integer> values = new ArrayList<>(valueSet);
        Collections.sort(values);

        int consecutive = 1;
        int highestCard = values.get(values.size()-1);

        for (int i = values.size() - 1; i > 0; i--) {
            if (values.get(i-1) == values.get(i) - 1) {
                consecutive++;

                if (consecutive == 5) {
                    return highestCard;
                }
            }
            else {
                consecutive = 1;
                highestCard = values.get(i-1);
            }
        }

        if (valueSet.contains(14) && valueSet.contains(2) && valueSet.contains(3) && valueSet.contains(4) && valueSet.contains(5)) {
            return 5;
        }
        return -1;
    }

    public int isStraight(ArrayList<Integer> values) {
        Set<Integer> valueSet = new HashSet<>(values);

        values = new ArrayList<>(valueSet);
        Collections.sort(values);

        int consecutive = 1;
        int highestCard = values.get(values.size() - 1);

        for (int i = values.size() - 1; i > 0; i--) {
            if (values.get(i - 1) == values.get(i) - 1) {
                consecutive++;

                if (consecutive == 5) {
                    return highestCard;
                }
            } else {
                consecutive = 1;
                highestCard = values.get(i - 1);
            }
        }

        if (valueSet.contains(14) && valueSet.contains(2) && valueSet.contains(3) && valueSet.contains(4) && valueSet.contains(5)) {
            return 5;
        }
        return -1;
    }
    
    public int isStraightFlush() {
        Map<Character, ArrayList<Integer>> suitMap = new HashMap<>();

        for (Card card : evaluator) {
            char suit = card.getCardType();

            if (!suitMap.containsKey(suit)) {
                suitMap.put(suit, new ArrayList<>());
            }
            suitMap.get(suit).add(card.getCardValue());
        }
        for (char suit : suitMap.keySet()) {
            if (suitMap.get(suit).size() >= 5) {
                return isStraight(suitMap.get(suit));
            }
        }
        return -1;
    }

    public HandResult evaluateHand() {
        Map<Integer, Integer> cardMap = new HashMap<>();
        for (int i = 0; i < evaluator.size(); i++) {
            Card currentCard = evaluator.get(i);
            if (!cardMap.containsKey(currentCard.getCardValue())) {
                cardMap.put(currentCard.getCardValue(), 1);
            } else {
                cardMap.put(currentCard.getCardValue(), cardMap.get(currentCard.getCardValue()) + 1);
            }
        }
        ArrayList<Integer> pairs = new ArrayList<>();
        ArrayList<Integer> triples = new ArrayList<>();
        int fourOfKind = -1;
        for (Integer key : cardMap.keySet()) {
            if (cardMap.get(key) == 4) {
                fourOfKind = key;
            } else if (cardMap.get(key) == 3) {
                triples.add(key);
            } else if (cardMap.get(key) == 2) {
                pairs.add(key);
            }
        }
        int straightFlushHigh = isStraightFlush();
        Card flushHigh = isFlush();
        int straightHigh = isStraight();
        ArrayList<Integer> tieBreakers = new ArrayList<>();
        if (straightFlushHigh != -1) { //straight flush or royal flush
            tieBreakers.add(straightFlushHigh);
            return new HandResult(8, "Straight flush with " + Card.valueIntoName(straightFlushHigh), tieBreakers); 
        }
        if (fourOfKind != -1) { //4 of a kind
            tieBreakers.add(fourOfKind);
            
            int highestKicker = -1;

            for (Card card : evaluator) {
                if (card.getCardValue() != fourOfKind && card.getCardValue() > highestKicker) {
                    highestKicker = card.getCardValue();
                }
            }

            tieBreakers.add(highestKicker);
            return new HandResult(7, "4 of a kind with " + Card.valueIntoName(fourOfKind), tieBreakers);
        }
        if (triples.size() > 1) { //3 of a kind + 3 of a kind (Full house)
            Collections.sort(triples);
            int highestTriple = triples.get(triples.size() - 1);
            int secondTriple = triples.get(triples.size() - 2);
            tieBreakers.add(highestTriple);
            tieBreakers.add(secondTriple);
            return new HandResult(6, "Full house with " + Card.valueIntoName(highestTriple) + " over " + Card.valueIntoName(secondTriple), tieBreakers);
        }
        if (!triples.isEmpty() && !pairs.isEmpty()) { //Full house
            int highestTriple = Collections.max(triples);
            int highestPair = Collections.max(pairs);
            tieBreakers.add(highestTriple);
            tieBreakers.add(highestPair);
            return new HandResult(6, "Full house with " + Card.valueIntoName(highestTriple) + " over " + Card.valueIntoName(highestPair), tieBreakers);
        }
        if (flushHigh != null) { //flush
            PriorityQueue<Integer> greatestFirst = new PriorityQueue<>(Collections.reverseOrder());
            for (Card card : evaluator) {
                if (flushHigh.getCardType() == card.getCardType()) {
                    greatestFirst.add(card.getCardValue());
                }
            }
            int i = 0;
            while (i < 5) {
                tieBreakers.add(greatestFirst.poll());
                i++;
            }
            return new HandResult(5, "Flush with " + flushHigh.getCardName(), tieBreakers);
        }
        if (straightHigh != -1) { //straight
            tieBreakers.add(straightHigh);
            return new HandResult(4, "Straight with " + Card.valueIntoName(straightHigh), tieBreakers);
        }
        if (!triples.isEmpty()) { //3 of a kind
            tieBreakers.add(triples.get(0));

            int highestKicker = -1;
            int secondHighestKicker = -1;

            for (Card card : evaluator) {
                int value = card.getCardValue(); 

                if (value != triples.get(0)) {
                    if (value > highestKicker) {
                        secondHighestKicker = highestKicker;
                        highestKicker = value;
                    }
                    else if (value > secondHighestKicker) {
                        secondHighestKicker = value;
                    }
                }
            }
            tieBreakers.add(highestKicker);
            tieBreakers.add(secondHighestKicker);
            return new HandResult(3, "3 of a kind with " + Card.valueIntoName(triples.get(0)), tieBreakers);
        }
        if (pairs.size() > 1) { //2 pair
            Collections.sort(pairs);
            int highestPair = pairs.get(pairs.size()-1);
            int secondHighestPair = pairs.get(pairs.size() - 2);
            tieBreakers.add(highestPair);
            tieBreakers.add(secondHighestPair);

            int highestKicker = -1;

            for (Card card : evaluator) {
                if (card.getCardValue() != highestPair && card.getCardValue() != secondHighestPair && card.getCardValue() > highestKicker) {
                    highestKicker = card.getCardValue();
                }
            }
            tieBreakers.add(highestKicker);

            return new HandResult(2, "Two pair with " + Card.valueIntoName(highestPair) + " and " + Card.valueIntoName(secondHighestPair), tieBreakers);
        }
        if (pairs.size() == 1) { //pair
            tieBreakers.add(pairs.get(0));
            PriorityQueue<Integer> greatestFirst = new PriorityQueue<>(Collections.reverseOrder());
            for (Card card : evaluator) {
                if (card.getCardValue() != pairs.get(0)) {
                    greatestFirst.add(card.getCardValue());
                }
            }
            int i = 0;
            while (i < 3) {
                tieBreakers.add(greatestFirst.poll());
                i++;
            }
            return new HandResult(1, "Pair with " + Card.valueIntoName(pairs.get(0)), tieBreakers);
        }
        PriorityQueue<Integer> greatestFirst = new PriorityQueue<>(Collections.reverseOrder());
            for (Card card : evaluator) {
                greatestFirst.add(card.getCardValue());
            }
            int i = 0;
            while (i < 5) {
                tieBreakers.add(greatestFirst.poll());
                i++;
            }
        return new HandResult(0, "High card with " + Card.valueIntoName(Collections.max(cardMap.keySet())), tieBreakers);
    }
}
