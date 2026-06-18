import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;
import java.util.Optional;

public class PlayerRepository {
    private static EntityManagerFactory emf;

    private static EntityManagerFactory getEMF() {
        if (emf == null || !emf.isOpen()) {
            emf = Persistence.createEntityManagerFactory("UnoGameUnit");
        }
        return emf;
    }

    public void save(Player player) {
        EntityManager em = getEMF().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(player);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public Optional<Player> findByName(String name) {
        EntityManager em = getEMF().createEntityManager();
        try {
            Player player = em.createQuery(
                            "SELECT p FROM Player p WHERE p.name = :name", Player.class)
                    .setParameter("name", name)
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(player);
        } finally {
            em.close();
        }
    }

    public List<Player> findAll() {
        EntityManager em = getEMF().createEntityManager();
        try {
            return em.createQuery("SELECT p FROM Player p", Player.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Player getOrCreatePlayer(String name) {
        Optional<Player> existing = findByName(name);
        if (existing.isPresent()) {
            return existing.get();
        }
        Player newPlayer = new Player(name);
        save(newPlayer);
        return newPlayer;
    }

    public static void closeFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}