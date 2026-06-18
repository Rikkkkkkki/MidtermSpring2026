import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "players")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<GameRecord> games = new ArrayList<>();

    public Player() {}

    public Player(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<GameRecord> getGames() {
        return games;
    }

    public void addGame(GameRecord game) {
        games.add(game);
    }

    @Override
    public String toString() {
        return "Player{" + "name='" + name + '\'' + '}';
    }
}