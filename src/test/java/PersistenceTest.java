import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Isolated persistence tests using in-memory H2.
 * Requires a "UnoTestUnit" persistence-unit in persistence.xml.
 */
public class PersistenceTest {

    public static void main(String[] args) {
        TestRunner r = new TestRunner("PersistenceTest");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("UnoTestUnit");
        EntityManager em = emf.createEntityManager();

        // ── Save and retrieve a Player ────────────────────────────────────
        em.getTransaction().begin();
        Player p = new Player("TestPlayer");
        em.persist(p);
        em.getTransaction().commit();

        Player found = em.createQuery(
                        "SELECT p FROM Player p WHERE p.name = :n", Player.class)
                .setParameter("n", "TestPlayer").getSingleResult();
        r.check(found.getName().equals("TestPlayer"), "Player persisted and retrieved");

        // ── Save a GameSession ────────────────────────────────────────────
        em.getTransaction().begin();
        GameSession session = new GameSession(LocalDateTime.now());
        session.complete(3);
        em.persist(session);
        em.getTransaction().commit();

        GameSession fs = em.find(GameSession.class, session.getId());
        r.check(fs.getRoundsPlayed() == 3, "GameSession roundsPlayed = 3");
        r.check(fs.getEndTime() != null, "GameSession endTime set");

        // ── Save a GameRecord linked to session ───────────────────────────
        em.getTransaction().begin();
        GameRecord rec = new GameRecord(session, found, 42, true);
        em.persist(rec);
        em.getTransaction().commit();

        List<GameRecord> records = em.createQuery(
                        "SELECT g FROM GameRecord g WHERE g.player.name = :n", GameRecord.class)
                .setParameter("n", "TestPlayer").getResultList();
        r.check(records.size() == 1, "One GameRecord saved for TestPlayer");
        r.check(records.get(0).getFinalScore() == 42, "GameRecord finalScore = 42");
        r.check(records.get(0).getWinner(), "GameRecord winner = true");

        // ── Win count query ───────────────────────────────────────────────
        long wins = em.createQuery(
                "SELECT COUNT(g) FROM GameRecord g WHERE g.player.name = :n AND g.winner = true",
                Long.class).setParameter("n", "TestPlayer").getSingleResult();
        r.check(wins == 1L, "Win count = 1");

        // ── Highest score query ───────────────────────────────────────────
        GameRecord top = em.createQuery(
                        "SELECT g FROM GameRecord g ORDER BY g.finalScore DESC", GameRecord.class)
                .setMaxResults(1).getResultList().get(0);
        r.check(top.getFinalScore() == 42, "Highest score = 42");

        em.close();
        emf.close();
        r.summary();
    }
}