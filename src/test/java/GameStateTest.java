import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for GameState.
 */
public class GameStateTest {

    private static GameState makeState(int playerCount) {
        List<String>  names = new ArrayList<>();
        List<Boolean> flags = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            names.add("Bot" + (i + 1));
            flags.add(Boolean.FALSE);
        }
        return new GameState(names, flags, new Random(42));
    }

    @Test void initialDealSevenCardsEach() {
        GameState s = makeState(2);
        s.resetForNewRound();
        assertEquals(7, s.handOf(0).size());
        assertEquals(7, s.handOf(1).size());
    }

    @Test void upCardNotWild() {
        GameState s = makeState(2);
        s.resetForNewRound();
        assertNotEquals(Card.Rank.WILD,           s.upCard.getRank());
        assertNotEquals(Card.Rank.WILD_DRAW_FOUR, s.upCard.getRank());
    }

    @Test void deckHas108Cards() {
        GameState s = makeState(2);
        s.resetForNewRound();
        List<Card> all = new ArrayList<>();
        all.addAll(s.deck);
        all.addAll(s.discard);
        for (int i = 0; i < s.playerCount(); i++) all.addAll(s.handOf(i));
        all.add(s.upCard);
        assertEquals(108, all.size());
    }

    @Test void deckCompositionNumbers() {
        GameState s = makeState(2);
        s.resetForNewRound();
        List<Card> all = new ArrayList<>();
        all.addAll(s.deck); all.addAll(s.discard);
        for (int i = 0; i < s.playerCount(); i++) all.addAll(s.handOf(i));
        all.add(s.upCard);
        long numbers = all.stream().filter(c -> c.getRank() == Card.Rank.NUMBER).count();
        assertEquals(76, numbers);
    }

    @Test void deckCompositionActions() {
        GameState s = makeState(2);
        s.resetForNewRound();
        List<Card> all = new ArrayList<>();
        all.addAll(s.deck); all.addAll(s.discard);
        for (int i = 0; i < s.playerCount(); i++) all.addAll(s.handOf(i));
        all.add(s.upCard);
        long actions = all.stream().filter(c ->
                c.getRank() == Card.Rank.SKIP ||
                        c.getRank() == Card.Rank.REVERSE ||
                        c.getRank() == Card.Rank.DRAW_TWO).count();
        assertEquals(24, actions);
    }

    @Test void deckCompositionWilds() {
        GameState s = makeState(2);
        s.resetForNewRound();
        List<Card> all = new ArrayList<>();
        all.addAll(s.deck); all.addAll(s.discard);
        for (int i = 0; i < s.playerCount(); i++) all.addAll(s.handOf(i));
        all.add(s.upCard);
        long wilds = all.stream().filter(c ->
                c.getRank() == Card.Rank.WILD ||
                        c.getRank() == Card.Rank.WILD_DRAW_FOUR).count();
        assertEquals(8, wilds);
    }

    @Test void advanceTurnWrapsForward() {
        GameState s = makeState(3);
        s.resetForNewRound();
        s.currentPlayer = 2;
        s.direction = 1;
        s.advanceTurn();
        assertEquals(0, s.currentPlayer);
    }

    @Test void advanceTurnWrapsBackward() {
        GameState s = makeState(3);
        s.resetForNewRound();
        s.currentPlayer = 0;
        s.direction = -1;
        s.advanceTurn();
        assertEquals(2, s.currentPlayer);
    }

    @Test void drawFromEmptyDeckUsesDiscard() {
        GameState s = makeState(2);
        s.resetForNewRound();
        s.deck.clear();
        s.discard.add(Card.of("G7"));
        Card drawn = s.drawFromDeck();
        assertEquals("G7", drawn.getCode());
        assertTrue(s.discard.isEmpty());
    }

    @Test void drawFallbackWhenBothEmpty() {
        GameState s = makeState(2);
        s.resetForNewRound();
        s.deck.clear();
        s.discard.clear();
        assertEquals("W", s.drawFromDeck().getCode());
    }

    @Test void scoreAccumulates() {
        GameState s = makeState(2);
        s.resetForNewRound();
        assertEquals(0, s.scoreOf(0));
        s.addScore(0, 42);
        assertEquals(42, s.scoreOf(0));
        s.addScore(0, 8);
        assertEquals(50, s.scoreOf(0));
    }

    @Test void scoresPersistedAcrossReset() {
        GameState s = makeState(3);
        s.resetForNewRound();
        s.addScore(0, 25);
        s.addScore(1, 15);
        s.resetForNewRound();
        assertEquals(25, s.scoreOf(0));
        assertEquals(15, s.scoreOf(1));
    }

    @Test void handsRedealtAfterReset() {
        GameState s = makeState(3);
        s.resetForNewRound();
        s.resetForNewRound();
        assertEquals(7, s.handOf(0).size());
        assertEquals(7, s.handOf(1).size());
        assertEquals(7, s.handOf(2).size());
    }
}