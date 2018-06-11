import java.io.*;
import java.nio.channels.Channels;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

public class Symulacja {

    public static void main(String[] args) {

        Properties defaultProperties = readDefaultProperties();
        Properties simulationConf = readSimulationConf();
        Properties properties = checkAndMerge(defaultProperties, simulationConf);

        Random random = new Random(Integer.parseInt(properties.getProperty("seed")));

        try (FileWriter writer = new FileWriter(properties.getProperty("plikZRaportem"))) {
            writer.write("# twoje wyniki powinny zawierać te komentarze\n");
            for (String key : properties.stringPropertyNames()) {
                if (!key.equals("plikZRaportem")) {
                    writer.write(key + "=" + properties.getProperty(key) + "\n");
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
            try {
                throw new BadValue(properties.getProperty("plikZRaportem"), "plikZRaportem");
            } catch (BadValue e2) {
                System.err.println(e2.getMessage());
                System.exit(5);
            }
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
            System.exit(3);
        }

        return p;
    }

    private static Properties checkAndMerge(Properties defaultProperties, Properties simulationConf) {
        // warning: ugly code
        Properties res = null;

        List<String> keysForIntegerValues = new ArrayList<>(Arrays.asList("seed", "liczbaAgentów",
                "liczbaDni", "śrZnajomych"));
        List<String> keysForDoubleValues = new ArrayList<>(Arrays.asList("prawdTowarzyski",
                "prawdSpotkania", "prawdZarażenia", "prawdWyzdrowienia", "śmiertelność"));
        String keyForStringValue = "plikZRaportem";

        try {
            String value = null, key = null;
            try {
                res = new Properties(defaultProperties);
                Set<String> keySet = res.stringPropertyNames();

                for (String k : keysForIntegerValues) {
                    key = k;
                    if (keySet.contains(key)) {
                        value = res.getProperty(key);
                        Integer.parseInt(value);
                    }
                }

                for (String k : keysForDoubleValues) {
                    key = k;
                    if (keySet.contains(key)) {
                        value = res.getProperty(key);
                        double d = Double.parseDouble(value);

                        // all double values are probability
                        if (d < 0.0 || 1.0 < d) {
                            throw new BadValue(value, key);
                        }
                    }
                }

                // manual checks...
                key = "liczbaAgentów";
                value = res.getProperty(key);
                if (value != null) {
                    int v = Integer.parseInt(value);
                    if (v < 1 || 1000000 < v) {
                        throw new BadValue(value, key);
                    }

                    key = "śrZnajomych";
                    value = res.getProperty(key);
                    if (value != null) {
                        int v2 = Integer.parseInt(value);
                        if (v2 < 0) {
                            throw new BadValue(value, key);
                        }
                    }
                }

                key = "liczbaDni";
                value = res.getProperty(key);
                if (value != null) {
                    int v = Integer.parseInt(value);
                    if (v < 1 || 1000 < v) {
                        throw new BadValue(value, key);
                    }
                }

                // merging
                for (Object k : simulationConf.keySet()) {
                    key = k.toString();
                    value = simulationConf.getProperty(key);

                    if (keysForIntegerValues.contains(key)) {
                        Integer.parseInt(value);
                    } else if (keysForDoubleValues.contains(key)) {
                        double d = Double.parseDouble(value);

                        // all double values are probability
                        if (d < 0.0 || 1.0 < d) {
                            throw new BadValue(value, key);
                        }
                    }

                    res.setProperty(key, value);
                }

                // manual checks...
                keySet = res.stringPropertyNames();
                for (String k : keysForIntegerValues) {
                    if (!keySet.contains(k)) {
                        throw new NoValue(k);
                    }
                }
                for (String k : keysForDoubleValues) {
                    if (!keySet.contains(k)) {
                        throw new NoValue(k);
                    }
                }
                if (!keySet.contains(keyForStringValue)) {
                    throw new NoValue(keyForStringValue);
                }

                key = "liczbaAgentów";
                value = res.getProperty(key);
                int v = Integer.parseInt(value);
                if (v < 1 || 1000000 < v) {
                    throw new BadValue(value, key);
                }
                key = "śrZnajomych";
                value = res.getProperty(key);
                int v2 = Integer.parseInt(value);
                if (v2 < 0 || v - 1 < v2) {
                    throw new BadValue(value, key);
                }

                key = "liczbaDni";
                value = res.getProperty(key);
                v = Integer.parseInt(value);
                if (v < 1 || 1000 < v) {
                    throw new BadValue(value, key);
                }

                key = "prawdSpotkania";
                value = res.getProperty(key);
                if (Double.parseDouble(value) == 1.0) {
                    throw new BadValue(value, key);
                }

            } catch (NumberFormatException e) {
                throw new BadValue(value, key);
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