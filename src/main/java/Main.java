import java.time.LocalDateTime;
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
 * Logging is configured on startup to capture game events.
 *
 * Report commands (exit after printing, no game started):
 *   --report-recent          List the 10 most recent game sessions
 *   --report-wins <name>     Show win count for a named player
 *   --report-top             Show the player with the highest single-game score
 */
public class Main {

    private static final Logger logger = Logger.getLogger("com.uno");

    public static void main(String[] args) {
        configureLogging();

        int     bots  = 3;
        int     games = 1;
        boolean human = false;
        boolean quiet = false;
        long    seed  = System.currentTimeMillis();

        for (int i = 0; i < args.length; i++) {
            if      (args[i].equals("--bots")   && i + 1 < args.length) bots  = Integer.parseInt(args[++i]);
            else if (args[i].equals("--games")  && i + 1 < args.length) games = Integer.parseInt(args[++i]);
            else if (args[i].equals("--human"))  human = true;
            else if (args[i].equals("--quiet"))  quiet = true;
            else if (args[i].equals("--seed")   && i + 1 < args.length) seed  = Long.parseLong(args[++i]);
            else if (args[i].equals("--report-recent")) {
                printRecentGames();
                return;
            }
            else if (args[i].equals("--report-wins") && i + 1 < args.length) {
                printWins(args[++i]);
                return;
            }
            else if (args[i].equals("--report-top")) {
                printTopScore();
                return;
            }
            else if (args[i].equals("--help")) {
                System.out.println("Usage: java -jar uno-cli.jar [--bots N] [--games N] [--human] [--quiet] [--seed N]");
                System.out.println("       java -jar uno-cli.jar --report-recent");
                System.out.println("       java -jar uno-cli.jar --report-wins <playerName>");
                System.out.println("       java -jar uno-cli.jar --report-top");
                return;
            }
        }

        Random      random = new Random(seed);
        Scanner     sc     = new Scanner(System.in);
        ConsoleView view   = new ConsoleView(quiet, sc);

        logger.info("=== UNO Game Starting ===");
        logger.info("Seed: " + seed + ", Games: " + games + ", Bots: " + bots + ", Human: " + human);

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

        LocalDateTime sessionStart = LocalDateTime.now();

        for (int g = 1; g <= games; g++) {
            view.showGameBanner(g);
            logger.info("Starting game " + g + " of " + games);
            engine.playRound();
        }

        // Persist one session record covering all rounds played.
        persistGameResult(state.playerNames, state.scores,
                state.lastWinner, games, sessionStart);

        view.showFinalScores(state.playerNames, state.scores);
        logger.info("=== UNO Game Complete ===");

        sc.close();
    }

    // ── Persistence ────────────────────────────────────────────────────────

    private static void persistGameResult(List<String> playerNames, int[] scores,
                                          int winnerIndex, int roundsPlayed,
                                          LocalDateTime startTime) {
        PlayerRepository      playerRepo  = new PlayerRepository();
        GameSessionRepository sessionRepo = new GameSessionRepository();
        GameRecordRepository  gameRepo    = new GameRecordRepository();

        GameSession session = new GameSession(startTime);
        session.complete(roundsPlayed);
        sessionRepo.save(session);

        for (int i = 0; i < playerNames.size(); i++) {
            Player     player = playerRepo.getOrCreatePlayer(playerNames.get(i));
            GameRecord record = new GameRecord(session, player, scores[i], i == winnerIndex);
            gameRepo.save(record);
        }
        logger.info("Game results persisted to database");
    }

    // ── Report commands ────────────────────────────────────────────────────

    private static void printRecentGames() {
        GameSessionRepository repo     = new GameSessionRepository();
        List<GameSession>     sessions = repo.findRecent(10);
        System.out.println("\n=== Recent Games ===");
        if (sessions.isEmpty()) {
            System.out.println("No games recorded yet.");
            return;
        }
        for (GameSession s : sessions) {
            System.out.println("Session #" + s.getId()
                    + " | Rounds: "  + s.getRoundsPlayed()
                    + " | Started: " + s.getStartTime()
                    + " | Ended: "   + s.getEndTime());
            for (GameRecord r : s.getRecords()) {
                System.out.println("  " + r.getPlayer().getName()
                        + ": " + r.getFinalScore()
                        + (r.getWinner() ? " [WINNER]" : ""));
            }
        }
    }

    private static void printWins(String name) {
        GameRecordRepository repo = new GameRecordRepository();
        long wins = repo.getWinCount(name);
        System.out.println(name + " has " + wins + " win(s).");
    }

    private static void printTopScore() {
        GameRecordRepository repo = new GameRecordRepository();
        GameRecord top = repo.getHighestScore();
        if (top == null) {
            System.out.println("No games recorded yet.");
            return;
        }
        System.out.println("Top score: " + top.getPlayer().getName()
                + " with " + top.getFinalScore() + " points.");
    }

    // ── Logging setup ──────────────────────────────────────────────────────

    private static void configureLogging() {
        try {
            LogManager.getLogManager().readConfiguration(
                    Main.class.getResourceAsStream("/logging.properties"));
        } catch (Exception e) {
            java.util.logging.ConsoleHandler handler = new java.util.logging.ConsoleHandler();
            handler.setLevel(java.util.logging.Level.INFO);
            Logger rootLogger = LogManager.getLogManager().getLogger("");
            rootLogger.addHandler(handler);
            rootLogger.setLevel(java.util.logging.Level.INFO);
        }
    }
}