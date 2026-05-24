/**
 * Immutable value object representing a single UNO card.
 *
 * Replaces the primitive String card representation from the original Main class.
 * All card knowledge (color, rank, number, points) lives here instead of being
 * scattered across static helper methods.
 *
 * String codes are preserved exactly as in the original so all existing
 * characterization behavior stays unchanged.
 */
public final class Card {

    public enum Rank {
        NUMBER, SKIP, REVERSE, DRAW_TWO, WILD, WILD_DRAW_FOUR
    }

    private final String code;
    private final String color;   // "R","Y","G","B", or "" for wilds
    private final Rank rank;
    private final int number;     // digit value for NUMBER rank, -1 otherwise

    private Card(String code, String color, Rank rank, int number) {
        this.code   = code;
        this.color  = color;
        this.rank   = rank;
        this.number = number;
    }

    /** Parse a raw card code string exactly as produced by the original deck builder. */
    public static Card of(String code) {
        if (code.equals("W")) {
            return new Card(code, "", Rank.WILD, -1);
        }
        if (code.equals("W4")) {
            return new Card(code, "", Rank.WILD_DRAW_FOUR, -1);
        }
        String c = String.valueOf(code.charAt(0));   // first char is color letter
        String rest = code.substring(1);
        if (rest.equals("S")) {
            return new Card(code, c, Rank.SKIP, -1);
        }
        if (rest.equals("R")) {
            return new Card(code, c, Rank.REVERSE, -1);
        }
        if (rest.equals("+2")) {
            return new Card(code, c, Rank.DRAW_TWO, -1);
        }
        // number card
        return new Card(code, c, Rank.NUMBER, Integer.parseInt(rest));
    }

    public String getCode()   { return code; }
    public String getColor()  { return color; }
    public Rank   getRank()   { return rank; }
    public int    getNumber() { return number; }

    /** Point value used for scoring at end of a round. */
    public int points() {
        switch (rank) {
            case NUMBER:         return number;
            case SKIP:
            case REVERSE:
            case DRAW_TWO:       return 20;
            case WILD:
            case WILD_DRAW_FOUR: return 50;
            default:             return 0;
        }
    }

    @Override public String toString() { return code; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;
        return code.equals(((Card) o).code);
    }

    @Override public int hashCode() { return code.hashCode(); }
}