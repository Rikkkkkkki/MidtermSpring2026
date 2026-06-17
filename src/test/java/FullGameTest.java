import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Full-game runtime verification with deterministic behavior.
 *
 * Demonstrates end-to-end game flow with scoring behavior verified.
 * Uses fixed seed for reproducibility. Tests:
 *   - Complete multi-round game execution
 *   - Score accumulation across rounds
 *   - Game state transitions (reset between rounds)
 *   - Final state consistency
 *
 * These are higher-level round-flow checks that exercise the full game
 * without disturbing the separation of concerns in GameEngine, GameState,
 * Rules, BotStrategy, and ConsoleView.
 */
public class FullGameTest {

    public static void main(String[] args) {
        TestRunner r = new TestRunner("FullGameTest");

        // ── Full 3-round game with deterministic seed ────────────────────
        Random deterministicRandom = new Random(42);
        List<String> names = new ArrayList<>();
        List<Boolean> humanFlags = new ArrayList<>();
        names.add("Bot1");
        names.add("Bot2");
        names.add("Bot3");
        humanFlags.add(false);
        humanFlags.add(false);
        humanFlags.add(false);

        GameState state = new GameState(names, humanFlags, deterministicRandom);
        ConsoleView quietView = new ConsoleView(true, null);  // quiet mode, no Scanner needed
        GameEngine engine = new GameEngine(state, quietView);

        // ── Round 1 ────────────────────────────────────────────────────────
        int winner1 = engine.playRound();
        r.check(winner1 >= 0 && winner1 < 3, "Round 1 completes with valid winner");
        r.check(state.scoreOf(winner1) > 0, "Winner 1 scores positive points");
        int round1Total = state.scoreOf(0) + state.scoreOf(1) + state.scoreOf(2);
        r.check(round1Total > 0, "Total points awarded in round 1");

        // ── Round 2 ────────────────────────────────────────────────────────
        int winner2 = engine.playRound();
        r.check(winner2 >= 0 && winner2 < 3, "Round 2 completes with valid winner");
        // Scores should accumulate, not reset
        int round2Total = state.scoreOf(0) + state.scoreOf(1) + state.scoreOf(2);
        r.check(round2Total >= round1Total, "Scores accumulate across rounds");

        // ── Round 3 ────────────────────────────────────────────────────────
        int winner3 = engine.playRound();
        r.check(winner3 >= 0 && winner3 < 3, "Round 3 completes with valid winner");
        int round3Total = state.scoreOf(0) + state.scoreOf(1) + state.scoreOf(2);
        r.check(round3Total >= round2Total, "Scores continue to accumulate in round 3");

        // ── Verify final state consistency ──────────────────────────────────
        // After 3 rounds, at least one player should have non-zero score
        boolean anyScored = state.scoreOf(0) > 0 || state.scoreOf(1) > 0 || state.scoreOf(2) > 0;
        r.check(anyScored, "At least one player has non-zero score after 3 rounds");

        // Scores should never be negative
        r.check(state.scoreOf(0) >= 0 && state.scoreOf(1) >= 0 && state.scoreOf(2) >= 0,
                "All scores are non-negative");

        // ── Deterministic behavior verification ─────────────────────────────
        // Run the same game again with same seed; should get same winners
        Random deterministicRandom2 = new Random(42);
        GameState state2 = new GameState(names, humanFlags, deterministicRandom2);
        GameEngine engine2 = new GameEngine(state2, quietView);

        int rerun1 = engine2.playRound();
        int rerun2 = engine2.playRound();
        int rerun3 = engine2.playRound();

        r.check(rerun1 == winner1, "Deterministic seed produces same winner in round 1");
        r.check(rerun2 == winner2, "Deterministic seed produces same winner in round 2");
        r.check(rerun3 == winner3, "Deterministic seed produces same winner in round 3");

        r.check(state2.scoreOf(0) == state.scoreOf(0) &&
                        state2.scoreOf(1) == state.scoreOf(1) &&
                        state2.scoreOf(2) == state.scoreOf(2),
                "Deterministic seed produces identical final scores");

        // ── Round-flow state transition check ───────────────────────────────
        // Verify that hands are dealt properly after each reset
        GameState flowTest = new GameState(names, humanFlags, new Random(123));
        flowTest.resetForNewRound();
        int totalCards1 = flowTest.handOf(0).size() + flowTest.handOf(1).size() +
                flowTest.handOf(2).size() + 1;  // +1 for upCard
        r.check(totalCards1 == 22, "After reset: 3 × 7 + 1 up-card = 22 cards in play");

        // Play until someone wins
        for (int i = 0; i < 3000; i++) {
            if (flowTest.handOf(flowTest.currentPlayer).isEmpty()) {
                break;
            }
            // Simulate minimal play (just advance)
            flowTest.advanceTurn();
        }

        // Second round resets hands properly
        flowTest.resetForNewRound();
        int totalCards2 = flowTest.handOf(0).size() + flowTest.handOf(1).size() +
                flowTest.handOf(2).size() + 1;
        r.check(totalCards2 == 22, "After second reset: hands are re-dealt, still 22 cards in play");

        r.summary();
    }
}