import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class GameRecordRepository {
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("UnoGameUnit");

    public void save(GameRecord record) {
        EntityManager em = emf.createEntityManager();
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
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT g FROM GameRecord g ORDER BY g.timestamp DESC",
                            GameRecord.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<GameRecord> findByPlayerName(String playerName) {
        EntityManager em = emf.createEntityManager();
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
}