import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for Card.
 */
public class CardTest {

    // ── Color extraction ───────────────────────────────────────────────────

    @Test void colorOfR5()   { assertEquals("R", Card.of("R5").getColor()); }
    @Test void colorOfY3()   { assertEquals("Y", Card.of("Y3").getColor()); }
    @Test void colorOfGp2()  { assertEquals("G", Card.of("G+2").getColor()); }
    @Test void colorOfBS()   { assertEquals("B", Card.of("BS").getColor()); }
    @Test void colorOfWild() { assertEquals("",  Card.of("W").getColor()); }
    @Test void colorOfW4()   { assertEquals("",  Card.of("W4").getColor()); }

    // ── Rank identification ────────────────────────────────────────────────

    @Test void rankR5()   { assertEquals(Card.Rank.NUMBER,         Card.of("R5").getRank()); }
    @Test void rankR0()   { assertEquals(Card.Rank.NUMBER,         Card.of("R0").getRank()); }
    @Test void rankGS()   { assertEquals(Card.Rank.SKIP,           Card.of("GS").getRank()); }
    @Test void rankYR()   { assertEquals(Card.Rank.REVERSE,        Card.of("YR").getRank()); }
    @Test void rankBp2()  { assertEquals(Card.Rank.DRAW_TWO,       Card.of("B+2").getRank()); }
    @Test void rankW()    { assertEquals(Card.Rank.WILD,           Card.of("W").getRank()); }
    @Test void rankW4()   { assertEquals(Card.Rank.WILD_DRAW_FOUR, Card.of("W4").getRank()); }

    // ── Number extraction ──────────────────────────────────────────────────

    @Test void numberR0()  { assertEquals(0,  Card.of("R0").getNumber()); }
    @Test void numberB9()  { assertEquals(9,  Card.of("B9").getNumber()); }
    @Test void numberY7()  { assertEquals(7,  Card.of("Y7").getNumber()); }
    @Test void numberGS()  { assertEquals(-1, Card.of("GS").getNumber()); }
    @Test void numberWild(){ assertEquals(-1, Card.of("W").getNumber()); }

    // ── Point values ───────────────────────────────────────────────────────

    @Test void pointsR5()  { assertEquals(5,  Card.of("R5").points()); }
    @Test void pointsB0()  { assertEquals(0,  Card.of("B0").points()); }
    @Test void pointsG9()  { assertEquals(9,  Card.of("G9").points()); }
    @Test void pointsRS()  { assertEquals(20, Card.of("RS").points()); }
    @Test void pointsYR()  { assertEquals(20, Card.of("YR").points()); }
    @Test void pointsBp2() { assertEquals(20, Card.of("B+2").points()); }
    @Test void pointsW()   { assertEquals(50, Card.of("W").points()); }
    @Test void pointsW4()  { assertEquals(50, Card.of("W4").points()); }

    // ── Code round-trip ────────────────────────────────────────────────────

    @Test void codeR5()  { assertEquals("R5",  Card.of("R5").getCode()); }
    @Test void codeW4()  { assertEquals("W4",  Card.of("W4").getCode()); }
    @Test void codeGp2() { assertEquals("G+2", Card.of("G+2").getCode()); }

    // ── toString ──────────────────────────────────────────────────────────

    @Test void toStringReturnsCode() { assertEquals("R5", Card.of("R5").toString()); }
}