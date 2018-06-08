import java.util.List;
import java.util.Properties;
import java.util.Random;

public class StandardAgent extends Agent {

    public StandardAgent(int id, Health health, List<Agent> friends, List<Appointment> appointments) {
        super(id, health, friends, appointments);
    }

    @Override
    public void makeAppointments(Properties properties, Random random, int currentDay) {
        if (friends.isEmpty()) {
            return;
        }

        double probability = Double.parseDouble(properties.getProperty("prawdSpotkania"));
        if (this.health == Health.INFECTED) {
            probability /= 2.0;
        }

        boolean toMakeOrNotToMake = random.nextDouble() <= probability;

        int numberOfCandidates = friends.size();

        while (toMakeOrNotToMake) {
            Agent agent = friends.get(random.nextInt(numberOfCandidates));
            int day = currentDay + 1 +
                    random.nextInt(Integer.parseInt(properties.getProperty("liczbaDni")) - currentDay);

            appointments.add(new Appointment(day, agent));

            toMakeOrNotToMake = random.nextDouble() <= probability;
        }
    }


    @Override
    public String toString() {
        return this.id + (this.health == Health.INFECTED ? "* " : " ") + "zwykły";
    }
}
