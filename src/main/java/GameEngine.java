import java.util.List;
import java.util.logging.Logger;

/**
 * Orchestrates a single round of UNO.
 *
 * Extracted from the giant game loop inside Main.playGame().
 * GameEngine owns the turn flow and delegates to:
 *   - GameState  for mutable data and deck operations
 *   - Rules      for legality checks and scoring
 *   - BotStrategy for bot card/color selection
 *   - ConsoleView for all output and human input
 *
 * No System.out calls appear here; that keeps rule logic independently testable.
 *
 * Card effect handling (skip, reverse, draw-two, wild, wild-draw-four) is
 * identical to the original Main so characterized behavior is preserved.
 *
 * Implements UNO call detection and penalty enforcement.
 */
public class GameEngine {

    private static final int SAFETY_LIMIT = 3000;
    private static final Logger logger = Logger.getLogger("com.uno");

    private final GameState    state;
    private final ConsoleView  view;

    public GameEngine(GameState state, ConsoleView view) {
        this.state = state;
        this.view  = view;
    }

    /**
     * Play one complete round. Returns the index of the winning player,
     * or -1 if the safety limit was reached.
     */
    public int playRound() {
        state.resetForNewRound();
        logger.info("Round started with players: " + String.join(", ", state.playerNames));

        for (int guard = 0; guard < SAFETY_LIMIT; guard++) {
            int player = state.currentPlayer;
            String name = state.nameOf(player);
            List<Card> hand = state.handOf(player);

            // ── UNO Penalty Check ──────────────────────────────────────────
            // If last player now has 1 card and didn't call UNO, penalize them
            checkAndApplyUnoPenalty(player);

            view.showTurnHeader(name, state.upCard, state.calledColor, hand);

            int chosen;
            if (state.humanAt(player)) {
                chosen = view.promptHumanTurn(hand, state.upCard, state.calledColor);
            } else {
                chosen = BotStrategy.chooseCard(hand, state.upCard, state.calledColor);
            }

            // ── Draw phase ─────────────────────────────────────────────────
            if (chosen == -1) {
                Card drawn = state.drawFromDeck();
                hand.add(drawn);
                view.showDraw(name, drawn);

                if (Rules.isLegal(drawn, state.upCard, state.calledColor)) {
                    if (!state.humanAt(player)) {
                        // Bots auto-play a drawn card when legal.
                        chosen = hand.size() - 1;
                    } else {
                        if (view.promptPlayDrawnCard(drawn)) {
                            chosen = hand.size() - 1;
                        }
                    }
                }
            }

            if (chosen < 0) {
                // Player drew and did not play; turn passes.
                state.advanceTurn();
                continue;
            }

            // ── Play-phase legality checks ────────────────────────────────
            if (chosen >= hand.size()) {
                // Invalid index — penalty card and turn loss (original quirk preserved).
                view.showPenaltyInvalidIndex(name);
                hand.add(state.drawFromDeck());
                state.advanceTurn();
                continue;
            }

            Card card = hand.get(chosen);
            if (!Rules.isLegal(card, state.upCard, state.calledColor)) {
                // Illegal card by index — penalty card and turn loss (original quirk preserved).
                view.showPenaltyIllegalCard(name, card);
                hand.add(state.drawFromDeck());
                state.advanceTurn();
                continue;
            }

            // ── Commit the play ────────────────────────────────────────────
            hand.remove(chosen);
            state.discard.add(state.upCard);
            state.upCard     = card;
            state.calledColor = "";
            view.showPlays(name, card);
            logger.info(name + " played " + card.getCode());

            // Reset UNO call for this player (they played, so UNO state resets)
            state.setUnoCall(player, false);

            // Color declaration for wilds.
            if (card.getRank() == Card.Rank.WILD || card.getRank() == Card.Rank.WILD_DRAW_FOUR) {
                if (state.humanAt(player)) {
                    state.calledColor = view.promptHumanColor();
                } else {
                    state.calledColor = BotStrategy.chooseColor(hand);
                }
                view.showCallsColor(name, state.calledColor);
            }

            // ── UNO announcement ───────────────────────────────────────────
            if (hand.size() == 1) {
                view.showUno(name);
                state.setUnoCall(player, true);
            }

            // ── Win check ──────────────────────────────────────────────────
            if (hand.isEmpty()) {
                int points = tallyOpponentPoints(player);
                state.addScore(player, points);
                state.lastWinner = player;
                view.showWins(name, points);
                logger.info(name + " wins round! Scored " + points + " points. Total: " + state.scoreOf(player));
                return player;
            }

            // ── Card effects ───────────────────────────────────────────────
            applyCardEffect(card, name);
        }

        view.showSafetyLimit();
        logger.warning("Round stopped at safety limit");
        return -1;
    }

    // ── Private helpers ────────────────────────────────────────────────────

    /**
     * Check if the current player has 1 card but didn't call UNO on their last turn.
     * If so, they draw 2 penalty cards (but keep their 1 card).
     * This implements the rule: if you reach 1 card and don't announce UNO
     * before the next relevant action, you're penalized.
     */
    private void checkAndApplyUnoPenalty(int currentPlayer) {
        // Look at the previous player
        int prevPlayer = currentPlayer - state.direction;
        if (prevPlayer >= state.playerCount()) prevPlayer = 0;
        if (prevPlayer < 0) prevPlayer = state.playerCount() - 1;

        // If the previous player has 1 card but didn't call UNO, penalize
        if (state.handOf(prevPlayer).size() == 1 && !state.hasCalledUno(prevPlayer)) {
            String prevName = state.nameOf(prevPlayer);
            view.showUnoPenalty(prevName);
            state.handOf(prevPlayer).add(state.drawFromDeck());
            state.handOf(prevPlayer).add(state.drawFromDeck());
            logger.info(prevName + " missed UNO call and drew 2 penalty cards");
        }
    }

    /**
     * Apply the effect of the just-played card.
     * Mirrors the if-else chain from the original Main.playGame().
     */
    private void applyCardEffect(Card card, String currentPlayerName) {
        switch (card.getRank()) {
            case SKIP:
                state.advanceTurn();   // skip the next player
                state.advanceTurn();
                break;

            case REVERSE:
                state.direction *= -1;
                if (state.playerCount() == 2) {
                    // With 2 players, reverse acts as skip (original behavior).
                    state.advanceTurn();
                    state.advanceTurn();
                } else {
                    state.advanceTurn();
                }
                break;

            case DRAW_TWO:
                state.advanceTurn();
                state.handOf(state.currentPlayer).add(state.drawFromDeck());
                state.handOf(state.currentPlayer).add(state.drawFromDeck());
                view.showDrawsTwo(state.nameOf(state.currentPlayer));
                state.advanceTurn();
                break;

            case WILD_DRAW_FOUR:
                state.advanceTurn();
                for (int i = 0; i < 4; i++) {
                    state.handOf(state.currentPlayer).add(state.drawFromDeck());
                }
                view.showDrawsFour(state.nameOf(state.currentPlayer));
                state.advanceTurn();
                break;

            default:
                // WILD (color only), NUMBER, etc.
                state.advanceTurn();
                break;
        }
    }

    /** Sum the point value of every hand except the winner's. */
    private int tallyOpponentPoints(int winnerIndex) {
        int total = 0;
        for (int i = 0; i < state.playerCount(); i++) {
            if (i != winnerIndex) {
                total += Rules.scoreHand(state.handOf(i));
            }
        }
        return total;
    }
}