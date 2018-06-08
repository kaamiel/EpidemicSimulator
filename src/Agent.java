import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public abstract class Agent {

    protected int id;
    protected Health health;
    protected List<Agent> friends;
    protected List<Appointment> appointments;

    public Agent(int id, Health health, List<Agent> friends, List<Appointment> appointments) {
        this.id = id;
        this.health = health;
        this.friends = friends;
        this.appointments = appointments;
    }

    public int getId() {
        return this.id;
    }

    public Health getHealth() {
        return this.health;
    }

    public List<Agent> getFriends() {
        return friends;
    }

    public boolean addFriend(Agent agent) {
        if (!friends.contains(agent)) {
            friends.add(agent);
            return true;
        }
        return false;
    }

    private void tellFriendsYoureDead() {
        for (Agent friend : friends) {
            friend.friends.remove(this);
        }
    }

    public boolean die(Properties properties, Random random) {
        if (this.health != Health.INFECTED) {
            return false;
        }

        boolean toDieOrNotToDie = random.nextDouble() <= Double.parseDouble(properties.getProperty("śmiertelność"));

        if (toDieOrNotToDie) {
            tellFriendsYoureDead();
            this.health = Health.DEAD;
            return true;
        }
        return false;
    }

    public void recover(Properties properties, Random random) {
        if (this.health != Health.INFECTED) {
            return;
        }

        boolean toRecoverOrNotToRecover = random.nextDouble() <= Double.parseDouble(properties.getProperty("prawdWyzdrowienia"));

        if (toRecoverOrNotToRecover) {
            this.health = Health.IMMUNE;
        }
    }

    public abstract void makeAppointments(Properties properties, Random random, int currentDay);

    private void meetFriend(Properties properties, Random random, Agent friend) {
        if (friend.health == Health.DEAD) {
            return;
        }

        double probability = Double.parseDouble(properties.getProperty("prawdZarażenia"));

        if (this.health == Health.INFECTED && friend.health == Health.HEALTHY) {
            boolean toInfectOrNotToInfect = random.nextDouble() <= probability;

            if (toInfectOrNotToInfect) {
                friend.health = Health.INFECTED;
            }
        }

        if (this.health == Health.HEALTHY && friend.health == Health.INFECTED) {
            boolean toInfectOrNotToInfect = random.nextDouble() <= probability;

            if (toInfectOrNotToInfect) {
                this.health = Health.INFECTED;
            }
        }
    }

    public void meetFriends(Properties properties, Random random, int currentDay) {
        this.appointments.sort(Appointment::compareTo);

        List<Appointment> toRemove = new ArrayList<>();

        for (Appointment appointment : this.appointments) {
            if (appointment.getDay() > currentDay) {
                break;
            }

            meetFriend(properties, random, appointment.getAgent());
            toRemove.add(appointment);
        }

        for (Appointment appointment : toRemove) {
            this.appointments.remove(appointment);
        }
    }
}
