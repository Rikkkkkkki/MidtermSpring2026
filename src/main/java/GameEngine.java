import java.util.List;
import java.util.logging.Logger;

/**
 * Orchestrates a single round of UNO.
 */
public class GameEngine {

    private static final int SAFETY_LIMIT = 3000;
    private static final Logger logger = Logger.getLogger("com.uno");

    private final GameState   state;
    private final ConsoleView view;

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

            // LOG: player turn start
            logger.info(name + "'s turn. Hand size: " + hand.size()
                    + ". Up card: " + state.upCard.getCode()
                    + (state.calledColor.isEmpty() ? "" : " called " + state.calledColor));

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

                // LOG: card drawn
                logger.info(name + " draws " + drawn.getCode());
                view.showDraw(name, drawn);

                if (Rules.isLegal(drawn, state.upCard, state.calledColor)) {
                    if (!state.humanAt(player)) {
                        chosen = hand.size() - 1;
                    } else {
                        if (view.promptPlayDrawnCard(drawn)) {
                            chosen = hand.size() - 1;
                        }
                    }
                }
            }

            if (chosen < 0) {
                state.advanceTurn();
                continue;
            }

            // ── Play-phase legality checks ─────────────────────────────────
            if (chosen >= hand.size()) {
                // LOG: invalid index input
                logger.warning(name + " selected invalid index " + chosen
                        + " (hand size " + hand.size() + ") — penalty card issued");
                view.showPenaltyInvalidIndex(name);
                hand.add(state.drawFromDeck());
                state.advanceTurn();
                continue;
            }

            Card card = hand.get(chosen);
            if (!Rules.isLegal(card, state.upCard, state.calledColor)) {
                // LOG: illegal card attempt
                logger.warning(name + " attempted illegal card " + card.getCode()
                        + " on up card " + state.upCard.getCode()
                        + (state.calledColor.isEmpty() ? "" : " (called " + state.calledColor + ")")
                        + " — penalty card issued");
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

            // LOG: card played
            logger.info(name + " played " + card.getCode());

            // Color declaration for wilds.
            if (card.getRank() == Card.Rank.WILD || card.getRank() == Card.Rank.WILD_DRAW_FOUR) {
                if (state.humanAt(player)) {
                    state.calledColor = view.promptHumanColor();
                } else {
                    state.calledColor = BotStrategy.chooseColor(hand);
                }
                view.showCallsColor(name, state.calledColor);
                logger.info(name + " called color " + state.calledColor);
            }

            // UNO announcement.
            if (hand.size() == 1) {
                view.showUno(name);
                logger.info(name + " says UNO!");
            }

            // ── Win check ──────────────────────────────────────────────────
            if (hand.isEmpty()) {
                int points = tallyOpponentPoints(player);
                state.addScore(player, points);
                view.showWins(name, points);

                // LOG: round end
                logger.info(name + " wins round! Scored " + points
                        + " points. Total: " + state.scoreOf(player));
                return player;
            }

            // ── Card effects ───────────────────────────────────────────────
            applyCardEffect(card, name);
        }

        view.showSafetyLimit();
        logger.warning("Round stopped at safety limit");
        return -1;
    }

    private void applyCardEffect(Card card, String currentPlayerName) {
        switch (card.getRank()) {
            case SKIP:
                state.advanceTurn();
                state.advanceTurn();
                break;

            case REVERSE:
                state.direction *= -1;
                if (state.playerCount() == 2) {
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
                logger.info(state.nameOf(state.currentPlayer) + " draws two cards from Draw Two effect");
                state.advanceTurn();
                break;

            case WILD_DRAW_FOUR:
                state.advanceTurn();
                for (int i = 0; i < 4; i++) {
                    state.handOf(state.currentPlayer).add(state.drawFromDeck());
                }
                view.showDrawsFour(state.nameOf(state.currentPlayer));
                logger.info(state.nameOf(state.currentPlayer) + " draws four cards from Wild Draw Four effect");
                state.advanceTurn();
                break;

            default:
                state.advanceTurn();
                break;
        }
    }

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