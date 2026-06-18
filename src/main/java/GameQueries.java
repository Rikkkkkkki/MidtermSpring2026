import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class GameQueries {
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("UnoGameUnit");

    /**
     * Query 1: Recent Games (last N games)
     */
    public static List<GameRecord> getRecentGames(int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT g FROM GameRecord g ORDER BY g.timestamp DESC",
                            GameRecord.class)
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Query 2: Player Win Count
     */
    public static long getPlayerWinCount(String playerName) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(g) FROM GameRecord g WHERE g.player.name = :name AND g.winner = true",
                            Long.class)
                    .setParameter("name", playerName)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Query 3: Highest Score (by player)
     */
    public static GameRecord getHighestScore() {
        EntityManager em = emf.createEntityManager();
        try {
            List<GameRecord> results = em.createQuery(
                            "SELECT g FROM GameRecord g ORDER BY g.finalScore DESC",
                            GameRecord.class)
                    .setMaxResults(1)
                    .getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * Bonus Query: Player Statistics
     */
    public static void printPlayerStats(String playerName) {
        long wins = getPlayerWinCount(playerName);
        GameRecordRepository repo = new GameRecordRepository();
        List<GameRecord> games = repo.findByPlayerName(playerName);

        if (games.isEmpty()) {
            System.out.println("No games found for player: " + playerName);
            return;
        }

        int totalGames = games.size();
        int totalScore = games.stream().mapToInt(GameRecord::getFinalScore).sum();
        int avgScore = totalScore / totalGames;

        System.out.println("\n=== Player Stats: " + playerName + " ===");
        System.out.println("Total Games: " + totalGames);
        System.out.println("Wins: " + wins);
        System.out.println("Win Rate: " + (wins * 100 / totalGames) + "%");
        System.out.println("Total Score: " + totalScore);
        System.out.println("Average Score: " + avgScore);
    }

    public static void closeFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}