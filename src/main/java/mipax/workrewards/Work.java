package mipax.workrewards;

import java.util.ArrayList;
import java.util.Objects;

public record Work (ArrayList<String[]> db) {

    public void add(String newRow) {
        String[] row = newRow.split(";");
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

}
