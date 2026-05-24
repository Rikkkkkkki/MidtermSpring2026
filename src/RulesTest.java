import java.util.ArrayList;
import java.util.List;

/**
 * Characterization tests for Rules.
 *
 * Covers every legality path in the original Main.isLegal():
 *   - color match
 *   - number match
 *   - action-type match
 *   - wild always legal
 *   - called-color match after wild
 *   - illegal mismatch combinations
 *   - scoring
 *   - dominant-color selection
 *
 * These tests can run without any CLI, game loop, or Scanner.
 */
public class RulesTest {

    public static void main(String[] args) {
        TestRunner r = new TestRunner("RulesTest");

        // ── Match by color ─────────────────────────────────────────────────
        r.check(Rules.isLegal(Card.of("R2"), Card.of("R9"), ""),   "R2 on R9 - same color");
        r.check(Rules.isLegal(Card.of("RS"), Card.of("R5"), ""),   "RS on R5 - skip same color");
        r.check(Rules.isLegal(Card.of("R+2"), Card.of("R7"), ""),  "R+2 on R7 - draw-two same color");
        r.check(Rules.isLegal(Card.of("RR"), Card.of("R3"), ""),   "RR on R3 - reverse same color");

        // ── Match by number ────────────────────────────────────────────────
        r.check(Rules.isLegal(Card.of("G5"), Card.of("R5"), ""),   "G5 on R5 - same number");
        r.check(Rules.isLegal(Card.of("B0"), Card.of("Y0"), ""),   "B0 on Y0 - zero matches");
        r.check(Rules.isLegal(Card.of("Y9"), Card.of("G9"), ""),   "Y9 on G9 - same number");

        // ── Match by action type ───────────────────────────────────────────
        r.check(Rules.isLegal(Card.of("GS"), Card.of("BS"), ""),   "GS on BS - skip on skip");
        r.check(Rules.isLegal(Card.of("YR"), Card.of("RR"), ""),   "YR on RR - reverse on reverse");
        r.check(Rules.isLegal(Card.of("B+2"), Card.of("G+2"), ""), "B+2 on G+2 - draw-two on draw-two");

        // ── Wild always legal ──────────────────────────────────────────────
        r.check(Rules.isLegal(Card.of("W"),  Card.of("R9"), ""),   "W always legal");
        r.check(Rules.isLegal(Card.of("W4"), Card.of("R9"), ""),   "W4 always legal");
        r.check(Rules.isLegal(Card.of("W"),  Card.of("GS"), ""),   "W legal on action card");

        // ── Called-color match after wild ──────────────────────────────────
        r.check(Rules.isLegal(Card.of("B3"), Card.of("W"),  "B"),  "B3 on W called B");
        r.check(Rules.isLegal(Card.of("BS"), Card.of("W4"), "B"),  "BS on W4 called B");

        // Called color takes precedence over upCard color mismatch.
        r.check(Rules.isLegal(Card.of("G2"), Card.of("R5"), "G"),  "G2 legal when G is called");

        // ── Illegal combinations ───────────────────────────────────────────
        r.check(!Rules.isLegal(Card.of("B3"), Card.of("R9"), ""),  "B3 on R9 - illegal");
        r.check(!Rules.isLegal(Card.of("G5"), Card.of("R9"), ""),  "G5 on R9 - different number, different color");
        r.check(!Rules.isLegal(Card.of("GS"), Card.of("R9"), ""),  "GS on R9 - skip can't match number card different color");
        r.check(!Rules.isLegal(Card.of("B3"), Card.of("W4"), "R"), "B3 when R called after W4");

        // ── scoreHand ──────────────────────────────────────────────────────
        List<Card> hand = new ArrayList<>();
        hand.add(Card.of("R5"));   // 5
        hand.add(Card.of("B9"));   // 9
        hand.add(Card.of("GS"));   // 20
        hand.add(Card.of("W"));    // 50
        r.check(Rules.scoreHand(hand) == 84, "scoreHand: 5+9+20+50=84 (from rules.html example)");

        List<Card> emptyHand = new ArrayList<>();
        r.check(Rules.scoreHand(emptyHand) == 0, "scoreHand of empty hand is 0");

        // ── dominantColor ──────────────────────────────────────────────────
        List<Card> blueHeavy = new ArrayList<>();
        blueHeavy.add(Card.of("B1"));
        blueHeavy.add(Card.of("B2"));
        blueHeavy.add(Card.of("R3"));
        r.check(Rules.dominantColor(blueHeavy).equals("B"), "dominant color is B");

        List<Card> redHeavy = new ArrayList<>();
        redHeavy.add(Card.of("R1"));
        redHeavy.add(Card.of("R2"));
        redHeavy.add(Card.of("G3"));
        r.check(Rules.dominantColor(redHeavy).equals("R"), "dominant color is R");

        // Tie on R and Y: R wins (original tie-break order).
        List<Card> tieRY = new ArrayList<>();
        tieRY.add(Card.of("R1"));
        tieRY.add(Card.of("Y2"));
        r.check(Rules.dominantColor(tieRY).equals("R"), "R wins tie-break over Y");

        // All wilds: falls through to B default in original code.
        List<Card> allWilds = new ArrayList<>();
        allWilds.add(Card.of("W"));
        allWilds.add(Card.of("W4"));
        r.check(Rules.dominantColor(allWilds).equals("R"), "all wilds -> R (all counts zero, R wins tie-break)");

        r.summary();
    }
}