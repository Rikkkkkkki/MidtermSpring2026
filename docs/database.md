# Database Documentation

## Database

- **Type**: H2 Embedded Database
- **Production file**: `./unodata.mv.db` (created automatically on first run)
- **Test database**: in-memory (`jdbc:h2:mem:unotest`) — no file, no setup required

## ORM Framework

- **Framework**: Hibernate 6 / Jakarta Persistence (JPA 3.0)
- **Config file**: `src/main/resources/META-INF/persistence.xml`
- **Persistence units**:
    - `UnoGameUnit` — file-based H2, used during normal gameplay
    - `UnoTestUnit` — in-memory H2, used by `PersistenceTest`

## Schema

Schema is created automatically by Hibernate (`hbm2ddl.auto`). The logical structure is:

### players
| Column | Type         | Notes          |
|--------|--------------|----------------|
| id     | BIGINT (PK)  | auto-generated |
| name   | VARCHAR(255) | unique         |

### game_sessions
| Column        | Type      | Notes                        |
|---------------|-----------|------------------------------|
| id            | BIGINT PK | auto-generated               |
| start_time    | TIMESTAMP | when the session began       |
| end_time      | TIMESTAMP | when the session completed   |
| rounds_played | INT       | number of rounds in session  |

### game_records
| Column      | Type      | Notes                              |
|-------------|-----------|------------------------------------|
| id          | BIGINT PK | auto-generated                     |
| session_id  | BIGINT FK | references game_sessions           |
| player_id   | BIGINT FK | references players                 |
| final_score | INT       | cumulative score for this session  |
| winner      | BOOLEAN   | true for the session winner        |
| timestamp   | TIMESTAMP | when this record was written       |

## Persistence Classes

| Class                    | Role                                      |
|--------------------------|-------------------------------------------|
| `Player.java`            | Entity: player identity                   |
| `GameSession.java`       | Entity: one completed game session        |
| `GameRecord.java`        | Entity: one player's result in a session  |
| `PlayerRepository.java`  | Save and look up players                  |
| `GameSessionRepository.java` | Save sessions, query recent games     |
| `GameRecordRepository.java`  | Save records, win count, top score    |

## Running Persistence Tests

```bash
javac -cp <classpath> PersistenceTest.java
java  -cp <classpath> PersistenceTest
```

`PersistenceTest` uses `UnoTestUnit` (in-memory H2) and is fully self-contained.
No database file, no external service, and no manual setup is needed.

## CLI Report Commands

Run these instead of starting a game:

```bash
# List the 10 most recent game sessions with per-player scores
java -jar uno-cli.jar --report-recent

# Show how many times a named player has won
java -jar uno-cli.jar --report-wins Bot1

# Show the player with the single highest recorded score
java -jar uno-cli.jar --report-top
```

## Notes

- `hbm2ddl.auto=update` is used in production so existing game history is never deleted between runs.
- `hbm2ddl.auto=create-drop` is used in tests so each test run starts with a clean schema.
- No database credentials are hardcoded beyond the default H2 embedded user (`sa` / empty password), which is H2's standard and requires no configuration.