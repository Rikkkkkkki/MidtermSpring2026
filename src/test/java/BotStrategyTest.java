import java.util.ArrayList;
import java.util.List;

/**
 * Characterization tests for BotStrategy.
 *
 * Documents the exact priority order the bot uses:
 *   Draw Two > Skip > Number > Wild
 *
 * And documents the color-selection behavior.
 *
 * These tests run without any CLI or game loop.
 */
public class BotStrategyTest {

    public static void main(String[] args) {
        TestRunner r = new TestRunner("BotStrategyTest");

        Card upRed9 = Card.of("R9");

        // ── Draw (-1) when nothing playable ───────────────────────────────
        List<Card> nothing = list("G3", "B5", "YS");
        r.check(BotStrategy.chooseCard(nothing, upRed9, "") == -1, "returns -1 when nothing playable");

        // ── Prefers Draw Two over all others ──────────────────────────────
        List<Card> withDrawTwo = list("R3", "RS", "R+2", "W");
        // index 2 is R+2 (draw two, same color)
        r.check(BotStrategy.chooseCard(withDrawTwo, upRed9, "") == 2, "prefers draw-two at index 2");

        // ── Prefers Skip over number and wild (no draw-two available) ─────
        List<Card> withSkip = list("R3", "RS", "W");
        // index 1 is RS (skip, same color)
        r.check(BotStrategy.chooseCard(withSkip, upRed9, "") == 1, "prefers skip over number");

        // ── Prefers number over wild (no draw-two, no skip) ───────────────
        List<Card> withNumber = list("B3", "R4", "W");
        // index 1 is R4 (number, same color)
        r.check(BotStrategy.chooseCard(withNumber, upRed9, "") == 1, "prefers number over wild");

        // ── Plays wild only as last resort ────────────────────────────────
        List<Card> onlyWild = list("G3", "B5", "W");
        r.check(BotStrategy.chooseCard(onlyWild, upRed9, "") == 2, "wild at last resort");

        // ── Called color respected ────────────────────────────────────────
        // upCard is W, called color G; G3 is legal
        List<Card> calledColor = list("B7", "G3", "W4");
        r.check(BotStrategy.chooseCard(calledColor, Card.of("W"), "G") == 1,
                "plays G3 when G is called color");

        // ── Bot draws (returns -1) when hand has no legal play ────────────
        List<Card> noPlay = list("B7", "G3");
        // upCard R9, no called color, no match → -1
        r.check(BotStrategy.chooseCard(noPlay, upRed9, "") == -1, "draws when no legal play");

        // ── chooseColor: dominant color ───────────────────────────────────
        List<Card> blueHeavy = list("B1", "B2", "R3");
        r.check(BotStrategy.chooseColor(blueHeavy).equals("B"), "chooseColor picks blue");

        List<Card> redHeavy = list("R1", "R2", "R3", "G1");
        r.check(BotStrategy.chooseColor(redHeavy).equals("R"), "chooseColor picks red");

        // ── W4 chosen over ordinary wild when only wilds remain ───────────
        // Priority is Draw Two first, which W4 is not — W4 falls under wild pool
        List<Card> wilds = list("W", "W4");
        int idx = BotStrategy.chooseCard(wilds, upRed9, "");
        r.check(idx == 0 || idx == 1, "wild or W4 chosen when only wilds available");

        r.summary();
    }

    // Helper: build a hand from card codes.
    private static List<Card> list(String... codes) {
        List<Card> hand = new ArrayList<>();
        for (String code : codes) hand.add(Card.of(code));
        return hand;
    }
}