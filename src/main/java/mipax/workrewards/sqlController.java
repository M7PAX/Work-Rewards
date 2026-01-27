//package mipax.workrewards;
//
//import javafx.event.ActionEvent;
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.Statement;
//
//public class Controller {
//
//    public void connectButton(ActionEvent event) {
//        Database connectNow = new Database();
//        Connection connectDB= connectNow.getConnection();
//
//        String connectQuery = "";
//
//        try {
//            Statement statement = connectDB.createStatement();
//            ResultSet queryOutput = statement.executeQuery(connectQuery);
//
//            System.out.println(queryOutput);
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
