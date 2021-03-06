import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;

/**
 * Created By vieee
 */
public class HistoryProvider {

    String fileName = "history";
    Pattern pattern;

    public HistoryProvider() {
        // RegEx to match and extract month, date and event description
        pattern = Pattern.compile("^(\\d{2})\\s(\\d{2})\\t(.*)$");

        // Create a new history file if not present
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                boolean created = file.createNewFile();
                if (created) {
                    System.out.println("---- Server: New history file '" + fileName + "' created");
                }
            }

        } catch (IOException e) {
            System.err.println("---- Server: Error creating history file");
        }
    }

    public void addEvent(Event event) {
        try {
            FileWriter fileWriter = new FileWriter(fileName, true);

            BufferedWriter writer = new BufferedWriter(fileWriter);

            writer.write(String.format("%02d", event.getMonth()) + " " + String.format("%02d", event.getDate()) + "\t"
                    + event.getDescription());
            writer.newLine(); // Append a newline after writing event

            writer.close();

        } catch (IOException e) {
            System.out.println("---- Server: Error writing to file");
            e.printStackTrace();
        }
    }

    public List<Event> query(int month, int date) {
        List<Event> events = new ArrayList<>();

        String line;
        try {
            FileReader fileReader = new FileReader(fileName);

            BufferedReader reader = new BufferedReader(fileReader);

            // Traverse through each line and pick those that match month and date
            // Temporarily store them in a list
            while ((line = reader.readLine()) != null) {
                Event event = parseEvent(line);
                if (event != null) {
                    if (event.getMonth() == month && event.getDate() == date) {
                        events.add(event);
                    }
                }
            }

            reader.close();
        } catch (FileNotFoundException ex) {
            System.err.println("---- Server: File '" + fileName + "' not found!");
            ex.printStackTrace();
        } catch (IOException e) {
            System.err.println("---- Server: Error reading file '" + fileName + "'");
            e.printStackTrace();
        }

        return events;
    }

    private Event parseEvent(String line) {
        Matcher matcher = pattern.matcher(line);

        // If the regular expression matches
        if (matcher.matches()) {
            int month = Integer.valueOf(matcher.group(1)); // get month
            int date = Integer.valueOf(matcher.group(2)); // get date
            String description = matcher.group(3); // get event description

            return new Event(month, date, description);
        }

        return null;
    }

    static Event matchEvent(String line) {
        Matcher matcher = Pattern.compile("^(\\d{2})\\s(\\d{2})\\t(.*)$").matcher(line);

        // If the regular expression matches
        if (matcher.matches()) {
            int month = Integer.valueOf(matcher.group(1)); // get month
            int date = Integer.valueOf(matcher.group(2)); // get date
            String description = matcher.group(3); // get event description

            return new Event(month, date, description);
        }

        return null;
    }

    public static <ListSelectionEvent> void main(String[] args) throws IOException {
        String fileName = "history";
        JFrame frame;
        if (args.length == 1) {
            fileName = "history_backup";
            frame = new JFrame("All Events");
        } else {
            frame = new JFrame("Current Events");
        }
        FileReader fileReader = new FileReader(fileName);
        String line;
        int levels = 0;
        BufferedReader reader = new BufferedReader(fileReader);
        String[][] eventData = new String[2000][3];
        String columns[] = { "Date", "Month", "Event" };

        while ((line = reader.readLine()) != null) {
            Event eventReader = matchEvent(line);
            eventData[levels++][0] = Integer.toString(eventReader.getDate());
            eventData[levels - 1][1] = Integer.toString(eventReader.getMonth());
            eventData[levels - 1][2] = eventReader.getDescription();
        }
        reader.close();

        JTable jTable = new JTable(eventData, columns);
        jTable.setCellSelectionEnabled(true);
        ListSelectionModel select = jTable.getSelectionModel();
        select.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane sp = new JScrollPane(jTable);
        frame.add(sp);
        frame.setSize(300, 900);
        frame.setVisible(true);
    }
}
