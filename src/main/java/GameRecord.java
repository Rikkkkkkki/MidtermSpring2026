import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_records")
public class GameRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private GameSession session;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private Integer finalScore;

    @Column(nullable = false)
    private Boolean winner;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public GameRecord() {}

    public GameRecord(GameSession session, Player player, Integer finalScore, Boolean winner) {
        this.session = session;
        this.player = player;
        this.finalScore = finalScore;
        this.winner = winner;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public GameSession getSession() { return session; }
    public void setSession(GameSession session) { this.session = session; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public Integer getFinalScore() { return finalScore; }
    public void setFinalScore(Integer finalScore) { this.finalScore = finalScore; }
    public Boolean getWinner() { return winner; }
    public void setWinner(Boolean winner) { this.winner = winner; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "GameRecord{player=" + player.getName() + ", score=" + finalScore +
                ", winner=" + winner + ", timestamp=" + timestamp + '}';
    }
}