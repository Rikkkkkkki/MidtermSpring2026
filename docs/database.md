# UNO Game Persistence Documentation

## Database

- **Type**: H2 Embedded Database
- **Location**: `./unodata.h2.db` (created automatically)
- **No external setup required** - works out of the box

## ORM Framework

- **Framework**: JPA/Hibernate
- **Config**: `src/main/resources/META-INF/persistence.xml`

## Schema

### Players Table
```sql
CREATE TABLE players (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);
```

### Game Records Table
```sql
CREATE TABLE game_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT NOT NULL,
    rounds_played INT NOT NULL,
    final_score INT NOT NULL,
    winner BOOLEAN NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    FOREIGN KEY (player_id) REFERENCES players(id)
);
```

## Running Tests

```bash
mvn test
```

Tests use an isolated in-memory H2 database.

## Querying Game History

```bash
# View recent 10 games
java -jar target/uno-cli.jar --stats recent

# View player wins
java -jar target/uno-cli.jar --stats wins Player1

# View highest score
java -jar target/uno-cli.jar --stats highest
```

## Persistence Classes

- **Entities**: `Player.java`, `GameRecord.java`
- **Repositories**: `PlayerRepository.java`, `GameRecordRepository.java`
- **Queries**: `GameQueries.java`
- **Tests**: `GameRecordRepositoryTest.java`

## Notes

- All game results are automatically saved after each game
- No manual database configuration needed
- Database is in-memory for tests, H2 file-based for production