import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Mutable game state for one session of UNO.
 *
 * Extracts all global static fields from the original Main into a single
 * owned object.  The game loop in GameEngine operates on this state.
 * Keeping state here makes it possible to run multiple independent games
 * without the globals from the original interfering with each other.
 *
 * The deck composition, shuffle order, and initial deal are identical to
 * the original Main.playGame() so all characterized behavior is preserved.
 */
public class GameState {
    public int lastWinner = -1;
    // Player data
    final List<String>       playerNames  = new ArrayList<>();
    final List<Boolean>      isHuman      = new ArrayList<>();
    final List<List<Card>>   hands        = new ArrayList<>();
    final int[]              scores;

    // Table state
    final List<Card>         deck         = new ArrayList<>();
    final List<Card>         discard      = new ArrayList<>();
    Card                     upCard;
    String                   calledColor  = "";

    // Turn control
    int                      currentPlayer = 0;
    int                      direction     = 1;   //  1 = clockwise, -1 = counter

    private final Random     random;

    public GameState(List<String> names, List<Boolean> humanFlags, Random random) {
        this.random = random;
        this.scores = new int[names.size()];
        for (int i = 0; i < names.size(); i++) {
            playerNames.add(names.get(i));
            isHuman.add(humanFlags.get(i));
            hands.add(new ArrayList<>());
        }
    }

    /**
     * Reset the table for a new round, preserving accumulated scores.
     * Mirrors the deck-building and dealing logic from the original Main.playGame().
     */
    public void resetForNewRound() {
        deck.clear();
        discard.clear();
        for (List<Card> hand : hands) {
            hand.clear();
        }

        String[] colors = {"R", "Y", "G", "B"};
        for (String col : colors) {
            deck.add(Card.of(col + "0"));
            for (int n = 1; n <= 9; n++) {
                deck.add(Card.of(col + n));
                deck.add(Card.of(col + n));
            }
            deck.add(Card.of(col + "S"));
            deck.add(Card.of(col + "S"));
            deck.add(Card.of(col + "R"));
            deck.add(Card.of(col + "R"));
            deck.add(Card.of(col + "+2"));
            deck.add(Card.of(col + "+2"));
        }
        for (int i = 0; i < 4; i++) {
            deck.add(Card.of("W"));
            deck.add(Card.of("W4"));
        }
        Collections.shuffle(deck, random);

        // Deal 7 cards to each player.
        for (int i = 0; i < playerNames.size(); i++) {
            for (int j = 0; j < 7; j++) {
                hands.get(i).add(drawFromDeck());
            }
        }

        // The up card must not start as a wild card.
        upCard = drawFromDeck();
        while (upCard.getRank() == Card.Rank.WILD || upCard.getRank() == Card.Rank.WILD_DRAW_FOUR) {
            discard.add(upCard);
            upCard = drawFromDeck();
        }

        calledColor   = "";
        direction     = 1;
        currentPlayer = random.nextInt(playerNames.size());
    }

    /**
     * Draw from the deck, reshuffling the discard pile into it when empty.
     * Falls back to a wild card if both piles are empty (safety behavior from original).
     */
    public Card drawFromDeck() {
        if (deck.isEmpty()) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.isEmpty()) {
            return Card.of("W");   // safety fallback, matches original
        }
        return deck.remove(0);
    }

    /** Advance currentPlayer by one step in the current direction. */
    public void advanceTurn() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) currentPlayer = 0;
        if (currentPlayer < 0)                   currentPlayer = playerNames.size() - 1;
    }

    public int playerCount()           { return playerNames.size(); }
    public String nameOf(int i)        { return playerNames.get(i); }
    public boolean humanAt(int i)      { return isHuman.get(i); }
    public List<Card> handOf(int i)    { return hands.get(i); }
    public int scoreOf(int i)          { return scores[i]; }
    public void addScore(int i, int n) { scores[i] += n; }
}