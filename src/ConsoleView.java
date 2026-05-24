import java.util.List;
import java.util.Scanner;

/**
 * Console rendering and human input for the UNO CLI.
 *
 * All System.out output and Scanner reads from the original Main are
 * consolidated here.  GameEngine calls this view for display and for
 * prompting the human player; it never writes to System.out directly.
 *
 * This boundary makes it straightforward to replace the CLI display
 * (e.g., with a GUI or a test double) without touching game logic.
 *
 * Prompt wording and output format are preserved exactly from the original
 * so characterized integration behavior is not broken.
 */
public class ConsoleView {

    private final boolean quiet;
    private final Scanner scanner;

    public ConsoleView(boolean quiet, Scanner scanner) {
        this.quiet   = quiet;
        this.scanner = scanner;
    }

    // ── Game-level output ──────────────────────────────────────────────────

    public void showGameBanner(int gameNumber) {
        if (!quiet) System.out.println("\n=== Game " + gameNumber + " ===");
    }

    public void showFinalScores(List<String> names, int[] scores) {
        System.out.println("\nFinal scores:");
        for (int i = 0; i < names.size(); i++) {
            System.out.println(names.get(i) + ": " + scores[i]);
        }
    }

    public void showTooFewOrTooManyPlayers() {
        System.out.println("UNO needs 2 to 4 players.");
    }

    public void showHelp() {
        System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
    }

    // ── Turn output ────────────────────────────────────────────────────────

    public void showTurnHeader(String playerName, Card upCard, String calledColor, List<Card> hand) {
        if (quiet) return;
        String colorSuffix = calledColor.isEmpty() ? "" : " called " + calledColor;
        System.out.println("\nUp card: " + upCard.getCode() + colorSuffix);
        System.out.println(playerName + " hand: " + formatHand(hand));
    }

    public void showDraw(String playerName, Card drawn) {
        if (!quiet) System.out.println(playerName + " draws " + drawn.getCode());
    }

    public void showPlays(String playerName, Card card) {
        if (!quiet) System.out.println(playerName + " plays " + card.getCode());
    }

    public void showCallsColor(String playerName, String color) {
        if (!quiet) System.out.println(playerName + " calls " + color);
    }

    public void showUno(String playerName) {
        if (!quiet) System.out.println(playerName + " says UNO!");
    }

    public void showWins(String playerName, int points) {
        if (!quiet) System.out.println(playerName + " wins and scores " + points);
    }

    public void showPenaltyInvalidIndex(String playerName) {
        if (!quiet) System.out.println(playerName + " selected an invalid index and draws a penalty card.");
    }

    public void showPenaltyIllegalCard(String playerName, Card card) {
        if (!quiet) System.out.println(playerName + " tried illegal card " + card.getCode() + " and draws a penalty card.");
    }

    public void showDrawsTwo(String playerName) {
        if (!quiet) System.out.println(playerName + " draws two.");
    }

    public void showDrawsFour(String playerName) {
        if (!quiet) System.out.println(playerName + " draws four.");
    }

    public void showSafetyLimit() {
        if (!quiet) System.out.println("Game stopped at safety limit.");
    }

    // ── Human input prompts ────────────────────────────────────────────────

    /**
     * Ask the human player to choose a card index, card code, or "draw".
     * Returns -1 for draw, or the resolved hand index.
     *
     * Parsing and validation behavior preserved exactly from original askHuman().
     * Notable quirks kept:
     *   - Typing a numeric index for any card (legal or not) goes through
     *     without a legality check here; illegal plays are caught in GameEngine.
     *   - Typing a card code that is illegal prints "That card is not legal."
     *     and loops.
     *   - Typing an unrecognised string prints "Card not found." and loops.
     */
    public int promptHumanTurn(List<Card> hand, Card upCard, String calledColor) {
        while (true) {
            System.out.print("Choose card index/code or draw: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("DRAW")) {
                return -1;
            }
            // Try numeric index.
            try {
                int index = Integer.parseInt(input);
                if (index >= 0 && index < hand.size()) {
                    return index;
                }
                // Index out of range — fall through to "Card not found."
            } catch (NumberFormatException ignored) {
            }
            // Try card code match.
            boolean codeFound = false;
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).getCode().equals(input)) {
                    codeFound = true;
                    if (Rules.isLegal(hand.get(i), upCard, calledColor)) {
                        return i;
                    }
                    System.out.println("That card is not legal.");
                }
            }
            if (!codeFound) {
                System.out.println("Card not found.");
            }
        }
    }

    /**
     * Ask the human to pick R/Y/G/B after playing a wild.
     * Loops until a valid color letter is entered, same as original askColor().
     */
    public String promptHumanColor() {
        while (true) {
            System.out.print("Call color R/Y/G/B: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("R") || input.equals("Y") || input.equals("G") || input.equals("B")) {
                return input;
            }
            System.out.println("Bad color.");
        }
    }

    /**
     * After a human draws a card, ask whether they want to play it.
     * Returns true when the player says yes.
     */
    public boolean promptPlayDrawnCard(Card drawn) {
        System.out.print("Play drawn card " + drawn.getCode() + "? y/n: ");
        String answer = scanner.nextLine();
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /** Format a hand as "0:R5 1:G3 …" matching the original join() output. */
    public static String formatHand(List<Card> hand) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hand.size(); i++) {
            if (i > 0) sb.append(' ');
            sb.append(i).append(':').append(hand.get(i).getCode());
        }
        return sb.toString();
    }
}