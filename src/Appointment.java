public class Appointment implements Comparable<Appointment> {

    private int day;
    private Agent agent;

    public Appointment(int day, Agent agent) {
        this.day = day;
        this.agent = agent;
    }

    public int getDay() {
        return this.day;
    }

    public Agent getAgent() {
        return this.agent;
    }

    @Override
    public int compareTo(Appointment o) {
        return Integer.compare(this.day, o.day);
    }
}
