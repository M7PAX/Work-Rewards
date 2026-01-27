package mipax.workrewards;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;

public class FileDB {
    static String fileName = "WRdb.csv";

    public ArrayList<String[]> getFile(){
        ArrayList<String[]> db = new ArrayList<>();
        File csv = new File(fileName);

        try (Scanner Reader = new Scanner(csv)) {
            if (csv.createNewFile()) {
                return db;
            } else {
                while (Reader.hasNextLine()) {
                    String data = Reader.nextLine();
                    String[] row = data.split(";");
                    db.add(row);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return db;
    }

    public void save(ArrayList<String[]> db) throws IOException {
        FileWriter writer = new FileWriter(fileName);
        for (String[] row : db) {
            writer.write(row[0] + ";" + row[1] + ";" + row[2] + ";" + row[3] + ";" + row[4] + ";" + row[5] + "\n");
        }
        writer.close();
        System.exit(0);
    }
}
