import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class GameRecordRepository {
    private static EntityManagerFactory emf;

    private static EntityManagerFactory getEMF() {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory("UnoGameUnit");
        }
        return emf;
    }

    public void save(GameRecord record) {
        EntityManager em = getEMF().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(record);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void saveAll(List<GameRecord> records) {
        for (GameRecord record : records) {
            save(record);
        }
    }

    public List<GameRecord> findAll() {
        EntityManager em = getEMF().createEntityManager();
        try {
            return em.createQuery("SELECT g FROM GameRecord g ORDER BY g.timestamp DESC",
                            GameRecord.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<GameRecord> findByPlayerName(String playerName) {
        EntityManager em = getEMF().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT g FROM GameRecord g WHERE g.player.name = :name ORDER BY g.timestamp DESC",
                            GameRecord.class)
                    .setParameter("name", playerName)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public static void closeFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    public GameRecord getHighestScore() {
        EntityManager em = getEMF().createEntityManager();
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

    public long getWinCount(String playerName) {
        EntityManager em = getEMF().createEntityManager();
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
}