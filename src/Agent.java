import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public abstract class Agent {

    protected int id;
    protected Health health;
    protected List<Agent> friends;
    protected List<Appointment> appointments; // i invited them
    protected List<Appointment> invitations; // they invited me

    public Agent(int id, Health health, List<Agent> friends, List<Appointment> appointments, List<Appointment> invitations) {
        this.id = id;
        this.health = health;
        this.friends = friends;
        this.appointments = appointments;
        this.invitations = invitations;
    }

    public int getId() {
        return this.id;
    }

    public Health getHealth() {
        return this.health;
    }

    public List<Agent> getFriends() {
        return this.friends;
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

        for (Appointment invitation : invitations) {
            // i am invited
            invitation.getInviter().appointments.remove(invitation);
        }
    }

    public boolean die(Properties properties, Random random) {
        if (this.health != Health.INFECTED) {
            return false;
        }

        boolean toDieOrNotToDie = random.nextDouble() <= Double.parseDouble(properties.getProperty("śmiertelność"));

        if (toDieOrNotToDie) {
            tellFriendsYoureDead();
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

    private void meetFriend(Properties properties, Random random, Appointment appointment) {
        double probability = Double.parseDouble(properties.getProperty("prawdZarażenia"));

        // i am inviter
        Agent friend = appointment.getInvited();

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

        friend.invitations.remove(appointment);
    }

    public void meetFriends(Properties properties, Random random, int currentDay) {
        this.appointments.sort(Appointment::compareTo);

        List<Appointment> toRemove = new ArrayList<>();

        for (Appointment appointment : this.appointments) {
            if (appointment.getDay() > currentDay) {
                break;
            }

            meetFriend(properties, random, appointment);
            toRemove.add(appointment);
        }

        this.appointments.removeAll(toRemove);

    }
}
