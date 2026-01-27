//package mipax.workrewards;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//
//public class Database {
//    public Connection dataDatabaseLink;
//
//    public Connection getConnection() {
//        String databaseName = "workRewards";
//        String databaseUser = "mipax";
//        String databasePassword = "1234";
//        String url = "jdbc:mysql://localhost/" + databaseName;
//
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            dataDatabaseLink = DriverManager.getConnection(url,  databaseUser, databasePassword);
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return dataDatabaseLink;
//    }
//}
