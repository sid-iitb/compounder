package noob.sk4x0r.compounder;

import com.opencsv.CSVReader;
import noob.sk4x0r.compounder.data.CandleStickData;
import org.joda.time.DateTime;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OptionsDataDumper {
    private static final String TABLE_NAME = "script_data";
    public static void dumpData(final File folder) throws SQLException, PropertyVetoException, IOException {
        ScriptDataCommands.initialize();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                dumpData(fileEntry);
            } else if (fileEntry.getName().equalsIgnoreCase("BANKNIFTY.txt")
                    || fileEntry.getName().equalsIgnoreCase("NIFTY.txt")
                    || fileEntry.getName().equalsIgnoreCase("INDIAVIX.txt")) {
                System.out.println("" + fileEntry);
                Connection connection = ScriptDataCommands.getConncection();
                try {
                    CSVReader reader;
                    reader = new CSVReader(new FileReader(fileEntry));
                    reader.readNext();
                    String[] line;
                    while ((line = reader.readNext()) != null) {
                        try {
                            String script = line[0];
                            String date = line[1];
                            String time = line[2];
                            String open = line[3];
                            String high = line[4];
                            String low = line[5];
                            String close = line[6];
                            DateTime dateTime = getDateTime(date, time);
                            CandleStickData scriptData = new CandleStickData(
                                    script,
                                    dateTime,
                                    convertAmountFromRupeesToPaisa(open),
                                    convertAmountFromRupeesToPaisa(high),
                                    convertAmountFromRupeesToPaisa(low),
                                    convertAmountFromRupeesToPaisa(close),
                                    0L);
                            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + TABLE_NAME +
                                    " (`script`, `date`, `time`, `open`, `high`, `low`, `close`, `volume` ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                            preparedStatement.setString(1, scriptData.getScript());
                            preparedStatement.setLong(2, getDate(scriptData.getDateTime()));
                            preparedStatement.setLong(3, getTime(scriptData.getDateTime()));
                            preparedStatement.setLong(4, scriptData.getOpen());
                            preparedStatement.setLong(5, scriptData.getHigh());
                            preparedStatement.setLong(6, scriptData.getLow());
                            preparedStatement.setLong(7, scriptData.getClose());
                            preparedStatement.setLong(8, scriptData.getVolume());
                            preparedStatement.executeUpdate();
                            preparedStatement.close();
                        } catch (SQLIntegrityConstraintViolationException e){
                        }
                    }
                    reader.close();
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
                connection.close();
            }
        }
    }

    private static long getTime(DateTime dateTime) {
        StringBuilder stringBuilder = new StringBuilder();
        if(dateTime.getHourOfDay() < 10 ){
            stringBuilder.append("0");
        }
        stringBuilder.append(dateTime.getHourOfDay());
        if(dateTime.getMinuteOfHour() < 10){
            stringBuilder.append(0);
        }
        stringBuilder.append(dateTime.getMinuteOfHour());
        return Long.parseLong(stringBuilder.toString());
    }

    private static long getDate(DateTime dateTime) {
        return Long.parseLong(String.valueOf(dateTime.getYear())
                + (dateTime.getMonthOfYear() < 10 ? "0": "") + String.valueOf(dateTime.getMonthOfYear())
                + (dateTime.getDayOfMonth() < 10 ? "0": "") +String.valueOf(dateTime.getDayOfMonth()));
    }


    public static long convertAmountFromRupeesToPaisa(String amount) {
        int decimalPosition = amount.indexOf('.');
        if(decimalPosition == -1){
            return Long.parseLong(amount)*100;
        }
        if (decimalPosition + 1 == amount.length()) {
            return Long.parseLong(amount.replace(".", "") + "00");
        } else if (decimalPosition + 2 == amount.length()) {
            return Long.parseLong(amount.replace(".", "") + "0");
        } else if (decimalPosition + 3 == amount.length()) {
            return Long.parseLong(amount.replace(".", ""));
        }
        return Long.parseLong(amount.split("\\.")[0]+ amount.split("\\.")[1].substring(0,2));
    }

    public static void main(String[] args) throws ParseException, SQLException, PropertyVetoException, IOException {

        String path = "/Users/santosh.patil/Desktop/oneminutedata";
        File folder = new File(path);
        dumpData(folder);
    }

    private static DateTime getDateTime(String date, String time) throws ParseException {
        Date d = new SimpleDateFormat("yyyyMMddHH:mm")
                .parse(String.valueOf(date) + String.valueOf(time));
        DateTime dateTime = new DateTime(d.getTime());
        return dateTime.minusMinutes(1);
    }
}
