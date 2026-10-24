import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;
public class Round {

    private ArrayList<Card> game = new ArrayList<>();
    private ArrayList<Player> players;
    private int pot = 0; 
    private int currentBet = 0;
    private HashMap<Player, Integer> playerBets = new HashMap<>();
    private HashSet<Player> actedThisStage = new HashSet<>();
    private int currentPlayerIndex = 0;
    private HashMap<Player, Integer> totalContributions = new HashMap<>();
    private boolean handComplete = false;
    private int dealerIndex;
    private boolean blindsPosted = false;
    private int lastFullRaiseSize = 0;
    private int bigBlindAmount = 0;
    private HashMap<Player, Integer> lastActedBet = new HashMap<>();

    public Round(ArrayList<Player> players) {
        this(players, 0);
    }

    public boolean isHandComplete() {
        return handComplete;
    }

    public Round(ArrayList<Player> players, int dealerIndex) {
        if (players == null || players.size() < 2) {
            throw new IllegalArgumentException("Round needs at least 2 players");
        }
        if (dealerIndex < 0 || dealerIndex >= players.size()) {
            throw new IllegalArgumentException("Invalid dealer index");
        }
        this.players = new ArrayList<>(players);
        this.dealerIndex = dealerIndex;

        for (Player player : players) {
            playerBets.put(player, 0);
            totalContributions.put(player, 0);
        }
        resetTurnOrder();
    }

    private void resetPostflopTurnOrder() {
        if (isBettingStageComplete()) {
            currentPlayerIndex = -1;
            return;
        }

        for (int i = 1; i <= players.size(); i++) {
            int nextIndex = (dealerIndex + i) % players.size();

            if (canAct(players.get(nextIndex))) {
                currentPlayerIndex = nextIndex;
                return;
            }
        }

        throw new IllegalStateException("No eligible players");
    }

    public void postBlinds(int smallBlind, int bigBlind) {
        if (handComplete || blindsPosted || pot != 0|| !game.isEmpty()) {
            throw new IllegalStateException("Cannot post blinds now");
        }

        if (smallBlind <= 0 || bigBlind <= smallBlind) {
            throw new IllegalArgumentException("Invalid blind amounts");
        }

        bigBlindAmount = bigBlind;

        int smallBlindIndex;
        int bigBlindIndex;

        if (players.size() == 2) {
            smallBlindIndex = dealerIndex;
            bigBlindIndex = (dealerIndex + 1) % players.size();
        }
        else {
            smallBlindIndex = (dealerIndex + 1) % players.size();
            bigBlindIndex = (dealerIndex + 2) % players.size();
        }

        Player sbPlayer = players.get(smallBlindIndex);
        Player bbPlayer = players.get(bigBlindIndex);

        int sbPayment = Math.min(smallBlind, sbPlayer.getChips());
        int bbPayment = Math.min(bigBlind, bbPlayer.getChips());

        if (sbPayment > 0) {
            addToPot(sbPlayer, sbPayment);
        }

        if (bbPayment > 0) {
            addToPot(bbPlayer, bbPayment);
        }

        currentBet = bigBlind;
        lastFullRaiseSize = bigBlind;
        blindsPosted = true;

        if (isBettingStageComplete()) {
            currentPlayerIndex = -1;
            return;
        }

        for (int i = 1; i <= players.size(); i++) {
            int nextIndex = (bigBlindIndex + i) % players.size();

            if (canAct(players.get(nextIndex))) {
                currentPlayerIndex = nextIndex;
                return;
            }
        }
        throw new IllegalStateException("No eligible players");
    }

    public Player getDealer() {
        return players.get(dealerIndex);
    }

    public ArrayList<Card> getGame() {
        return game;
    }

    public ArrayList<Player> getPlayers() {
        return new ArrayList<Player>(players);
    }
    
    public int getPot() {
        return pot;
    }

    public void dealFlop(Card card1, Card card2, Card card3) {
        if (handComplete) {
            throw new IllegalStateException("Hand is already complete");
        }
        if (game.size() != 0) {
            throw new IllegalStateException("Flop already dealt");
        }
        resetBettingStage();

        game.add(card1);
        game.add(card2);
        game.add(card3);
    }

    public void dealturn(Card card) {
        if (handComplete) {
            throw new IllegalStateException("Hand is already complete");
        }
        if (game.size() != 3) {
            throw new IllegalStateException("Cannot deal turn yet");
        }
        resetBettingStage();
        game.add(card);
    }

    public void dealRiver(Card card) {
        if (handComplete) {
            throw new IllegalStateException("Hand is already complete");
        }
        if (game.size() != 4) {
            throw new IllegalStateException("Cannot deal river yet");
        }
        
        resetBettingStage();
        game.add(card);
    }

    private void addToPot(Player player, int amount) {
        if (!players.contains(player)) {
            throw new IllegalArgumentException("Player not in round");
        }
        player.removeChips(amount);
        pot += amount;

        playerBets.put(player, playerBets.get(player) + amount);
        totalContributions.put(player, totalContributions.get(player) + amount);
    }

    public void distributePot() {
        distributeSidePots();
    }

    public ArrayList<Player> findWinners() {
        if (game.size() != 5) {
            throw new IllegalStateException("River not dealt yet");
        }
        
        ArrayList<Player> winners = new ArrayList<>();
        HandResult bestHand = null;

        for (Player player : players) {
            if (player.isFolded()) {
                continue;
            }
            HandEvaluator evaluator = new HandEvaluator(player, game);
            HandResult currentHand = evaluator.evaluateHand();

            if (bestHand == null) {
                bestHand = currentHand;
                winners.add(player);
            } else {
                int comparison = HandResult.compareHands(currentHand, bestHand);

                if (comparison == 1) {
                    bestHand = currentHand;
                    winners.clear();
                    winners.add(player);
                } else if (comparison == 0) {
                    winners.add(player);
                }
            }
        }

        return winners;
    }

    public void check(Player player) {
        validateTurn(player);
        if (!players.contains(player)) {
            throw new IllegalArgumentException("Player not in round");
        }

        if (player.isFolded()) {
            throw new IllegalStateException("Folded player cannot check");
        }

        if (playerBets.get(player) != currentBet) {
            throw new IllegalStateException("Must call or raise");
        }
        actedThisStage.add(player);
        lastActedBet.put(player, currentBet);
        nextTurn();
    }

    public void call(Player player) {
        validateTurn(player);

        int amountToCall = currentBet - playerBets.get(player);

        if (amountToCall <= 0) {
            throw new IllegalStateException("Nothing to call");
        }

        int actualCall = Math.min(amountToCall, player.getChips());

        addToPot(player, actualCall);

        actedThisStage.add(player);
        lastActedBet.put(player, currentBet);
        nextTurn();
    }

    public void bet(Player player, int amount) {
        validateTurn(player);
        if (!players.contains(player)) {
            throw new IllegalArgumentException("Player not in round");
        }

        if (player.isFolded()) {
            throw new IllegalStateException("Folded player cannot bet");
        }

        if (currentBet != 0) {
            throw new IllegalStateException("Must raise instead");
        }

        if (amount <= 0 || amount > player.getChips()) {
            throw new IllegalArgumentException("Invalid bet amount");
        }

        if (amount < bigBlindAmount && amount != player.getChips()) {
            throw new IllegalArgumentException("Bet is below the minimum opening bet");
        }

        addToPot(player, amount);
        currentBet = amount;
        lastFullRaiseSize = Math.max(amount, bigBlindAmount);
        actedThisStage.clear();
        actedThisStage.add(player);
        lastActedBet.put(player, currentBet);
        nextTurn();
    }
    public void raise(Player player, int newBet) {
        validateTurn(player);

        boolean anotherPlayerCanAct = false;

        for (Player other : players) {
            if (other != player && canAct(other)) {
                anotherPlayerCanAct = true;
                break;
            }
        }

        if (!anotherPlayerCanAct) {
            throw new IllegalStateException("Cannot raise when no other player can act");
        }

        if (currentBet == 0) {
            throw new IllegalStateException("Must bet instead");
        }

        if (newBet <= currentBet) {
            throw new IllegalArgumentException("Raise must exceed current bet");
        }

        int amountToAdd = newBet - playerBets.get(player);

        if (amountToAdd > player.getChips()) {
            throw new IllegalStateException("Not enough chips");
        }

        if (actedThisStage.contains(player)) {
            int previousBet = lastActedBet.get(player);

            boolean facingShortOpening = previousBet == 0 && currentBet > 0 && currentBet < bigBlindAmount;

            if (!facingShortOpening && currentBet - previousBet < lastFullRaiseSize) {
                throw new IllegalStateException("Betting has not reopened for this player");
            }
        }

        boolean completingOpeningBet = bigBlindAmount > 0 && currentBet < bigBlindAmount;

        int minimumTotalBet;

        if (completingOpeningBet) {
            minimumTotalBet = bigBlindAmount;
        }
        else {
            minimumTotalBet = currentBet + lastFullRaiseSize;
        }

        boolean fullRaise = newBet >= minimumTotalBet;
        boolean allIn = amountToAdd == player.getChips();

        if (!fullRaise && !allIn) {
            throw new IllegalArgumentException("Raise is below the minimum required amount");
        }

        int raiseSize = newBet - currentBet;

        addToPot(player, amountToAdd);
        currentBet = newBet;

        if (fullRaise) {
            if (completingOpeningBet) {
                lastFullRaiseSize = bigBlindAmount;
            }
            else {
                lastFullRaiseSize = raiseSize;
            }

            actedThisStage.clear();
        }

        actedThisStage.add(player);
        lastActedBet.put(player, currentBet);
        nextTurn();
    }

    public boolean isBettingStageComplete() {
        int activePlayers = 0;
        int eligiblePlayers = 0;
        Player lastEligible = null; 

        for (Player player : players) {
            if (!player.isFolded()) {
                activePlayers++;
            }

            if(canAct(player)) {
                eligiblePlayers++;
                lastEligible = player;
            }
        }

        if (activePlayers <= 1 || eligiblePlayers == 0) {
            return true;
        }

        if (eligiblePlayers == 1 && playerBets.get(lastEligible) == currentBet) {
            return true;
        }

        for (Player player : players) {
            if (!canAct(player)) {
                continue; 
            }

            if (!actedThisStage.contains(player)) {
                return false;
            }

            if (playerBets.get(player) != currentBet) {
                return false;
            }
        }
        return true;
    }

    private void resetBettingStage() {
        if (!isBettingStageComplete()) {
            throw new IllegalStateException("Betting is not finished");
        }
        currentBet = 0;
        lastFullRaiseSize = bigBlindAmount;
        actedThisStage.clear();

        for (Player player : players) {
            playerBets.put(player, 0);
        }
        lastActedBet.clear();
        resetPostflopTurnOrder();
    }

    public Player getCurrentPlayer() {
        if (currentPlayerIndex == -1) {
            throw new IllegalStateException("No current player");
        }
        return players.get(currentPlayerIndex);
    }

    private void nextTurn() {
        if (isBettingStageComplete()) {
            currentPlayerIndex = -1;
            return;
        }

        for (int i = 1; i <= players.size(); i++) {
            int nextIndex = (currentPlayerIndex + i) % players.size();

            if(canAct(players.get(nextIndex))){
                currentPlayerIndex = nextIndex;
                return;
            }
        }
        throw new IllegalStateException("No eligible players");
    }

    private void validateTurn(Player player) {
        if (handComplete) {
            throw new IllegalStateException("Hand is already complete");
        }
        if (!players.contains(player)) {
            throw new IllegalArgumentException("Player not in round");
        }

        if (player.isFolded()) {
            throw new IllegalStateException("Folded player cannot act");
        }

        if (player.getChips() == 0) {
            throw new IllegalStateException("All-in player cannot act");
        }

        if(isBettingStageComplete()) {
            throw new IllegalStateException("Betting stage already complete");
        }

        if (player != getCurrentPlayer()) {
            throw new IllegalStateException("Not your turn");
        }
    }

    public void fold(Player player) {
        validateTurn(player);

        player.fold();

        int remainingPlayers = 0;
        Player lastPlayer = null;

        for (Player p : players) {
            if (!p.isFolded()) {
                remainingPlayers++;
                lastPlayer = p;
            }
        }

        if (remainingPlayers == 1) {
            if (pot > 0) {
                lastPlayer.addChips(pot);
            }

            pot = 0;
            handComplete = true;
            currentPlayerIndex = -1;
            return;
        }

        nextTurn();
    }

    private void resetTurnOrder() {
        if (isBettingStageComplete()) {
            currentPlayerIndex = -1;
            return;
        }

        for (int i = 0; i < players.size(); i++) {
            if (canAct(players.get(i))) {
                currentPlayerIndex = i;
                return;
            }
        }
        throw new IllegalStateException("No elgible players");
    }

    private boolean canAct(Player player) {
        return !player.isFolded() && player.getChips() > 0;
    }

    public ArrayList<SidePot> calculateSidePots() {
        ArrayList<SidePot> sidePots = new ArrayList<>();
        ArrayList<Integer> levels = new ArrayList<>(new HashSet<>(totalContributions.values()));

        levels.remove(Integer.valueOf(0));
        Collections.sort(levels);

        int previousLevel = 0;

        for (int level : levels) {
            int contributors = 0;
            ArrayList<Player> eligible = new ArrayList<>();

            for (Player player : players) {
                if (totalContributions.get(player) >= level) {
                    contributors++;

                    if (!player.isFolded()) {
                        eligible.add(player);
                    }
                }
            }
            int amount = (level - previousLevel) * contributors;
            
            if (contributors == 1) {
                if (eligible.size() != 1) {
                    throw new IllegalStateException("Invalid unmatched contribution");
                }
                sidePots.add(new SidePot(amount, eligible, true));
            }
            else if (!sidePots.isEmpty()) {
                SidePot lastPot = sidePots.get(sidePots.size() - 1);

                if (!lastPot.isRefund() && lastPot.getEligiblePlayers().equals(eligible)) {
                    int combinedAmount = lastPot.getAmount() + amount; 

                    sidePots.set(sidePots.size() - 1, new SidePot(combinedAmount, eligible)); 
                }
                else {
                    sidePots.add(new SidePot(amount, eligible));
                }
            }
            else {
                    sidePots.add(new SidePot(amount, eligible)); 
            }
            previousLevel = level;
        }
        return sidePots;
    }
    private ArrayList<Player> findWinnersForPot(SidePot sidePot) {
        ArrayList<Player> winners = new ArrayList<>();
        HandResult bestHand = null;

        for (Player player : sidePot.getEligiblePlayers()) {
            HandEvaluator evaluator = new HandEvaluator(player, game);
            HandResult currentHand = evaluator.evaluateHand();

            if (bestHand == null) {
                bestHand = currentHand;
                winners.add(player);
            }
            else {
                int comparison = HandResult.compareHands(currentHand, bestHand);

                if (comparison > 0) {
                    bestHand = currentHand;
                    winners.clear();
                    winners.add(player);
                }
                else if (comparison == 0) {
                    winners.add(player);
                }
            }
        }
        return winners;
    }

    public void distributeSidePots() {
        if (handComplete) {
            throw new IllegalStateException("Hand is already complete");
        }

        if (game.size() != 5) {
            throw new IllegalStateException("River not dealt yet");
        }

        if (!isBettingStageComplete()) {
            throw new IllegalStateException("Betting is not finished");
        }

        if (pot == 0) {
            throw new IllegalStateException("Pot is empty");
        }

        ArrayList<SidePot> sidePots = calculateSidePots();

        int total = 0;

        for (SidePot sidePot : sidePots) {
            total += sidePot.getAmount();

            if (sidePot.getEligiblePlayers().isEmpty()) {
                throw new IllegalStateException("Pot has no eligible players");
            }

            if (sidePot.isRefund() && sidePot.getEligiblePlayers().size() != 1) {
                throw new IllegalStateException("Invalid refund");
            }
        }
        if (total != pot) {
            throw new IllegalStateException("Pot amounts do not match");
        }

        for (SidePot sidePot : sidePots) {
            if (sidePot.isRefund()) {
                Player player = sidePot.getEligiblePlayers().get(0);
                player.addChips(sidePot.getAmount());
                continue;
            }

            ArrayList<Player> winners = findWinnersForPot(sidePot);

            int share = sidePot.getAmount() / winners.size();
            int remainder = sidePot.getAmount() % winners.size();

            for (Player winner : winners) {
                if (share > 0) {
                    winner.addChips(share);
                }
            }

            for (int i = 1; i <= players.size() && remainder > 0; i++) {
                int index = (dealerIndex + i) % players.size();
                Player player = players.get(index);

                if (winners.contains(player)) {
                    player.addChips(1);
                    remainder--;
                }
            }
        }
        pot = 0;
        handComplete = true;
        currentPlayerIndex = -1;  
    }
}