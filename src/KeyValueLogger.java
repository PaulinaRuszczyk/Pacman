import java.io.*;
import java.util.*;

public class KeyValueLogger {
    private String filePath;
    private Map<String, Integer> keyValuePairs;
    private PriorityQueue<Map.Entry<String, Integer>> highestValues;

    public KeyValueLogger(String filePath) {
        this.filePath = filePath;
        this.keyValuePairs = new HashMap<>();
        this.highestValues = new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));
        readFromFile();
    }

    public void log(String key, int value) {
        keyValuePairs.put(key, value);
        highestValues.offer(new AbstractMap.SimpleEntry<>(key, value));
        if (highestValues.size() > 10) {
            highestValues.poll();
        }
    }



    public void writeSortedToFile() {
        List<Map.Entry<String, Integer>> sortedPairs = new ArrayList<>(keyValuePairs.entrySet());
        sortedPairs.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        keyValuePairs.clear(); // Clear existing key-value pairs
        highestValues.clear(); // Clear existing highest values

        for (Map.Entry<String, Integer> entry : sortedPairs) {
            keyValuePairs.put(entry.getKey(), entry.getValue());
            highestValues.offer(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, Integer> entry : sortedPairs) {
                writer.write(entry.getKey() + " - " + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void readFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" - ");
                if (parts.length == 2) {
                    String key = parts[0];
                    int value = Integer.parseInt(parts[1]);
                    keyValuePairs.put(key, value);
                    updateHighestValues(key, value);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateHighestValues(String key, int value) {
        if (highestValues.size() < 10) {
            highestValues.offer(new AbstractMap.SimpleEntry<>(key, value));
        } else if (value > highestValues.peek().getValue()) {
            highestValues.poll();
            highestValues.offer(new AbstractMap.SimpleEntry<>(key, value));
        }
    }

    public List<String> getSortedContents() {
        List<Map.Entry<String, Integer>> sortedPairs = new ArrayList<>(keyValuePairs.entrySet());
        sortedPairs.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        List<String> sortedContents = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sortedPairs) {
            sortedContents.add(entry.getKey() + " - " + entry.getValue());
        }

        return sortedContents;
    }


//    public static void main(String[] args) {
//        KeyValueLogger logger = new KeyValueLogger("log.txt");
//
//        logger.log("Ebis", 300);
//
//        logger.writeSortedToFile();
//
//        List<String> sortedContents = logger.getSortedContents();
//        for (String line : sortedContents) {
//            System.out.println(line);
//        }
//
//    }
}