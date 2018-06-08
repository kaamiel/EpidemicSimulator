import java.util.List;

public abstract class Agent {

    private Health health;
    private List<Agent> friends;

    public Agent (Health health, List<Agent> friends) {
        this.health = health;
        this.friends = friends;

//        List<Agent> tmp = new ArrayList<>();
//        Collections.copy(tmp, friends);
//        this.friends = tmp;
    }

    @Override
    public String toString() {
        return health.toString() + " " + friends.toString();
    }
}
