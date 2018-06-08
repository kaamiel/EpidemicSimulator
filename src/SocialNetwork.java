import java.util.List;
import java.util.Properties;
import java.util.Random;

public class SocialNetwork {

    private List<Agent> agents;
    private int numberOfAgents;

    private void initRandomly(Properties p) {
        this.numberOfAgents = Integer.valueOf(p.getProperty("liczbaAgentów"));

//        Random random = new Random(Integer.valueOf(p.getProperty("seed")));
//
//        int k = random.nextInt();/////////////////
    }

    public SocialNetwork(Properties p) {
        initRandomly(p);
    }
}
