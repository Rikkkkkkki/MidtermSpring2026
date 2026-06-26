import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game_sessions")
public class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column(nullable = false)
    private Integer roundsPlayed;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<GameRecord> records = new ArrayList<>();

    public GameSession() {}

    public GameSession(LocalDateTime startTime) {
        this.startTime = startTime;
        this.roundsPlayed = 0;
    }

    public void complete(int rounds) {
        this.endTime = LocalDateTime.now();
        this.roundsPlayed = rounds;
    }

    public void addRecord(GameRecord r) { records.add(r); }

    public Long getId() { return id; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Integer getRoundsPlayed() { return roundsPlayed; }
    public List<GameRecord> getRecords() { return records; }
    public void setRoundsPlayed(Integer r) { this.roundsPlayed = r; }
    public void setEndTime(LocalDateTime t) { this.endTime = t; }
}