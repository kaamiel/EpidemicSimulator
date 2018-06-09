import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class SocialNetwork {

    private List<Agent> agents; // alive agents
    private int numberOfAliveHealthyAgents;
    private int numberOfAliveInfectedAgents;
    private int numberOfAliveImmuneAgents;

    public SocialNetwork(Properties properties, Random random) {
        initRandomly(properties, random);
    }

    public List<Agent> getAgents() {
        return this.agents;
    }

    public void removeAgent(Agent agent) {
        this.agents.remove(agent);
    }

    public String numberOfAliveAgents() {
        return this.numberOfAliveHealthyAgents + " " + this.numberOfAliveInfectedAgents +
                " " + this.numberOfAliveImmuneAgents;
    }

    public void updateNumberOfAgents() {
        this.numberOfAliveHealthyAgents = 0;
        this.numberOfAliveInfectedAgents = 0;
        this.numberOfAliveImmuneAgents = 0;

        for (Agent agent : this.agents) {
            switch (agent.getHealth()) {
                case HEALTHY:
                    ++this.numberOfAliveHealthyAgents;
                    break;
                case INFECTED:
                    ++this.numberOfAliveInfectedAgents;
                    break;
                case IMMUNE:
                    ++this.numberOfAliveImmuneAgents;
                    break;
            }
        }
    }

    private void initRandomly(Properties properties, Random random) {
        int numberOfAgents = Integer.parseInt(properties.getProperty("liczbaAgentów"));

        this.numberOfAliveHealthyAgents = numberOfAgents - 1;
        this.numberOfAliveInfectedAgents = 1;
        this.numberOfAliveImmuneAgents = 0;

        List<Agent> l = new ArrayList<>();

        int infectedAgent = 1 + random.nextInt(numberOfAgents);

        for (int i = 1; i <= numberOfAgents; ++i) {
            Health health = (i == infectedAgent) ? Health.INFECTED : Health.HEALTHY;

            boolean isOutgoing = random.nextDouble() <= Double.parseDouble(properties.getProperty("prawdTowarzyski"));
            Agent agent = isOutgoing ? new OutgoingAgent(i, health) :
                    new StandardAgent(i, health);

            l.add(agent);
        }

        int edgesToAdd = (int) (Math.round((Double.parseDouble(properties.getProperty("śrZnajomych")) * numberOfAgents) / 2));
        while (edgesToAdd > 0) {
            Agent a1 = l.get(random.nextInt(numberOfAgents));
            Agent a2 = l.get(random.nextInt(numberOfAgents));

            if (a1 == a2) {
                continue;
            }

            if (a1.addFriend(a2)) {
                a2.addFriend(a1);
                --edgesToAdd;
            }
        }

        this.agents = l;
    }

    public String graph() {
        StringBuilder builder = new StringBuilder();

        for (Agent agent : this.agents) {
            builder.append(agent.getId());

            for (Agent friend : agent.getFriends()) {
               builder.append(" ").append(friend.getId());
            }

            builder.append('\n');
        }

        return builder.toString();
    }
}
