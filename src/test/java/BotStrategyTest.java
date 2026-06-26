import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for BotStrategy.
 */
public class BotStrategyTest {

    private static List<Card> list(String... codes) {
        List<Card> hand = new ArrayList<>();
        for (String code : codes) hand.add(Card.of(code));
        return hand;
    }

    private static final Card UP_RED9 = Card.of("R9");

    @Test void drawsWhenNothingPlayable() {
        assertEquals(-1, BotStrategy.chooseCard(list("G3", "B5", "YS"), UP_RED9, ""));
    }

    @Test void prefersDrawTwo() {
        // hand: R3=0, RS=1, R+2=2, W=3 — index 2 is draw two
        assertEquals(2, BotStrategy.chooseCard(list("R3", "RS", "R+2", "W"), UP_RED9, ""));
    }

    @Test void prefersSkipOverNumber() {
        // hand: R3=0, RS=1, W=2 — index 1 is skip
        assertEquals(1, BotStrategy.chooseCard(list("R3", "RS", "W"), UP_RED9, ""));
    }

    @Test void prefersNumberOverWild() {
        // hand: B3=0, R4=1, W=2 — index 1 is number same color
        assertEquals(1, BotStrategy.chooseCard(list("B3", "R4", "W"), UP_RED9, ""));
    }

    @Test void playsWildAsLastResort() {
        // hand: G3=0, B5=1, W=2 — only wild matches
        assertEquals(2, BotStrategy.chooseCard(list("G3", "B5", "W"), UP_RED9, ""));
    }

    @Test void respectsCalledColor() {
        // upCard W, called G; G3 at index 1 is legal
        assertEquals(1, BotStrategy.chooseCard(list("B7", "G3", "W4"), Card.of("W"), "G"));
    }

    @Test void drawsWhenNoLegalPlay() {
        assertEquals(-1, BotStrategy.chooseCard(list("B7", "G3"), UP_RED9, ""));
    }

    @Test void chooseColorPicksBlue() {
        assertEquals("B", BotStrategy.chooseColor(list("B1", "B2", "R3")));
    }

    @Test void chooseColorPicksRed() {
        assertEquals("R", BotStrategy.chooseColor(list("R1", "R2", "R3", "G1")));
    }

    @Test void wildsOnlyChoosesEither() {
        int idx = BotStrategy.chooseCard(list("W", "W4"), UP_RED9, "");
        assertTrue(idx == 0 || idx == 1);
    }
}