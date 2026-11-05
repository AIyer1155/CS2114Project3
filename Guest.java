public class Guest {
    private final String name;
    private final boolean player;

    public Guest(String name, boolean player) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        this.name = name;
        this.player = player;
    }

    public String getName() {
        return name;
    }

    public boolean isPlayer() {
        return player;
    }
}