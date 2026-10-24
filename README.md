# PokerBot — Java Texas Hold'em Game Engine

A terminal-playable Texas Hold'em game engine built in Java. PokerBot manages card dealing, betting rounds, player turns, hand evaluation, and chip payouts, including all-in situations and multiple side pots.

**Scope:** PokerBot is a rules-based poker game engine, **not** an AI poker opponent, odds calculator, or move-recommendation system.

## Features

- Generates a standard 52-card deck, shuffles it, and deals unique cards.
- Supports 2–6 named players in a single terminal-playable hand.
- Handles dealer position, small and big blinds, and preflop/postflop turn order.
- Supports fold, check, call, bet, and raise actions with turn and wager validation.
- Handles short all-in calls, minimum raises, and betting-stage completion.
- Evaluates Texas Hold'em hands from players' two hole cards and five community cards, including ties and ace-low straights.
- Calculates main pots, multiple side pots, eligible winners, and unmatched-bet refunds.
- Splits tied pots and awards odd chips according to position relative to the dealer.
- Ends the hand early if all but one player fold, or distributes chips at showdown.

## Requirements

- Java Development Kit (JDK) installed
- Terminal / command line

The project uses standard Java libraries; no external dependencies are required.

## Run the game

Open a terminal in the folder containing the `.java` files:

```bash
javac *.java
java Main
```

The terminal prompts for 2–6 players and their names. Players start with 500 chips each, with 5/10 blinds.

On your turn, enter one of the following commands:

```text
fold
check
call
bet 20
raise 50
```

**Bet and raise amounts represent the player's target total wager for the current betting stage.** For example, `raise 50` means raising *to* 50 chips, not adding 50 chips on top of the current bet.

After betting, the game reveals the flop, turn, and river, then determines payouts at showdown. If everyone else folds, the remaining player wins immediately.

> **Demo limitation:** All players use the same terminal, and the terminal displays each active player's hole cards during their turns. It is a local demonstration, not a private multi-user poker client. The demo plays one hand per run.

## Run the tests

The repository includes 14 custom Java regression scenarios in `PokerBotTests.java`.

```bash
javac *.java
java PokerBotTests
```

These cover:

- Turn order, folds, blinds, and heads-up positions
- Full raises, minimum bets, short all-in raises, and cumulative all-in raises
- Side-pot calculation, distribution, refunds, and chip conservation
- Early fold-to-win outcomes and short-stacked big blinds
- Ace-low straights and odd-chip tie payouts

The tests use Java helper methods and assertions, not JUnit. Passing these scenarios is useful regression coverage, not a guarantee that every possible poker scenario has been tested.

## Code structure

| File | Responsibility |
| --- | --- |
| `Card.java` | Represents a playing card |
| `CardDeck.java` | Creates, shuffles, and deals the deck |
| `Player.java` | Holds a player's cards, chip balance, and fold status |
| `HandEvaluator.java` | Determines the best hand from available cards |
| `HandResult.java` | Holds hand rankings and tie-breaking data |
| `Round.java` | Controls betting, turn order, blinds, hand progression, and payouts |
| `SidePot.java` | Represents a contribution pot, eligible winners, or refund |
| `Main.java` | Interactive terminal demonstration |
| `PokerBotTests.java` | Regression test scenarios |

## Engineering highlights

### Tracking bets across streets

`Round` uses **per-stage bets** to determine how much a player owes on the current street and **total contributions** to calculate payouts across the entire hand. Contributions persist when betting resets between streets.

### Side-pot algorithm

The game collects unique positive contribution thresholds, sorts them, and forms contribution layers. It counts contributors for each layer and limits eligibility to players who have not folded. Adjacent layers with identical eligible players can be combined; unmatched contributions are marked for refund.

### Game-state validation

Turn validation prevents folded, all-in, and out-of-turn players from acting. A completed hand rejects further betting or card dealing. Payouts account for ties and conserve chips across the tested scenarios.

## Current limitations

- One playable hand per run; no multi-hand tournament or persistent session.
- No AI opponent, strategy solver, or action recommendations.
- No graphical UI, accounts, online multiplayer, or saved game state.
- This is a portfolio/gameplay demonstration rather than a production poker service.

## Purpose

This project focuses on Java object-oriented design, collections, algorithmic problem solving, state management, edge cases, and testing.
