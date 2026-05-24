import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * CLI entry point for the UNO game.
 *
 * Kept thin: parse args, build collaborators, run rounds, print final scores.
 * All game logic lives in GameEngine, GameState, Rules, BotStrategy, Card.
 * All console output lives in ConsoleView.
 *
 * The --self-test flag runs all characterization test suites so the
 * scripts/test.sh script continues to work unchanged.
 */
public class Main {

    public static void main(String[] args) {
        int     bots  = 3;
        int     games = 1;
        boolean human = false;
        boolean quiet = false;
        long    seed  = System.currentTimeMillis();

        for (int i = 0; i < args.length; i++) {
            if      (args[i].equals("--bots")  && i + 1 < args.length) bots  = Integer.parseInt(args[++i]);
            else if (args[i].equals("--games") && i + 1 < args.length) games = Integer.parseInt(args[++i]);
            else if (args[i].equals("--human"))  human = true;
            else if (args[i].equals("--quiet"))  quiet = true;
            else if (args[i].equals("--seed")  && i + 1 < args.length) seed  = Long.parseLong(args[++i]);
            else if (args[i].equals("--self-test")) { selfTest(); return; }
            else if (args[i].equals("--help"))  {
                System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
                return;
            }
        }

        Random       random = new Random(seed);
        Scanner      sc     = new Scanner(System.in);
        ConsoleView  view   = new ConsoleView(quiet, sc);

        // Build player lists.
        List<String>  names      = new ArrayList<>();
        List<Boolean> humanFlags = new ArrayList<>();
        if (human) {
            names.add("You");
            humanFlags.add(Boolean.TRUE);
        }
        for (int i = 1; i <= bots; i++) {
            names.add("Bot" + i);
            humanFlags.add(Boolean.FALSE);
        }

        if (names.size() < 2 || names.size() > 4) {
            view.showTooFewOrTooManyPlayers();
            return;
        }

        GameState  state  = new GameState(names, humanFlags, random);
        GameEngine engine = new GameEngine(state, view);

        for (int g = 1; g <= games; g++) {
            view.showGameBanner(g);
            engine.playRound();
        }

        view.showFinalScores(state.playerNames, state.scores);
    }

    // ── Characterization self-test: runs all suites via scripts/test.sh ───────

    static void selfTest() {
        int suitesFailed = 0;

        suitesFailed += runSuite("CardTest",        () -> CardTest.main(new String[]{}));
        suitesFailed += runSuite("RulesTest",       () -> RulesTest.main(new String[]{}));
        suitesFailed += runSuite("GameStateTest",   () -> GameStateTest.main(new String[]{}));
        suitesFailed += runSuite("BotStrategyTest", () -> BotStrategyTest.main(new String[]{}));

        System.out.println("\n================================================");
        if (suitesFailed == 0) {
            System.out.println("ALL TEST SUITES PASSED");
        } else {
            System.out.println(suitesFailed + " SUITE(S) FAILED");
            throw new RuntimeException(suitesFailed + " test suite(s) failed.");
        }
    }

    private static int runSuite(String name, Runnable suite) {
        System.out.println("\n──── " + name + " ────");
        try {
            suite.run();
            return 0;
        } catch (Exception e) {
            System.out.println("SUITE FAILED: " + e.getMessage());
            return 1;
        }
    }
}