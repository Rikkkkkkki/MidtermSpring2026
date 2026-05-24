import java.util.List;

/**
 * Bot card-selection and color-selection strategy.
 *
 * Extracted from chooseBotCard and chooseBotColor in the original Main.
 * The priority order is preserved exactly:
 *   1. Draw Two cards (legal ones first)
 *   2. Skip cards (legal ones)
 *   3. Number cards (legal ones)
 *   4. Any wild
 *
 * Bot color selection picks the color most represented in the bot's hand,
 * with the original tie-breaking order (R > Y > G > B).
 *
 * Having strategy in its own class means a smarter bot can be dropped in
 * without touching GameEngine or Rules.
 */
public class BotStrategy {

    private BotStrategy() {}

    /**
     * Choose the index of a card for the bot to play, or -1 to draw.
     *
     * @param hand        the bot's current hand
     * @param upCard      the current up card on the table
     * @param calledColor the color called after the last wild, or empty string
     * @return hand index to play, or -1 to draw
     */
    public static int chooseCard(List<Card> hand, Card upCard, String calledColor) {
        // 1. Prefer Draw Two.
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.getRank() == Card.Rank.DRAW_TWO && Rules.isLegal(card, upCard, calledColor)) {
                return i;
            }
        }
        // 2. Prefer Skip.
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.getRank() == Card.Rank.SKIP && Rules.isLegal(card, upCard, calledColor)) {
                return i;
            }
        }
        // 3. Prefer any other legal non-wild.
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.getRank() == Card.Rank.NUMBER && Rules.isLegal(card, upCard, calledColor)) {
                return i;
            }
        }
        // 4. Play wild as last resort.
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getRank() == Card.Rank.WILD
                    || hand.get(i).getRank() == Card.Rank.WILD_DRAW_FOUR) {
                return i;
            }
        }
        // Nothing playable; signal draw.
        return -1;
    }

    /**
     * Choose the color a bot calls after playing a wild.
     * Picks whichever color appears most in the bot's remaining hand.
     *
     * @param hand the bot's hand after the wild has been removed
     * @return one of "R","Y","G","B"
     */
    public static String chooseColor(List<Card> hand) {
        return Rules.dominantColor(hand);
    }
}