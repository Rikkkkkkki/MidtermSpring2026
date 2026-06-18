import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameRecordRepositoryTest {
    private PlayerRepository playerRepo;
    private GameRecordRepository gameRepo;

    @BeforeEach
    public void setUp() {
        playerRepo = new PlayerRepository();
        gameRepo = new GameRecordRepository();
    }

    @AfterEach
    public void tearDown() {
        PlayerRepository.closeFactory();
        GameRecordRepository.closeFactory();
    }

    @Test
    public void testSaveAndRetrievePlayer() {
        Player player = new Player("TestPlayer");
        playerRepo.save(player);

        assertTrue(playerRepo.findByName("TestPlayer").isPresent());
        assertEquals("TestPlayer", playerRepo.findByName("TestPlayer").get().getName());
    }

    @Test
    public void testSaveGameRecord() {
        Player player = new Player("Bot1");
        playerRepo.save(player);

        GameRecord record = new GameRecord(player, 1, 50, true);
        gameRepo.save(record);

        assertTrue(gameRepo.findAll().size() > 0);
    }

    @Test
    public void testFindGamesByPlayer() {
        Player player = new Player("TestBot");
        playerRepo.save(player);

        GameRecord record1 = new GameRecord(player, 1, 45, true);
        GameRecord record2 = new GameRecord(player, 2, 30, false);
        gameRepo.save(record1);
        gameRepo.save(record2);

        assertEquals(2, gameRepo.findByPlayerName("TestBot").size());
    }

    @Test
    public void testGetOrCreatePlayer() {
        Player player1 = playerRepo.getOrCreatePlayer("UniquePlayer");
        Player player2 = playerRepo.getOrCreatePlayer("UniquePlayer");

        assertEquals(player1.getName(), player2.getName());
    }
}