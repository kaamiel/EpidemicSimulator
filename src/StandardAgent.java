import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class StandardAgent extends Agent {

    public StandardAgent(int id, Health health, List<Agent> friends, List<Appointment> appointments, List<Appointment> invitations) {
        super(id, health, friends, appointments, invitations);
    }

    public StandardAgent(int id, Health health) {
        this(id, health, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
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
            Agent invited = friends.get(random.nextInt(numberOfCandidates));
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
        return this.id + (this.health == Health.INFECTED ? "* " : " ") + "zwykły";
    }
}
