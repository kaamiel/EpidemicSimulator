import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class OutgoingAgent extends Agent {

    public OutgoingAgent(int id, Health health, List<Agent> friends, List<Appointment> appointments, List<Appointment> invitations) {
        super(id, health, friends, appointments, invitations);
    }

    public OutgoingAgent(int id, Health health) {
        this(id, health, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    @Override
    public void makeAppointments(Properties properties, Random random, int currentDay) {
        if (friends.isEmpty()) {
            return;
        }

        double probability = Double.parseDouble(properties.getProperty("prawdSpotkania"));

        boolean toMakeOrNotToMake = random.nextDouble() <= probability;

        if (!toMakeOrNotToMake) {
            return;
        }

        List<Agent> candidates = new ArrayList<>(friends);
        if (this.health != Health.INFECTED) {
            for (Agent friend : friends) {
                for (Agent friendOfFriend : friend.friends) {
                    if ((friendOfFriend != this) && (!candidates.contains(friendOfFriend))) {
                        candidates.add(friendOfFriend);
                    }
                }
            }
        }

        int numberOfCandidates = candidates.size();

        while (toMakeOrNotToMake) {
            Agent invited = candidates.get(random.nextInt(numberOfCandidates));
            int day = currentDay + 1 +
                    random.nextInt(Integer.parseInt(properties.getProperty("liczbaDni")) - currentDay);

            Appointment appointment = new Appointment(day, this, invited);

            invited.invitations.add(appointment);
            this.appointments.add(appointment);

            toMakeOrNotToMake = random.nextDouble() <= probability;
        }
    }

    @Override
    public String toString() {
        return this.id + (this.health == Health.INFECTED ? "* " : " ")  + "towarzyski";
    }
}
