import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_records")
public class GameRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private Integer roundsPlayed;

    @Column(nullable = false)
    private Integer finalScore;

    @Column(nullable = false)
    private Boolean winner;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public GameRecord() {}

    public GameRecord(Player player, Integer roundsPlayed, Integer finalScore, Boolean winner) {
        this.player = player;
        this.roundsPlayed = roundsPlayed;
        this.finalScore = finalScore;
        this.winner = winner;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Integer getRoundsPlayed() {
        return roundsPlayed;
    }

    public void setRoundsPlayed(Integer roundsPlayed) {
        this.roundsPlayed = roundsPlayed;
    }

    public Integer getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(Integer finalScore) {
        this.finalScore = finalScore;
    }

    public Boolean getWinner() {
        return winner;
    }

    public void setWinner(Boolean winner) {
        this.winner = winner;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "GameRecord{" + "player=" + player.getName() + ", score=" + finalScore +
                ", winner=" + winner + ", timestamp=" + timestamp + '}';
    }
}