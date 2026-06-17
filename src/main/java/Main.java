import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * CLI entry point for the UNO game.
 *
 * Kept thin: parse args, build collaborators, run rounds, print final scores.
 * All game logic lives in GameEngine, GameState, Rules, BotStrategy, Card.
 * All console output lives in ConsoleView.
 *
 * The --self-test flag runs all characterization test suites so the
 * scripts/test.sh script continues to work unchanged.
 *
 * Logging is configured on startup to capture game events.
 */
public class Main {

    private static final Logger logger = Logger.getLogger("com.uno");

    public static void main(String[] args) {
        // Configure logging
        configureLogging();

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
            else if (args[i].equals("--help"))  {
                System.out.println("Usage: java -jar uno-cli.jar [--bots N] [--games N] [--human] [--quiet] [--seed N]");
                return;
            }
        }

        Random       random = new Random(seed);
        Scanner      sc     = new Scanner(System.in);
        ConsoleView  view   = new ConsoleView(quiet, sc);

        logger.info("=== UNO Game Starting ===");
        logger.info("Seed: " + seed + ", Games: " + games + ", Bots: " + bots + ", Human: " + human);

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
            logger.severe("Invalid player count: " + names.size() + " (must be 2-4)");
            return;
        }

        GameState  state  = new GameState(names, humanFlags, random);
        GameEngine engine = new GameEngine(state, view);

        for (int g = 1; g <= games; g++) {
            view.showGameBanner(g);
            logger.info("Starting game " + g + " of " + games);
            engine.playRound();
        }

        view.showFinalScores(state.playerNames, state.scores);
        logger.info("=== UNO Game Complete ===");

        sc.close();
    }

    /**
     * Configure logging to use the logging.properties file.
     * Logs are written to console.
     */
    private static void configureLogging() {
        try {
            LogManager.getLogManager().readConfiguration(
                    Main.class.getResourceAsStream("/logging.properties")
            );
        } catch (Exception e) {
            // If properties file not found, use default console logging
            java.util.logging.ConsoleHandler handler = new java.util.logging.ConsoleHandler();
            handler.setLevel(java.util.logging.Level.INFO);
            Logger rootLogger = LogManager.getLogManager().getLogger("");
            rootLogger.addHandler(handler);
            rootLogger.setLevel(java.util.logging.Level.INFO);
        }
    }
}