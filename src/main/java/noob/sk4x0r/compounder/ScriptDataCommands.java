package noob.sk4x0r.compounder;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import noob.sk4x0r.compounder.data.CandleStickData;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

public class ScriptDataCommands {
    private static ComboPooledDataSource cpds;

    public static void initialize() throws PropertyVetoException {
//        try {
//            Class.forName("com.mysql.jdbc.Driver");
//            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/compounder?autoReconnect=true&useSSL=false", "root", "");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        cpds = new ComboPooledDataSource();
        cpds.setDriverClass( "com.mysql.cj.jdbc.Driver" ); //loads the jdbc driver
        cpds.setJdbcUrl( "jdbc:mysql://localhost:3306/compounder?autoReconnect=true&useSSL=false" );
        cpds.setUser("root");
        cpds.setPassword("");
        cpds.setInitialPoolSize(16);
        cpds.setAcquireIncrement(8);
        cpds.setMaxPoolSize(3000);
        cpds.setPreferredTestQuery("select 1 from dual");
        cpds.setUnreturnedConnectionTimeout(300);
        cpds.setDebugUnreturnedConnectionStackTraces(true);
    }

    public static Connection getConncection() throws SQLException {
        return cpds.getConnection();
    }
}
