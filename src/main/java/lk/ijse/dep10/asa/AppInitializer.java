package lk.ijse.dep10.asa;

import javafx.application.Application;
import javafx.stage.Stage;
import lk.ijse.dep10.asa.db.DBConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class AppInitializer extends Application {

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (DBConnection.getDbConnection() != null &&
                        !DBConnection.getDbConnection().getConnection().isClosed()) {
                    System.out.println("Database connection is about to close");
                    DBConnection.getDbConnection().getConnection().close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }));    // we need to set the hook when starting the app, no usage if we put after launch()
        launch(args);
//        DBConnection.getDbConnection().getConnection().close();
        /*This will be a problem, if the program interrupted
            then connection will not be closed.

         */
    }

    @Override
    public void start(Stage primaryStage) {
        generateSchemaIfNotExist();


    }

    private void generateSchemaIfNotExist() {
        Connection connection = DBConnection.getDbConnection().getConnection();
        try {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SHOW TABLES");
            HashSet<String> tableNameSet = new HashSet<>();
            while (rst.next()) {
                tableNameSet.add(rst.getString(1));
            }
            boolean tableExist = tableNameSet.
                    containsAll(Set.of("Attendance", "Picture", "Student", "User"));
            System.out.println(tableExist);

            if (!tableExist) {
                System.out.println("Missing data tables are generating");
                stm.execute(readSchemaScript());
            }

            System.out.println(tableNameSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private String readSchemaScript() {
        InputStream is = getClass().getResourceAsStream("/schema.sql");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))){
            String line;
            StringBuilder dbScriptBuilder = new StringBuilder();

            while ((line = br.readLine()) != null) {
                dbScriptBuilder.append(line);
            }
            System.out.println(dbScriptBuilder);
            return dbScriptBuilder.toString();
        } catch (IOException e){
            throw new RuntimeException(e);
        }

    }
}
