import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Characterization tests for GameState.
 *
 * Covers:
 *   - deck composition (exact card counts from original Main)
 *   - initial deal (7 cards per player)
 *   - up-card never starts as a wild
 *   - drawFromDeck reshuffles discard when empty
 *   - advanceTurn wraps around in both directions
 *   - score accumulation
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
        // (discard may also contain wilds that were replaced)
        int remaining = state2.deck.size() + state2.discard.size();
        r.check(remaining == 108 - 15, "93 cards remain after 2-player deal + up-card (excluding discard loop)");

        // ── Deck has correct card type counts (full fresh state) ──────────
        // Independently count cards in a fresh full deck.
        GameState full = makeState(2);
        full.resetForNewRound();
        // Reconstruct a full deck by combining deck + discard + hands + upCard.
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
        // Each color: 1×0, 2×1-9 = 19 number cards; 4 colors = 76.
        r.check(numberCards == 76, "76 number cards total");
        // Each color: 2 skip + 2 reverse + 2 draw-two = 6; 4 colors = 24.
        r.check(actionCards == 24, "24 action cards total");
        // 4 wilds + 4 wild-draw-fours = 8.
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
        // Force-empty the deck and add sentinel to discard.
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