/**
 * Stateless rule evaluator for the UNO variant implemented by this project.
 *
 * Extracted from the duplicated legality checks that appeared three times in
 * the original Main class (isLegal, chooseBotCard loop bodies, playGame loop body).
 * A single source of truth now lives here and can be tested without the CLI.
 *
 * Behavior is preserved exactly from the original.  Quirks documented in
 * rules.html are kept:
 *   - Wilds are always legal.
 *   - A card matching the called color after a wild is legal even if it does
 *     not match the up-card color.
 *   - Action cards match by action type (SKIP on SKIP, etc.).
 *   - Number cards match by digit value.
 */
public final class Rules {

    private Rules() {}

    /**
     * Return true when {@code card} may legally be played on top of {@code upCard}
     * given that {@code calledColor} is the color declared after the last wild
     * (empty string when no wild is active).
     *
     * This is the canonical legality check replacing the duplicated logic in
     * the original Main.
     */
    public static boolean isLegal(Card card, Card upCard, String calledColor) {
        // Wilds are always playable.
        if (card.getRank() == Card.Rank.WILD || card.getRank() == Card.Rank.WILD_DRAW_FOUR) {
            return true;
        }
        // Match the called color that follows a wild.
        if (!calledColor.isEmpty() && card.getColor().equals(calledColor)) {
            return true;
        }
        // Match by color.
        if (card.getColor().equals(upCard.getColor())) {
            return true;
        }
        // Match by action type (SKIP on SKIP, REVERSE on REVERSE, etc.).
        if (card.getRank() != Card.Rank.NUMBER && card.getRank() == upCard.getRank()) {
            return true;
        }
        // Match by number digit.
        if (card.getRank() == Card.Rank.NUMBER
                && upCard.getRank() == Card.Rank.NUMBER
                && card.getNumber() == upCard.getNumber()) {
            return true;
        }
        return false;
    }

    /**
     * Calculate the total point value of all cards in the given hand.
     * Used to score a winner's round after other players' hands are tallied.
     */
    public static int scoreHand(java.util.List<Card> hand) {
        int total = 0;
        for (Card c : hand) {
            total += c.points();
        }
        return total;
    }

    /**
     * Return the color code ("R","Y","G","B") that has the highest count in
     * the given hand, using the same tie-breaking order as the original.
     * Used by the bot color chooser.
     */
    public static String dominantColor(java.util.List<Card> hand) {
        int r = 0, y = 0, g = 0, b = 0;
        for (Card c : hand) {
            switch (c.getColor()) {
                case "R": r++; break;
                case "Y": y++; break;
                case "G": g++; break;
                case "B": b++; break;
            }
        }
        if (r >= y && r >= g && r >= b) return "R";
        if (y >= r && y >= g && y >= b) return "Y";
        if (g >= r && g >= y && g >= b) return "G";
        return "B";
    }
}