import java.util.List;

/**
 * Characterization tests for Card.
 *
 * These tests document the exact behavior of Card.of() parsing and Card methods
 * as they exist in this implementation.  They are not tests for ideal UNO —
 * they capture how this codebase works so refactoring does not silently break it.
 */
public class CardTest {

    public static void main(String[] args) {
        TestRunner r = new TestRunner("CardTest");

        // ── Color extraction ───────────────────────────────────────────────
        r.check(Card.of("R5").getColor().equals("R"),   "color of R5 is R");
        r.check(Card.of("Y3").getColor().equals("Y"),   "color of Y3 is Y");
        r.check(Card.of("G+2").getColor().equals("G"),  "color of G+2 is G");
        r.check(Card.of("BS").getColor().equals("B"),   "color of BS is B");
        r.check(Card.of("W").getColor().equals(""),     "wild has no color");
        r.check(Card.of("W4").getColor().equals(""),    "wild-draw-four has no color");

        // ── Rank identification ────────────────────────────────────────────
        r.check(Card.of("R5").getRank()  == Card.Rank.NUMBER,         "R5 rank is NUMBER");
        r.check(Card.of("R0").getRank()  == Card.Rank.NUMBER,         "R0 rank is NUMBER");
        r.check(Card.of("GS").getRank()  == Card.Rank.SKIP,           "GS rank is SKIP");
        r.check(Card.of("YR").getRank()  == Card.Rank.REVERSE,        "YR rank is REVERSE");
        r.check(Card.of("B+2").getRank() == Card.Rank.DRAW_TWO,       "B+2 rank is DRAW_TWO");
        r.check(Card.of("W").getRank()   == Card.Rank.WILD,           "W rank is WILD");
        r.check(Card.of("W4").getRank()  == Card.Rank.WILD_DRAW_FOUR, "W4 rank is WILD_DRAW_FOUR");

        // ── Number extraction ──────────────────────────────────────────────
        r.check(Card.of("R0").getNumber() == 0,  "R0 number is 0");
        r.check(Card.of("B9").getNumber() == 9,  "B9 number is 9");
        r.check(Card.of("Y7").getNumber() == 7,  "Y7 number is 7");
        r.check(Card.of("GS").getNumber() == -1, "non-number returns -1");
        r.check(Card.of("W").getNumber()  == -1, "wild returns -1 for number");

        // ── Point values ───────────────────────────────────────────────────
        r.check(Card.of("R5").points()  == 5,  "R5 is worth 5 points");
        r.check(Card.of("B0").points()  == 0,  "B0 is worth 0 points");
        r.check(Card.of("G9").points()  == 9,  "G9 is worth 9 points");
        r.check(Card.of("RS").points()  == 20, "skip is worth 20 points");
        r.check(Card.of("YR").points()  == 20, "reverse is worth 20 points");
        r.check(Card.of("B+2").points() == 20, "draw-two is worth 20 points");
        r.check(Card.of("W").points()   == 50, "wild is worth 50 points");
        r.check(Card.of("W4").points()  == 50, "wild-draw-four is worth 50 points");

        // ── Code round-trip ────────────────────────────────────────────────
        r.check(Card.of("R5").getCode().equals("R5"),   "R5 code round-trip");
        r.check(Card.of("W4").getCode().equals("W4"),   "W4 code round-trip");
        r.check(Card.of("G+2").getCode().equals("G+2"), "G+2 code round-trip");

        // ── toString ──────────────────────────────────────────────────────
        r.check(Card.of("R5").toString().equals("R5"),  "toString returns code");

        r.summary();
    }
}