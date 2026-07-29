package mipax.workrewards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Work {
    public ArrayList<String[]> db = new ArrayList<>();
    private final String dbFile = "WRdb.csv";

    public void getdb() throws FileNotFoundException {
        File csv = new File(dbFile);
        Scanner Reader = new Scanner(csv);
        while (Reader.hasNextLine()) {
            String data = Reader.nextLine();
            String[] row = data.split(";", -1);
            db.add(row);
        }
        System.out.println(Arrays.deepToString(db.toArray()));
    }

    public List<String> getCategories() {
        List<String> c = new ArrayList<>();
        for (String[] row : db) {
            c.add(row[3]);
        }
        c.addAll(List.of("Main","New","Daily","Weekly","Monthly","Yearly"));
        Set<String> set = new HashSet<>(c);

        return new ArrayList<>(set);
    }

    public void add(String newRow) {
        String[] row = newRow.split(";",-1);
        db.add(row);
    }

    public void del(String id) {
        for (int i = 0; i < db.size(); i++) {
            String[] row = db.get(i);

            if (Objects.equals(row[0], id)) {
                db.remove(i);
            }
        }

    }

    public void save() throws IOException {
        System.out.println(Arrays.deepToString(db.toArray()));
        FileWriter writer = new FileWriter(dbFile);
        for (String[] row : db) {
            writer.write(row[0] + ";" + row[1] + ";" + row[2] + ";" + row[3] + ";" + row[4] + ";" + row[5] + ";" + row[6] + "\n");
        }
        writer.close();
    }
}
