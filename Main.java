
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("=== WELCOME TO POKERBOT ===");

        // Get number of players
        int numPlayers;

        while (true) {
            System.out.print("Number of players (2-6): ");

            try {
                numPlayers = Integer.parseInt(scanner.nextLine().trim());

                if (numPlayers >= 2 && numPlayers <= 6) {
                    break;
                }

                System.out.println("Please enter between 2 and 6 players.");

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }

        // Get player names
        String[] names = new String[numPlayers];

        for (int i = 0; i < numPlayers; i++) {
            while (true) {
                System.out.print("Enter Player " + (i + 1) + " name: ");
                String name = scanner.nextLine().trim();

                if (!name.isEmpty()) {
                    names[i] = name;
                    break;
                }

                System.out.println("Name cannot be empty.");
            }
        }

        // Create and shuffle deck
        CardDeck deck = new CardDeck();
        deck.shuffle();

        // Create players with random cards
        ArrayList<Player> players = new ArrayList<>();

        for (int i = 0; i < numPlayers; i++) {
            players.add(new Player(
                deck.dealCard(),
                deck.dealCard(),
                500
            ));
        }

        // Start round
        Round round = new Round(players, 0);
        round.postBlinds(5, 10);

        System.out.println("\n=== GAME STARTED ===");
        System.out.println("Starting chips: 500 per player");
        System.out.println("Blinds: 5/10");
        System.out.println("Dealer: " + names[0]);

        // PREFLOP
        System.out.println("\n=== PREFLOP ===");
        playBettingStage(round, players, names, scanner);

        // FLOP
        if (!round.isHandComplete()) {
            round.dealFlop(
                deck.dealCard(),
                deck.dealCard(),
                deck.dealCard()
            );

            System.out.println("\n=== FLOP ===");
            printBoard(round);
            playBettingStage(round, players, names, scanner);
        }

        // TURN
        if (!round.isHandComplete()) {
            round.dealturn(deck.dealCard());

            System.out.println("\n=== TURN ===");
            printBoard(round);
            playBettingStage(round, players, names, scanner);
        }

        // RIVER
        if (!round.isHandComplete()) {
            round.dealRiver(deck.dealCard());

            System.out.println("\n=== RIVER ===");
            printBoard(round);
            playBettingStage(round, players, names, scanner);
        }

        // SHOWDOWN
        if (!round.isHandComplete()) {
            System.out.println("\n=== SHOWDOWN ===");

            printBoard(round);

            System.out.println("Pot: " + round.getPot());

            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);

                if (!player.isFolded()) {
                    System.out.println(
                        names[i] + ": " +
                        player.getCard1().getCardName() + ", " +
                        player.getCard2().getCardName()
                    );
                }
            }

            // Show each pot
            ArrayList<SidePot> pots = round.calculateSidePots();

            for (int i = 0; i < pots.size(); i++) {
                SidePot sidePot = pots.get(i);

                System.out.println(
                    (sidePot.isRefund() ? "Refund" : "Pot") +
                    " " + (i + 1) + ": " +
                    sidePot.getAmount() + " chips"
                );
            }

            round.distributePot();

            System.out.println("Showdown completed!");
        } else {
            System.out.println("\nHand ended early!");

            for (int i = 0; i < players.size(); i++) {
                if (!players.get(i).isFolded()) {
                    System.out.println(
                        names[i] + " wins by everyone else folding!"
                    );
                }
            }
        }

        // FINAL RESULTS
        System.out.println("\n=== FINAL CHIP BALANCES ===");

        int totalChips = 0;

        for (int i = 0; i < players.size(); i++) {
            int chips = players.get(i).getChips();
            totalChips += chips;

            System.out.println(
                names[i] + ": " + chips + " chips"
            );
        }

        System.out.println("Remaining pot: " + round.getPot());
        System.out.println("Total chips: " + totalChips);
        System.out.println("Game over!");

        scanner.close();
    }

    public static void printBoard(Round round) {
        System.out.println("Community cards:");

        for (Card card : round.getGame()) {
            System.out.print(card.getCardName() + " | ");
        }

        System.out.println();
    }

    public static void playBettingStage(
        Round round,
        ArrayList<Player> players,
        String[] names,
        Scanner scanner
    ) {

        while (!round.isBettingStageComplete()
                && !round.isHandComplete()) {

            Player current = round.getCurrentPlayer();
            int index = players.indexOf(current);

            System.out.println("\n----------------------");
            System.out.println("Turn: " + names[index]);
            System.out.println("Chips: " + current.getChips());
            System.out.println("Pot: " + round.getPot());

            System.out.println(
                "Your cards: " +
                current.getCard1().getCardName() + ", " +
                current.getCard2().getCardName()
            );

            System.out.println(
                "Actions: fold, check, call, bet AMOUNT, raise AMOUNT"
            );

            System.out.print("Choose action: ");

            String input = scanner.nextLine().trim().toLowerCase();
            String[] parts = input.split("\\s+");

            try {
                switch (parts[0]) {

                    case "fold":
                        round.fold(current);
                        break;

                    case "check":
                        round.check(current);
                        break;

                    case "call":
                        round.call(current);
                        break;

                    case "bet":
                        if (parts.length != 2) {
                            System.out.println("Usage: bet AMOUNT");
                            continue;
                        }

                        round.bet(
                            current,
                            Integer.parseInt(parts[1])
                        );
                        break;

                    case "raise":
                        if (parts.length != 2) {
                            System.out.println("Usage: raise AMOUNT");
                            continue;
                        }

                        round.raise(
                            current,
                            Integer.parseInt(parts[1])
                        );
                        break;

                    default:
                        System.out.println("Invalid action. Try again.");
                }

            } catch (IllegalArgumentException |
                     IllegalStateException e) {

                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
