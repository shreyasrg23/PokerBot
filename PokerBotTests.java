
import java.util.ArrayList;
import java.util.Arrays;

public class PokerBotTests {

    public static void main(String[] args) {

        testTurnOrder();
        testFullRaise();
        testBlinds();
        testHeadsUp();
        testMinimumBet();
        testShortAllInRaises();
        testCumulativeRaises();
        testSidePotDistribution();
        testFoldedPlayerRefund();

        testEarlyWin();
        testAceLowStraight();
        testMultipleSidePots();
        testShortBigBlind();
        testOddChipTie();

        System.out.println("\nALL 14 TESTS PASSED!");
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private static ArrayList<Player> group(Player... players) {
        return new ArrayList<>(Arrays.asList(players));
    }

    private static void verify(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void expectFailure(
        Class<? extends RuntimeException> type,
        Runnable action
    ) {
        try {
            action.run();
        } catch (RuntimeException e) {
            if (type.isInstance(e)) {
                return;
            }

            throw new AssertionError("Unexpected exception: " + e);
        }

        throw new AssertionError(
            "Expected exception: " + type.getSimpleName()
        );
    }

    // ==========================================
    // TEST 1: TURN ORDER AND FOLDING
    // ==========================================

    private static void testTurnOrder() {
        Player a = new Player(new Card('H', 14), new Card('H', 13), 200);
        Player b = new Player(new Card('D', 14), new Card('D', 13), 200);
        Player c = new Player(new Card('C', 14), new Card('C', 13), 200);

        Round round = new Round(group(a, b, c));

        verify(round.getCurrentPlayer() == a, "Alice should start");

        round.check(a);
        verify(round.getCurrentPlayer() == b, "Bob should act");

        round.fold(b);
        verify(round.getCurrentPlayer() == c, "Charlie should act");

        round.check(c);

        verify(round.isBettingStageComplete(),
               "Betting stage should finish");

        System.out.println("TEST 1 PASSED: Turn order and folding");
    }

    // ==========================================
    // TEST 2: FULL RAISE
    // ==========================================

    private static void testFullRaise() {
        Player a = new Player(new Card('H', 14), new Card('H', 13), 500);
        Player b = new Player(new Card('D', 14), new Card('D', 13), 500);
        Player c = new Player(new Card('C', 14), new Card('C', 13), 500);

        Round round = new Round(group(a, b, c));

        round.bet(a, 50);
        round.call(b);
        round.raise(c, 100);
        round.call(a);
        round.call(b);

        verify(round.isBettingStageComplete(),
               "Betting should finish");

        verify(round.getPot() == 300,
               "Pot should equal 300");

        System.out.println("TEST 2 PASSED: Full raise");
    }

    // ==========================================
    // TEST 3: BLINDS AND TURN ORDER
    // ==========================================

    private static void testBlinds() {
        Player a = new Player(new Card('H', 14), new Card('H', 13), 200);
        Player b = new Player(new Card('D', 14), new Card('D', 13), 200);
        Player c = new Player(new Card('C', 14), new Card('C', 13), 200);
        Player d = new Player(new Card('S', 14), new Card('S', 13), 200);

        Round round = new Round(group(a, b, c, d), 0);

        round.postBlinds(5, 10);

        verify(round.getPot() == 15,
               "Blinds should total 15");

        verify(round.getCurrentPlayer() == d,
               "David acts first preflop");

        round.call(d);
        round.call(a);
        round.call(b);
        round.check(c);

        verify(round.isBettingStageComplete(),
               "Preflop should finish");

        verify(round.getPot() == 40,
               "Pot should equal 40");

        round.dealFlop(
            new Card('S', 2),
            new Card('S', 3),
            new Card('S', 4)
        );

        verify(round.getCurrentPlayer() == b,
               "Bob acts first postflop");

        round.check(b);
        round.check(c);
        round.check(d);
        round.check(a);

        verify(round.isBettingStageComplete(),
               "Flop betting should finish");

        System.out.println("TEST 3 PASSED: Blinds and turn order");
    }

    // ==========================================
    // TEST 4: HEADS-UP POSITIONS
    // ==========================================

    private static void testHeadsUp() {
        Player a = new Player(new Card('H', 14), new Card('H', 13), 200);
        Player b = new Player(new Card('D', 14), new Card('D', 13), 200);

        Round round = new Round(group(a, b), 0);

        round.postBlinds(5, 10);

        verify(round.getCurrentPlayer() == a,
               "Dealer should act first preflop");

        round.call(a);
        round.check(b);

        round.dealFlop(
            new Card('C', 2),
            new Card('C', 3),
            new Card('C', 4)
        );

        verify(round.getCurrentPlayer() == b,
               "Big blind acts first postflop");

        System.out.println("TEST 4 PASSED: Heads-up positions");
    }

    // ==========================================
    // TEST 5: MINIMUM OPENING BET
    // ==========================================

    private static void testMinimumBet() {
        Player a = new Player(new Card('H', 14), new Card('H', 13), 200);
        Player b = new Player(new Card('D', 14), new Card('D', 13), 200);
        Player c = new Player(new Card('C', 14), new Card('C', 13), 200);

        Round round = new Round(group(a, b, c), 0);

        round.postBlinds(5, 10);

        round.call(a);
        round.call(b);
        round.check(c);

        round.dealFlop(
            new Card('S', 2),
            new Card('S', 3),
            new Card('S', 4)
        );

        expectFailure(
            IllegalArgumentException.class,
            () -> round.bet(b, 5)
        );

        verify(round.getCurrentPlayer() == b,
               "Invalid bet must not advance turn");

        round.bet(b, 10);
        round.call(c);
        round.call(a);

        verify(round.isBettingStageComplete(),
               "Betting should finish");

        System.out.println("TEST 5 PASSED: Minimum opening bet");
    }

    // ==========================================
    // TEST 6: SHORT ALL-IN RAISES
    // ==========================================

    private static void testShortAllInRaises() {
        Player a = new Player(new Card('H', 14), new Card('H', 13), 500);
        Player b = new Player(new Card('D', 14), new Card('D', 13), 160);
        Player c = new Player(new Card('C', 14), new Card('C', 13), 190);
        Player d = new Player(new Card('S', 14), new Card('S', 13), 500);

        Round round = new Round(group(a, b, c, d));

        round.bet(a, 100);
        round.raise(b, 160);
        round.raise(c, 190);
        round.call(d);

        verify(round.getCurrentPlayer() == a,
               "Alice should act again");

        expectFailure(
            IllegalStateException.class,
            () -> round.raise(a, 300)
        );

        round.call(a);

        verify(round.isBettingStageComplete(),
               "Betting should finish");

        verify(round.getPot() == 730,
               "Pot should equal 730");

        System.out.println("TEST 6 PASSED: Short all-in raises");
    }

    // ==========================================
    // TEST 7: CUMULATIVE SHORT ALL-IN RAISES
    // ==========================================

    private static void testCumulativeRaises() {
        Player a = new Player(new Card('H', 14), new Card('H', 13), 500);
        Player b = new Player(new Card('D', 14), new Card('D', 13), 160);
        Player c = new Player(new Card('C', 14), new Card('C', 13), 220);
        Player d = new Player(new Card('S', 14), new Card('S', 13), 500);

        Round round = new Round(group(a, b, c, d));

        round.bet(a, 100);
        round.raise(b, 160);
        round.raise(c, 220);
        round.call(d);

        // Alice acted at 100.
        // The current bet is now 220.
        // Cumulative increase = 120.
        // This reopens betting.

        round.raise(a, 320);
        round.call(d);

        verify(round.isBettingStageComplete(),
               "Betting should finish");

        verify(round.getPot() == 1020,
               "Pot should equal 1020");

        System.out.println("TEST 7 PASSED: Cumulative raises");
    }

    // ==========================================
    // TEST 8: SIDE-POT DISTRIBUTION
    // ==========================================

    private static void testSidePotDistribution() {
        Player a = new Player(new Card('H', 14), new Card('H', 13), 100);
        Player b = new Player(new Card('C', 2), new Card('D', 2), 300);
        Player c = new Player(new Card('S', 9), new Card('D', 9), 300);

        Round round = new Round(group(a, b, c));

        round.bet(a, 100);
        round.raise(b, 300);
        round.call(c);

        verify(round.getPot() == 700,
               "Pot should equal 700");

        ArrayList<SidePot> pots = round.calculateSidePots();

        verify(pots.size() == 2,
               "Should have two pots");

        verify(pots.get(0).getAmount() == 300,
               "Main pot should equal 300");

        verify(pots.get(1).getAmount() == 400,
               "Side pot should equal 400");

        round.dealFlop(
            new Card('H', 2),
            new Card('H', 4),
            new Card('H', 6)
        );

        round.dealturn(new Card('C', 9));
        round.dealRiver(new Card('D', 12));

        // Alice: Flush
        // Bob: Three 2s
        // Charlie: Three 9s
        // Alice wins main pot.
        // Charlie wins side pot.

        round.distributePot();

        verify(a.getChips() == 300,
               "Alice should win 300");

        verify(b.getChips() == 0,
               "Bob should win nothing");

        verify(c.getChips() == 400,
               "Charlie should win 400");

        verify(round.getPot() == 0,
               "Pot must be empty");

        System.out.println("TEST 8 PASSED: Side-pot distribution");
    }

    // ==========================================
    // TEST 9: FOLDED CONTRIBUTIONS AND REFUNDS
    // ==========================================

    private static void testFoldedPlayerRefund() {
        Player a = new Player(new Card('C', 2), new Card('D', 2), 100);
        Player b = new Player(new Card('H', 14), new Card('H', 13), 100);
        Player c = new Player(new Card('S', 9), new Card('D', 9), 300);

        Round round = new Round(group(a, b, c));

        round.bet(a, 50);
        round.raise(b, 100);
        round.raise(c, 200);
        round.fold(a);

        verify(round.getPot() == 350,
               "Pot should equal 350");

        ArrayList<SidePot> pots = round.calculateSidePots();

        verify(pots.size() == 2,
               "Should have two entries");

        verify(pots.get(0).getAmount() == 250,
               "Contested pot should equal 250");

        verify(pots.get(1).getAmount() == 100,
               "Refund should equal 100");

        verify(pots.get(1).isRefund(),
               "Last entry should be a refund");

        round.dealFlop(
            new Card('H', 2),
            new Card('H', 4),
            new Card('H', 6)
        );

        round.dealturn(new Card('C', 9));
        round.dealRiver(new Card('D', 12));

        // Bob wins contested pot.
        // Charlie receives unmatched chips back.

        round.distributePot();

        verify(a.getChips() == 50,
               "Alice should have 50");

        verify(b.getChips() == 250,
               "Bob should have 250");

        verify(c.getChips() == 200,
               "Charlie should have 200");

        verify(
            a.getChips() + b.getChips() + c.getChips() == 500,
            "Total chips must be conserved"
        );

        System.out.println("TEST 9 PASSED: Refund and chip conservation");
    }

    // ==========================================
    // TEST 10: EARLY FOLD-TO-WIN
    // ==========================================

    private static void testEarlyWin() {
        Player a = new Player(new Card('H', 14), new Card('H', 13), 100);
        Player b = new Player(new Card('D', 14), new Card('D', 13), 100);
        Player c = new Player(new Card('C', 14), new Card('C', 13), 100);

        Round round = new Round(group(a, b, c), 0);

        round.postBlinds(5, 10);

        round.fold(a);
        round.fold(b);

        verify(round.getPot() == 0,
               "Pot should be awarded");

        verify(c.getChips() == 105,
               "Big blind should win 15");

        verify(
            a.getChips() + b.getChips() + c.getChips() == 300,
            "Chips must be conserved"
        );

        boolean rejected = false;

        try {
            round.dealFlop(
                new Card('S', 2),
                new Card('S', 3),
                new Card('S', 4)
            );
        } catch (IllegalStateException e) {
            rejected = true;
        }

        verify(rejected,
               "Completed hand must reject dealing");

        System.out.println("TEST 10 PASSED: Early fold-to-win");
    }

    // ==========================================
    // TEST 11: ACE-LOW STRAIGHT
    // ==========================================

    private static void testAceLowStraight() {
        Player a = new Player(new Card('D', 14), new Card('S', 5), 100);
        Player b = new Player(new Card('D', 13), new Card('C', 12), 100);

        ArrayList<Card> board = new ArrayList<>(Arrays.asList(
            new Card('H', 2),
            new Card('D', 3),
            new Card('C', 4),
            new Card('S', 9),
            new Card('H', 13)
        ));

        HandResult wheel = new HandEvaluator(a, board).evaluateHand();
        HandResult pair = new HandEvaluator(b, board).evaluateHand();

        verify(
            HandResult.compareHands(wheel, pair) > 0,
            "Ace-low straight should beat a pair"
        );

        System.out.println("TEST 11 PASSED: Ace-low straight");
    }

    // ==========================================
    // TEST 12: MULTIPLE SIDE POTS
    // ==========================================

    private static void testMultipleSidePots() {
        Player a = new Player(new Card('H', 14), new Card('H', 13), 100);
        Player b = new Player(new Card('D', 14), new Card('D', 13), 200);
        Player c = new Player(new Card('C', 14), new Card('C', 13), 500);
        Player d = new Player(new Card('S', 14), new Card('S', 13), 500);

        Round round = new Round(group(a, b, c, d));

        round.bet(a, 100);
        round.raise(b, 200);
        round.raise(c, 500);
        round.call(d);

        ArrayList<SidePot> pots = round.calculateSidePots();

        verify(pots.size() == 3,
               "Expected three pots");

        verify(pots.get(0).getAmount() == 400,
               "Main pot incorrect");

        verify(pots.get(1).getAmount() == 300,
               "Side pot 1 incorrect");

        verify(pots.get(2).getAmount() == 600,
               "Side pot 2 incorrect");

        verify(
            pots.get(0).getEligiblePlayers().size() == 4,
            "Main pot should have four eligible players"
        );

        verify(
            pots.get(1).getEligiblePlayers().size() == 3,
            "Side pot 1 should have three eligible players"
        );

        verify(
            pots.get(2).getEligiblePlayers().size() == 2,
            "Side pot 2 should have two eligible players"
        );

        verify(round.getPot() == 1300,
               "Total pot incorrect");

        System.out.println("TEST 12 PASSED: Multiple side pots");
    }

    // ==========================================
    // TEST 13: SHORT-STACKED BIG BLIND
    // ==========================================

    private static void testShortBigBlind() {
        Player a = new Player(new Card('H', 14), new Card('H', 13), 100);
        Player b = new Player(new Card('D', 14), new Card('D', 13), 100);
        Player c = new Player(new Card('C', 14), new Card('C', 13), 4);

        Round round = new Round(group(a, b, c), 0);

        round.postBlinds(5, 10);

        verify(c.getChips() == 0,
               "Big blind should be all-in");

        verify(round.getPot() == 9,
               "Initial pot should equal 9");

        round.call(a);
        round.call(b);

        verify(round.getPot() == 24,
               "Pot should equal 24");

        verify(round.isBettingStageComplete(),
               "Betting should finish");

        round.dealFlop(
            new Card('S', 2),
            new Card('S', 3),
            new Card('S', 4)
        );

        verify(round.getCurrentPlayer() == b,
               "Small blind should act first on flop");

        System.out.println("TEST 13 PASSED: Short-stacked big blind");
    }

    // ==========================================
    // TEST 14: ODD-CHIP TIE PAYOUT
    // ==========================================

    private static void testOddChipTie() {
        Player a = new Player(new Card('H', 14), new Card('H', 13), 10);
        Player b = new Player(new Card('S', 14), new Card('S', 13), 10);
        Player c = new Player(new Card('D', 12), new Card('D', 10), 10);

        Round round = new Round(group(a, b, c), 0);

        round.bet(a, 1);
        round.call(b);
        round.call(c);

        round.dealFlop(
            new Card('C', 2),
            new Card('D', 2),
            new Card('H', 7)
        );

        round.check(b);
        round.check(c);
        round.check(a);

        round.dealturn(new Card('S', 9));

        round.check(b);
        round.check(c);
        round.check(a);

        round.dealRiver(new Card('H', 11));

        round.check(b);
        round.check(c);
        round.check(a);

        round.distributePot();

        // Alice and Bob tie.
        // Bob receives the odd chip because he
        // is first clockwise from the dealer.

        verify(a.getChips() == 10,
               "Alice should have 10");

        verify(b.getChips() == 11,
               "Bob should have 11");

        verify(c.getChips() == 9,
               "Charlie should have 9");

        verify(
            a.getChips() + b.getChips() + c.getChips() == 30,
            "Chips must be conserved"
        );

        System.out.println("TEST 14 PASSED: Odd-chip tie payout");
    }
}
