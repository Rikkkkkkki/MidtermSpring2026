import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class GameSessionRepository {
    private static EntityManagerFactory emf;

    private static EntityManagerFactory getEMF() {
        if (emf == null || !emf.isOpen())
            emf = Persistence.createEntityManagerFactory("UnoGameUnit");
        return emf;
    }

    public void save(GameSession session) {
        EntityManager em = getEMF().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(session);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<GameSession> findRecent(int limit) {
        EntityManager em = getEMF().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT s FROM GameSession s ORDER BY s.startTime DESC",
                            GameSession.class)
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public static void closeFactory() {
        if (emf != null && emf.isOpen()) emf.close();
    }
}