import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Symulacja {

    public static void main(String[] args) {

        Properties defaultProperties = readDefaultProperties();
        Properties simulationConf = readSimulationConf();

        Map<String, Object> properties = checkAndMerge(defaultProperties, simulationConf);

        System.out.println(properties);


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

    private static Map<String, Object> checkAndMerge(Properties defaultProperties, Properties simulationConf) {

        Map<String, Object> res = new HashMap<>();

        List<String> keysForIntegerValues = new ArrayList<>(Arrays.asList("seed", "liczbaAgentów",
                "liczbaDni", "śrZnajomych"));

        List<String> keysForDoubleValues = new ArrayList<>(Arrays.asList("prawdTowarzyski",
                "prawdSpotkania", "prawdZarażenia", "prawdWyzdrowienia", "śmiertelność"));

        List<String> keysForStringValues = new ArrayList<>(Collections.singletonList("plikZRaportem"));

        try {
            Object value;
            String getProperty = null, key = null;
            try {
                for (String k : keysForIntegerValues) {
                    key = k;
                    if (!defaultProperties.containsKey(key) || defaultProperties.getProperty(key).equals("")) {
                        throw new NoValue(key);
                    }

                    getProperty = defaultProperties.getProperty(key);
                    value = Integer.valueOf(getProperty);

                    if (simulationConf.containsKey(key)) {
                        getProperty = simulationConf.getProperty(key);
                        value = Integer.valueOf(getProperty);
                    }

                    res.put(key, value);
                }

                for (String k : keysForDoubleValues) {
                    key = k;
                    if (!defaultProperties.containsKey(key) || defaultProperties.getProperty(key).equals("")) {
                        throw new NoValue(key);
                    }

                    getProperty = defaultProperties.getProperty(key);
                    value = Double.valueOf(getProperty);

                    if (simulationConf.containsKey(key)) {
                        getProperty = simulationConf.getProperty(key);
                        value = Integer.valueOf(getProperty);
                    }

                    res.put(key, value);
                }

                for (String k : keysForStringValues) {
                    key = k;
                    if (!defaultProperties.containsKey(key) || defaultProperties.getProperty(key).equals("")) {
                        throw new NoValue(key);
                    }

                    getProperty = defaultProperties.getProperty(key);

                    if (simulationConf.containsKey(key)) {
                        getProperty = simulationConf.getProperty(key);
                    }

                    res.put(key, getProperty);
                }
            } catch (NumberFormatException e) {
                throw new BadValue(getProperty, key);
            }

//            if (res.get("prawdSpotkania").equals(1.0)) {
//                throw new BadValue(1.0, "prawdSpotkania");
//            }
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
    BadValue(Object value, String key) {
        super("Niedozwolona wartość \"" + value + "\" dla klucza " + key);
    }
}