public class Appointment {

    private int day;
    private Agent inviter;
    private Agent invited;

    public Appointment(int day, Agent inviter, Agent invited) {
        this.day = day;
        this.inviter = inviter;
        this.invited = invited;
    }

    public int getDay() {
        return this.day;
    }

    public Agent getInviter() {
        return this.inviter;
    }

    public Agent getInvited() {
        return this.invited;
    }

    @Override
    public String toString() {
        return day + ": " + inviter + " -> " + invited;
    }
}
