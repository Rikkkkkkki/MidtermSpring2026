import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for Rules.
 */
public class RulesTest {

    // ── Match by color ─────────────────────────────────────────────────────

    @Test void sameColorNumber()  { assertTrue(Rules.isLegal(Card.of("R2"),  Card.of("R9"),  "")); }
    @Test void sameColorSkip()    { assertTrue(Rules.isLegal(Card.of("RS"),  Card.of("R5"),  "")); }
    @Test void sameColorDrawTwo() { assertTrue(Rules.isLegal(Card.of("R+2"), Card.of("R7"),  "")); }
    @Test void sameColorReverse() { assertTrue(Rules.isLegal(Card.of("RR"),  Card.of("R3"),  "")); }

    // ── Match by number ────────────────────────────────────────────────────

    @Test void sameNumber5()   { assertTrue(Rules.isLegal(Card.of("G5"), Card.of("R5"), "")); }
    @Test void sameNumberZero(){ assertTrue(Rules.isLegal(Card.of("B0"), Card.of("Y0"), "")); }
    @Test void sameNumber9()   { assertTrue(Rules.isLegal(Card.of("Y9"), Card.of("G9"), "")); }

    // ── Match by action type ───────────────────────────────────────────────

    @Test void skipOnSkip()       { assertTrue(Rules.isLegal(Card.of("GS"),  Card.of("BS"),  "")); }
    @Test void reverseOnReverse() { assertTrue(Rules.isLegal(Card.of("YR"),  Card.of("RR"),  "")); }
    @Test void drawTwoOnDrawTwo() { assertTrue(Rules.isLegal(Card.of("B+2"), Card.of("G+2"), "")); }

    // ── Wild always legal ──────────────────────────────────────────────────

    @Test void wildAlwaysLegal()        { assertTrue(Rules.isLegal(Card.of("W"),  Card.of("R9"), "")); }
    @Test void wildDrawFourAlwaysLegal(){ assertTrue(Rules.isLegal(Card.of("W4"), Card.of("R9"), "")); }
    @Test void wildLegalOnAction()      { assertTrue(Rules.isLegal(Card.of("W"),  Card.of("GS"), "")); }

    // ── Called-color match after wild ──────────────────────────────────────

    @Test void calledColorNumber() { assertTrue(Rules.isLegal(Card.of("B3"), Card.of("W"),  "B")); }
    @Test void calledColorSkip()   { assertTrue(Rules.isLegal(Card.of("BS"), Card.of("W4"), "B")); }
    @Test void calledColorOverride(){ assertTrue(Rules.isLegal(Card.of("G2"), Card.of("R5"), "G")); }

    // ── Illegal combinations ───────────────────────────────────────────────

    @Test void illegalColorMismatch()     { assertFalse(Rules.isLegal(Card.of("B3"),  Card.of("R9"),  "")); }
    @Test void illegalNumberAndColor()    { assertFalse(Rules.isLegal(Card.of("G5"),  Card.of("R9"),  "")); }
    @Test void illegalSkipOnNumber()      { assertFalse(Rules.isLegal(Card.of("GS"),  Card.of("R9"),  "")); }
    @Test void illegalWrongCalledColor()  { assertFalse(Rules.isLegal(Card.of("B3"),  Card.of("W4"), "R")); }

    // ── scoreHand ──────────────────────────────────────────────────────────

    @Test void scoreHandMixed() {
        List<Card> hand = new ArrayList<>();
        hand.add(Card.of("R5"));   // 5
        hand.add(Card.of("B9"));   // 9
        hand.add(Card.of("GS"));   // 20
        hand.add(Card.of("W"));    // 50
        assertEquals(84, Rules.scoreHand(hand));
    }

    @Test void scoreHandEmpty() {
        assertEquals(0, Rules.scoreHand(new ArrayList<>()));
    }

    // ── dominantColor ──────────────────────────────────────────────────────

    @Test void dominantBlue() {
        List<Card> hand = new ArrayList<>();
        hand.add(Card.of("B1")); hand.add(Card.of("B2")); hand.add(Card.of("R3"));
        assertEquals("B", Rules.dominantColor(hand));
    }

    @Test void dominantRed() {
        List<Card> hand = new ArrayList<>();
        hand.add(Card.of("R1")); hand.add(Card.of("R2")); hand.add(Card.of("G3"));
        assertEquals("R", Rules.dominantColor(hand));
    }

    @Test void tieBreakROverY() {
        List<Card> hand = new ArrayList<>();
        hand.add(Card.of("R1")); hand.add(Card.of("Y2"));
        assertEquals("R", Rules.dominantColor(hand));
    }

    @Test void allWildsDefaultsToR() {
        List<Card> hand = new ArrayList<>();
        hand.add(Card.of("W")); hand.add(Card.of("W4"));
        assertEquals("R", Rules.dominantColor(hand));
    }
}