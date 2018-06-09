import java.io.*;
import java.nio.channels.Channels;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Symulacja {

    public static void main(String[] args) {

        Properties defaultProperties = readDefaultProperties();
        Properties simulationConf = readSimulationConf();
        Properties properties = checkAndMerge(defaultProperties, simulationConf);
        //TODO: poprawić

        Random random = new Random(Integer.parseInt(properties.getProperty("seed")));

        try (FileWriter writer = new FileWriter(properties.getProperty("plikZRaportem"))) {
            writer.write("# twoje wyniki powinny zawierać te komentarze\n");
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                if (!entry.getKey().equals("plikZRaportem")) {
                    writer.write(entry.toString() + "\n");
                }
            }

            // wylosowanie grafu
            SocialNetwork socialNetwork = new SocialNetwork(properties, random);

            writer.write("\n# agenci jako: id typ lub id* typ dla chorego\n");
            for (Agent agent : socialNetwork.getAgents()) {
                writer.write(agent.toString() + "\n");
            }

            writer.write("\n# graf\n");
            writer.write(socialNetwork.graph());

            int numberOfDays = Integer.parseInt(properties.getProperty("liczbaDni"));

            writer.write("\n# liczność w kolejnych dniach\n");
            for (int currentDay = 1; currentDay <= numberOfDays; ++currentDay) {

                writer.write(socialNetwork.numberOfAliveAgents() + "\n");

                List<Agent> toRemove = new ArrayList<>();

                for (Agent agent : socialNetwork.getAgents()) {
                    // umieranie
                    boolean died = agent.die(properties, random);

                    if (died) {
                        toRemove.add(agent);
                        continue;
                    }

                    // zdrowienie
                    agent.recover(properties, random);

                }

                for (Agent agent : toRemove) {
                    socialNetwork.removeAgent(agent);
                }

                // umawianie spotkań
                if (currentDay < numberOfDays) {
                    for (Agent agent : socialNetwork.getAgents()) {
                        agent.makeAppointments(properties, random, currentDay);
                    }
                }

                // spotkania przypadające na dany dzień
                for (Agent agent : socialNetwork.getAgents()) {
                    agent.meetFriends(properties, random, currentDay);
                }

                socialNetwork.updateNumberOfAgents();

            }

            writer.write(socialNetwork.numberOfAliveAgents());
        } catch (IOException e) {
            System.err.println("Problem z plikiem " + properties.getProperty("plikZRaportem"));

        }
    }

    private static Properties readDefaultProperties() {
        Properties p = new Properties();

        try (FileInputStream stream =
                     new FileInputStream("default.properties");
             Reader reader = Channels.newReader(stream.getChannel(), StandardCharsets.UTF_8.name())) {

            p.load(reader);

        } catch (MalformedInputException e) {
            System.err.println("default.properties nie jest tekstowy");
            System.exit(2);
        } catch (FileNotFoundException e) {
            System.err.println("Brak pliku default.properties");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Błąd w pliku default.properties");
//            e.printStackTrace();
            System.exit(3);
        }

        return p;
    }

    private static Properties readSimulationConf() {
        Properties p = new Properties();

        try (FileInputStream stream = new FileInputStream("simulation-conf.xml")) {

            p.loadFromXML(stream);

        } catch (MalformedInputException e) {
            System.err.println("simulation-conf.xml nie jest XML");
            System.exit(2);
        } catch (FileNotFoundException e) {
            System.err.println("Brak pliku simulation-conf.xml");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Błąd w pliku simulation-conf.xml");
//            e.printStackTrace();
            System.exit(3);
        }

        return p;
    }

    private static Properties checkAndMerge(Properties defaultProperties, Properties simulationConf) {

        Properties res = new Properties();

        List<String> keysForIntegerValues = new ArrayList<>(Arrays.asList("seed", "liczbaAgentów",
                "liczbaDni", "śrZnajomych"));

        List<String> keysForDoubleValues = new ArrayList<>(Arrays.asList("prawdTowarzyski",
                "prawdSpotkania", "prawdZarażenia", "prawdWyzdrowienia", "śmiertelność"));

        List<String> keysForStringValues = new ArrayList<>(Collections.singletonList("plikZRaportem"));

        try {
            String value = null, key = null;
            try {
                for (String k : keysForIntegerValues) {
                    key = k;
                    if (!defaultProperties.containsKey(key) || defaultProperties.getProperty(key).equals("")) {
                        //TODO: poprawić, nie zawsze jest źle
                        throw new NoValue(key);
                    }

                    value = defaultProperties.getProperty(key);
                    Integer.parseInt(value);

                    if (simulationConf.containsKey(key)) {
                        value = simulationConf.getProperty(key);
                        Integer.parseInt(value);
                    }

                    res.put(key, value);
                }

                for (String k : keysForDoubleValues) {
                    key = k;
                    if (!defaultProperties.containsKey(key) || defaultProperties.getProperty(key).equals("")) {
                        throw new NoValue(key);
                    }

                    value = defaultProperties.getProperty(key);
                    Double.parseDouble(value);

                    if (simulationConf.containsKey(key)) {
                        value = simulationConf.getProperty(key);
                        Double.parseDouble(value);
                    }

                    res.put(key, value);
                }

                for (String k : keysForStringValues) {
                    key = k;
                    if (!defaultProperties.containsKey(key) || defaultProperties.getProperty(key).equals("")) {
                        throw new NoValue(key);
                    }

                    value = defaultProperties.getProperty(key);

                    if (simulationConf.containsKey(key)) {
                        value = simulationConf.getProperty(key);
                    }

                    res.put(key, value);
                }
            } catch (NumberFormatException e) {
                throw new BadValue(value, key);
            }

            //////
            if (Double.valueOf(res.getProperty("prawdSpotkania")).equals(1.0)) {
                throw new BadValue(res.getProperty("prawdSpotkania"), "prawdSpotkania");
            }
            if (Integer.parseInt(res.getProperty("śrZnajomych")) >= Integer.parseInt(res.getProperty("liczbaAgentów"))) {
                throw new BadValue(res.getProperty("śrZnajomych"), "śrZnajomych");
            }

        } catch (NoValue e) {
            System.err.println(e.getMessage());
            System.exit(4);
        } catch (BadValue e) {
            System.err.println(e.getMessage());
            System.exit(5);
        }

        return res;
    }
}

class NoValue extends Exception {
    NoValue(String key) {
        super("Brak wartości dla klucza " + key);
    }
}

class BadValue extends Exception {
    BadValue(String value, String key) {
        super("Niedozwolona wartość \"" + value + "\" dla klucza " + key);
    }
}