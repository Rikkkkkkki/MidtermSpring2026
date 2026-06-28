import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Comprehensive GameState tests covering:
 *   - Deck composition (exact card counts)
 *   - Initial deal (7 cards per player)
 *   - Up-card never starts as a wild
 *   - drawFromDeck reshuffles discard when empty
 *   - advanceTurn wraps around in both directions
 *   - Score accumulation
 *   - Multi-round game flow with state transitions
 *   - UNO call tracking and state management
 */
public class GameStateTest {

    public static void main(String[] args) {
        TestRunner r = new TestRunner("GameStateTest");

        // ── Setup helpers ──────────────────────────────────────────────────
        GameState state2 = makeState(2);
        state2.resetForNewRound();

        // ── Initial deal: 7 cards each ────────────────────────────────────
        r.check(state2.handOf(0).size() == 7, "player 0 gets 7 cards");
        r.check(state2.handOf(1).size() == 7, "player 1 gets 7 cards");

        // ── Up card is never a wild ────────────────────────────────────────
        r.check(state2.upCard.getRank() != Card.Rank.WILD,
                "up card is not a wild");
        r.check(state2.upCard.getRank() != Card.Rank.WILD_DRAW_FOUR,
                "up card is not a wild-draw-four");

        // ── Deck composition: 108 cards total before any draws ────────────
        // After deal (2 players × 7) + up card = 15 removed from 108 = 93 remaining
        int remaining = state2.deck.size() + state2.discard.size();
        r.check(remaining == 108 - 15, "93 cards remain after 2-player deal + up-card");

        // ── Full deck card type counts ──────────────────────────────────
        GameState full = makeState(2);
        full.resetForNewRound();
        List<Card> allCards = new ArrayList<>();
        allCards.addAll(full.deck);
        allCards.addAll(full.discard);
        for (int i = 0; i < full.playerCount(); i++) allCards.addAll(full.handOf(i));
        allCards.add(full.upCard);

        int numberCards = 0, actionCards = 0, wilds = 0;
        for (Card c : allCards) {
            switch (c.getRank()) {
                case NUMBER:         numberCards++; break;
                case SKIP:
                case REVERSE:
                case DRAW_TWO:       actionCards++; break;
                case WILD:
                case WILD_DRAW_FOUR: wilds++;       break;
            }
        }
        r.check(numberCards == 76, "76 number cards total");
        r.check(actionCards == 24, "24 action cards total");
        r.check(wilds == 8, "8 wild cards total");
        r.check(allCards.size() == 108, "108 cards total in deck");

        // ── advanceTurn wraps forward ──────────────────────────────────────
        GameState ts = makeState(3);
        ts.resetForNewRound();
        ts.currentPlayer = 2;
        ts.direction     = 1;
        ts.advanceTurn();
        r.check(ts.currentPlayer == 0, "wrap-around forward: 2 → 0 with 3 players");

        // ── advanceTurn wraps backward ────────────────────────────────────
        ts.currentPlayer = 0;
        ts.direction     = -1;
        ts.advanceTurn();
        r.check(ts.currentPlayer == 2, "wrap-around backward: 0 → 2 with 3 players");

        // ── drawFromDeck reshuffles discard when deck is empty ────────────
        GameState ds = makeState(2);
        ds.resetForNewRound();
        ds.deck.clear();
        ds.discard.add(Card.of("G7"));
        Card drawn = ds.drawFromDeck();
        r.check(drawn.getCode().equals("G7"), "draws from discard when deck empty");
        r.check(ds.discard.isEmpty(), "discard cleared after reshuffle");

        // ── drawFromDeck returns W fallback when both piles empty ─────────
        GameState empty = makeState(2);
        empty.resetForNewRound();
        empty.deck.clear();
        empty.discard.clear();
        Card fallback = empty.drawFromDeck();
        r.check(fallback.getCode().equals("W"), "fallback W card when both piles empty");

        // ── Score accumulation ────────────────────────────────────────────
        GameState sc = makeState(2);
        sc.resetForNewRound();
        r.check(sc.scoreOf(0) == 0, "initial score is 0");
        sc.addScore(0, 42);
        r.check(sc.scoreOf(0) == 42, "addScore accumulates correctly");
        sc.addScore(0, 8);
        r.check(sc.scoreOf(0) == 50, "addScore accumulates across calls");

        // ── Multi-round game flow with state transitions ────────────────
        GameState mr = makeState(3);
        mr.resetForNewRound();
        int initialHand0 = mr.handOf(0).size();
        r.check(initialHand0 == 7, "Round 1: Player 0 dealt 7 cards");

        mr.addScore(0, 25);
        mr.addScore(1, 15);
        r.check(mr.scoreOf(0) == 25 && mr.scoreOf(1) == 15,
                "After round 1: scores recorded correctly");

        mr.resetForNewRound();
        r.check(mr.scoreOf(0) == 25 && mr.scoreOf(1) == 15,
                "After reset: scores persist across rounds");
        int round2Hand0 = mr.handOf(0).size();
        r.check(round2Hand0 == 7, "Round 2: Player 0 re-dealt 7 cards after reset");

        mr.addScore(0, 10);
        mr.addScore(1, 20);
        r.check(mr.scoreOf(0) == 35 && mr.scoreOf(1) == 35,
                "After round 2: scores accumulate (25+10=35, 15+20=35)");

        mr.resetForNewRound();
        r.check(mr.scoreOf(0) == 35 && mr.scoreOf(1) == 35,
                "After reset: scores persist into round 3");
        mr.addScore(2, 30);
        r.check(mr.scoreOf(2) == 30, "New winner (player 2) scored 30 in round 3");

        // ── Game state transition consistency ───────────────────────────────
        GameState tr = makeState(4);
        tr.resetForNewRound();
        tr.direction = 1;
        tr.currentPlayer = 3;
        r.check(tr.currentPlayer == 3, "Set current player to 3");

        tr.resetForNewRound();
        r.check(tr.direction == 1, "Direction resets to 1 (forward)");
        r.check(tr.currentPlayer >= 0 && tr.currentPlayer < 4,
                "Current player is valid index after reset");

        tr.resetForNewRound();
        int totalDealt = tr.handOf(0).size() + tr.handOf(1).size() +
                tr.handOf(2).size() + tr.handOf(3).size();
        r.check(totalDealt == 28, "All 4 players dealt 7 cards each = 28 total");

        // ── UNO Call Tracking ──────────────────────────────────────────────
        GameState uno = makeState(3);
        uno.resetForNewRound();
        r.check(!uno.hasCalledUno(0), "Initially, no player has called UNO");
        r.check(!uno.hasCalledUno(1), "Initially, no player has called UNO");
        r.check(!uno.hasCalledUno(2), "Initially, no player has called UNO");

        // Player 0 claims UNO
        uno.setUnoCall(0, true);
        r.check(uno.hasCalledUno(0), "Player 0 UNO call recorded");
        r.check(!uno.hasCalledUno(1), "Player 1 still has not called UNO");

        // Reset clears UNO state
        uno.resetForNewRound();
        r.check(!uno.hasCalledUno(0), "UNO state reset for new round");
        r.check(!uno.hasCalledUno(1), "UNO state reset for new round");

        // ── UNO Penalty Detection Scenario ────────────────────────────────
        GameState penalty = makeState(3);
        penalty.resetForNewRound();
        // Simulate: player 0 has 1 card and didn't call UNO
        penalty.handOf(0).clear();
        penalty.handOf(0).add(Card.of("R5"));  // Force player 0 to have 1 card
        penalty.setUnoCall(0, false);  // They didn't call UNO

        r.check(penalty.handOf(0).size() == 1, "Player 0 has 1 card");
        r.check(!penalty.hasCalledUno(0), "Player 0 missed UNO call");
        // In real game, GameEngine.checkAndApplyUnoPenalty() would detect this

        r.summary();
    }

    private static GameState makeState(int playerCount) {
        List<String>  names = new ArrayList<>();
        List<Boolean> flags = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            names.add("Bot" + (i + 1));
            flags.add(Boolean.FALSE);
        }
        return new GameState(names, flags, new Random(42));
    }
}