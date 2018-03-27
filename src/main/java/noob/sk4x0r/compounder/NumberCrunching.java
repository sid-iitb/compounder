//package noob.sk4x0r.compounder;
//
//import lombok.extern.slf4j.Slf4j;
//import noob.sk4x0r.compounder.data.CandleStickData;
//import noob.sk4x0r.compounder.data.CandleStickOptionData;
//
//import java.sql.Connection;
//import java.sql.Date;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Time;
//import java.sql.Timestamp;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//
//public class NumberCrunching {
////    private static Connection con;
////    private static Connection con2;
////    public static void initialize() {
////        try {
////            Class.forName("com.mysql.jdbc.Driver");
////            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/compounder", "root", "");
////        } catch (Exception e) {
////            System.out.println(e);
////        }
////    }
////    public static void initialize2() {
////        try {
////            Class.forName("com.mysql.jdbc.Driver");
////            con2 = DriverManager.getConnection("jdbc:mysql://localhost:3306/compounder", "root", "");
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////    }
////
////    public static void save(CandleStickOptionData scriptData) throws SQLException {
////        try {
////            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO options_data values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
////            preparedStatement.setString(1, scriptData.getScript());
////            preparedStatement.setDate(2, new Date(scriptData.getExpiry().getTime()));
////            preparedStatement.setLong(3, scriptData.getStrikePrice());
////            preparedStatement.setString(4, scriptData.getType());
////            preparedStatement.setDate(5, new Date(scriptData.getDate().getTime()));
////            preparedStatement.setString(6, getTimeInt(scriptData.getTime()));
////            preparedStatement.setLong(7, scriptData.getOpen());
////            preparedStatement.setLong(8, scriptData.getHigh());
////            preparedStatement.setLong(9, scriptData.getLow());
////            preparedStatement.setLong(10, scriptData.getClose());
////            preparedStatement.setLong(11, scriptData.getVolume());
////            preparedStatement.setLong(12, scriptData.getOpenInterest());
////            preparedStatement.executeUpdate();
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////    }
////
////    private static String getTimeInt(Time time) {
////        return String.valueOf(time.getHours()) + String.valueOf(time.getMinutes());
////    }
////
////    public static void closeConnection() throws SQLException {
////        if (null != con) {
////            con.close();
////        }
////    }
////    public static void closeConnection2() throws SQLException {
////        if (null != con2) {
////            con2.close();
////        }
////    }
////    public static void main(String[] args) throws SQLException, ParseException {
////        while(true) {
////            initialize();
////            initialize2();
////            PreparedStatement preparedStatement = con.prepareStatement("select script, date, time, open, high, low, close, volume, open_interest from options_data2 " +
////                    "where script not in ('BANKNIFTY-I', 'BANKNIFTY-II', 'BANKNIFTY-III', 'BANKNIFTY-I.NFO', 'BANKNIFTY-II.NFO', 'BANKNIFTY-III.NFO') limit 1000");
////            ResultSet resultSet = preparedStatement.executeQuery();
////            int count = 0;
////            while (resultSet.next()) {
////                CandleStickData candleStickData = CandleStickData.builder()
////                        .script(resultSet.getString(1))
////                        .date(resultSet.getDate(2))
////                        .time(getTimeFromInt(resultSet.getInt(3)))
////                        .build();
////                CandleStickOptionData candleStickOptionData = CandleStickOptionData.builder()
////                        .script("BANKNIFTY")
////                        .expiry(getExpiry(resultSet.getString(1)))
////                        .strikePrice(getStrikePrice(resultSet.getString(1)))
////                        .type(getType(resultSet.getString(1)))
////                        .date(new Date(resultSet.getTimestamp(2).getTime()))
////                        .time(getTimeFromInt(resultSet.getInt(3)))
////                        .open(resultSet.getLong(4))
////                        .high(resultSet.getLong(5))
////                        .low(resultSet.getLong(6))
////                        .close(resultSet.getLong(7))
////                        .volume(resultSet.getLong(8))
////                        .openInterest(resultSet.getLong(9))
////                        .build();
////                save(candleStickOptionData);
////                delete(candleStickData);
////                count = ++count % 10000;
////                if (count == 0) {
////                    closeConnection2();
////                    initialize2();
////                }
////            }
////            closeConnection();
////            closeConnection2();
////        }
////    }
//
//    private static void delete(CandleStickData candleStickData) throws SQLException {
//
////        System.out.println(candleStickData);
////        System.out.println(getMinutesFromMidnight(candleStickData.getTime()));
////        PreparedStatement preparedStatement = con2.prepareStatement("Select * from options_data3 where script = ? and date = ? and time = ?");
////        preparedStatement.setString(1, candleStickData.getScript());
////        preparedStatement.setDate(2, new Date(candleStickData.getDate().getTime()));
////        preparedStatement.setInt(3, getMinutesFromMidnight(candleStickData.getTime()));
////        ResultSet resultSet = preparedStatement.executeQuery();
////        System.out.println(resultSet.getString(1));
//
//        System.out.println(candleStickData);
//        PreparedStatement preparedStatement = con2.prepareStatement("DELETE from options_data3 where script = ? and date = ? and time = ?");
//        preparedStatement.setString(1, candleStickData.getScript());
//        preparedStatement.setDate(2, new Date(candleStickData.getDate().getTime()));
//        preparedStatement.setInt(3, getMinutesFromMidnight(candleStickData.getTime()));
//        System.out.println(preparedStatement.execute());
//    }
//
//    private static Long getStrikePrice(String string) {
//        string = string.replace("BANKNIFTY", "")
//                .replace("PE", "")
//                .replace("CE", "")
//                .replace(".NFO", "");
//        return Long.parseLong(string.substring(string.length()-5, string.length()));
//    }
//
//    private static String getType(String string) {
//        return string.substring(21, 23);
//    }
//
//    private static java.util.Date getExpiry(String string) throws ParseException {
//        SimpleDateFormat formatter = new SimpleDateFormat("ddMMMyy");
//        return formatter.parse(string.substring(9, 16));
//    }
//
//    private static Time getTimeFromInt(Integer timeInt) {
//        return new Time(timeInt/100, timeInt%100, 0);
//    }
//
//    private static int getMinutesFromMidnight(Time time) {
//        return time.getHours() * 100 + time.getMinutes();
//    }
//}
