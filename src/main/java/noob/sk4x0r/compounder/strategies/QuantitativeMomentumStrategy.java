package noob.sk4x0r.compounder.strategies;

import noob.sk4x0r.compounder.ScriptDataCommands;
import noob.sk4x0r.compounder.backtesting.ShortStrangle;
import noob.sk4x0r.compounder.backtesting.ShortTrade;
import noob.sk4x0r.compounder.backtesting.Strategy;
import noob.sk4x0r.compounder.data.CandleStickOptionData;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.awt.peer.ListPeer;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class QuantitativeMomentumStrategy extends Strategy {
    static Map<String, List<CandleStickOptionData>> candleStickMap = new HashMap<>();
    public static void main(String[] args) throws Exception {
        ScriptDataCommands.initialize();
        QuantitativeMomentumStrategy strategy = new QuantitativeMomentumStrategy();
        strategy.execute();
    }

    private static final String TABLE_NAME = "nifty500_eod_data";

    protected void execute() throws Exception{
                List<Long> startTimes = new ArrayList<>();
        List<String> symbols = new ArrayList<>();
        ExecutorService es = Executors.newFixedThreadPool(16);
        try(Connection connection = ScriptDataCommands.getConncection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("Select distinct symbol from  nifty500");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                symbols.add(resultSet.getString(1));
            }
        }

        es.execute(() -> {
            QuantitativeMomentumStrategy strategy = new QuantitativeMomentumStrategy();
            try {
                strategy.test(symbols);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        es.shutdown();
        es.awaitTermination(1000, TimeUnit.DAYS);
//        printDayWiseMaxProfitSummary();

    }

    protected void test(List<String> symbols) throws Exception {
        try(Connection connection = ScriptDataCommands.getConncection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("Select distinct date from " + TABLE_NAME + " order by date");
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Long> dates = new ArrayList<>();
            while (resultSet.next()) {
                dates.add(resultSet.getLong(1));
            }
            dates = selectDates(dates);
            List<Long>  previousDates;
            for (int i = 12; i< dates.size()-1; i++) {
                Map<String, List<Long>> priceListMap = new HashMap<>();
                Map<String, Long> momentumMap = new HashMap<>();
                Long date = dates.get(i);
                previousDates= dates.subList(i-12, i+1);
                for(String symbol:symbols){
                    List<Long> priceList = getPriceList(symbol, previousDates);
                    if(priceList.size()!=13){
                        continue;
                    }else{
                        priceListMap.put(symbol, priceList);
                        momentumMap.put(symbol, getMomentum(priceList));
                    }
                }
                System.out.print(date +": ");
                System.out.println(sortByValue(momentumMap));
            }
        }
//        printSummary(startTime, endTime, moneyness, stopLoss, squareOffBothPositions, tradeCurrentExpiryOnThursday, tradeOnFriday);
//        printDayWiseSummary();
//        System.out.println(summarize());
//        System.out.println(startTime + " " + endTime + " " + premium + " " + stopLoss + " " + squareOffBothPositions + " " + getTotalProfit()/(double)100 + " " + getMaxDrawdown()/(double)100);
//        printTrades();
    }

    private Long getMomentum(List<Long> priceList) {
        Double momentum = 1D;
        int positiveCount = 0;
        for(int i = 0; i < priceList.size() - 1; i++){
            if(priceList.get(i) < priceList.get(i+1)){
                positiveCount ++;
            }
            momentum = momentum * (double) priceList.get(i) / (double) priceList.get(i+1);
        }
        if(positiveCount < priceList.size() / 2 + 1){
            return 0L;
        }
        momentum = momentum * 100;
        return momentum.longValue();
    }

    private List<Long> getPriceList(String symbol, List<Long> previousDates) throws SQLException {
        try (Connection connection =ScriptDataCommands.getConncection()){
            String paramStr = StringUtils.join(previousDates.iterator(),",");
            PreparedStatement preparedStatement = connection.prepareStatement("Select close from "+ TABLE_NAME+" where symbol = ? and date in (" + paramStr + ") order by date");
            preparedStatement.setString(1, symbol);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Long> priceList = new ArrayList<>();
            while(resultSet.next()){
                priceList.add(resultSet.getLong(1));
            }
            return priceList;
        }
    }

    private List<Long> selectDates(List<Long> dates) {
        List<Long> selectedDates = new ArrayList<>();
        int startYear = 2000;
        int endYear = 2017;
        for(int year=startYear;year<=endYear;year++){
            for(int month=1;month<=12;month++){
                for(int date=1;date<=10;date++){
                    if(date == 10){
                        throw new RuntimeException("Data error");
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append(year);
                    if(month < 10 ){
                        sb.append(0);
                    }
                    sb.append(month);
                    sb.append(0);
                    sb.append(date);
                    Long dateValue = Long.valueOf(sb.toString());
                    if(dates.contains(dateValue)){
                        selectedDates.add(dateValue);
                        break;
                    }
                }
            }
        }
        return selectedDates;
    }

    private ShortStrangle getShortStrangle(List<CandleStickOptionData> calls,
                                           List<CandleStickOptionData> puts,
                                           long moneyness,
                                           long date,
                                           long startTime,
                                           long endTime,
                                           long stopLoss,
                                           boolean squareOffBothPositions,
                                           boolean tradeCurrentExpiryOnThursday) throws SQLException, ParseException {
        ShortTrade putTrade = getTrade(puts, moneyness, date, startTime, endTime, stopLoss, tradeCurrentExpiryOnThursday);
        ShortTrade callTrade = getTrade(calls, moneyness, date, startTime, endTime, stopLoss, tradeCurrentExpiryOnThursday);
        if(null == putTrade || null == callTrade){
            return null;
        }
        if(squareOffBothPositions) {
            if (getTimeFromDateTime(putTrade.getBuyTs()) < getTimeFromDateTime(callTrade.getBuyTs())) {
                callTrade = getTrade(calls, moneyness, date, startTime, getTimeFromDateTime(putTrade.getBuyTs()), 10000, tradeCurrentExpiryOnThursday);
            } else if (getTimeFromDateTime(putTrade.getBuyTs()) < getTimeFromDateTime(callTrade.getBuyTs())) {
                putTrade = getTrade(puts, moneyness, date, startTime, getTimeFromDateTime(callTrade.getBuyTs()), 10000, tradeCurrentExpiryOnThursday);
            }
        }
        if(null == putTrade || null == callTrade){
            return null;
        }
        return ShortStrangle.builder()
                .call(callTrade)
                .put(putTrade)
                .moneyNess(moneyness)
                .stopLoss(stopLoss)
                .time(getDateTime(date, startTime))
                .build();
    }

    private boolean isValidExpiry(CandleStickOptionData candle, boolean tradeCurrentExpiryOnThursday){
        int daysToExpity = Days.daysBetween(candle.getDateTime().toLocalDate(), candle.getExpiry().toLocalDate()).getDays();
        if(tradeCurrentExpiryOnThursday) {
            return daysToExpity >= 0 && daysToExpity < 7;
        }else {
            return daysToExpity > 0 && daysToExpity <= 7;
        }
    }

    private ShortTrade getTrade(List<CandleStickOptionData> candles,
                                long moneyness,
                                long date,
                                long startTime,
                                long endTime,
                                long stopLoss,
                                boolean tradeCurrentExpiryOnThursday) throws SQLException, ParseException {
        if(candles.size() == 0){
            return null;
        }

        try(Connection connection = ScriptDataCommands.getConncection()) {
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            preparedStatement = connection.prepareStatement("select open from script_data " +
                    "where script = ? and date = ? and time = ?");
            preparedStatement.setString(1, "BANKNIFTY");
            preparedStatement.setLong(2, date);
            preparedStatement.setLong(3, startTime);
            resultSet = preparedStatement.executeQuery();
            Long bankNifty = null;
            if (resultSet.next()) {
                bankNifty = resultSet.getLong(1);
            }
            if (null == bankNifty) {
                return null;
            }

            CandleStickOptionData openingTrade = null;
            Long minDiff = Long.MAX_VALUE;

            Long strikePrice = null;
            if ("PUT".equalsIgnoreCase(candles.get(0).getType())) {
                strikePrice = ((bankNifty + 10000) - (bankNifty % 10000) - moneyness) / 100;
            } else {
                strikePrice = (bankNifty - (bankNifty % 10000) + moneyness) / 100;

            }
//        Long putStrikePrice = (bankNifty + 100) % 100 - premium;
//        Long callStrikePrice = (bankNifty ) % 100 + premium;

            for (CandleStickOptionData candle : candles) {
//            Long premium = new Double(premiumPercentage * bankNifty / (double) 10000).longValue();
//            if(Math.abs(premium - candle.getClose()) < minDiff && isValidExpiry(candle, tradeCurrentExpiryOnThursday)){
//                minDiff = Math.abs(premium - candle.getClose());
//                openingTrade = candle;
//            }
                if (strikePrice.longValue() == candle.getStrikePrice().longValue() && isValidExpiry(candle, tradeCurrentExpiryOnThursday)) {
                    openingTrade = candle;
                    break;
                }
            }


            if (null == openingTrade) {
                return null;
            }

            preparedStatement = connection.prepareStatement("select * from " + TABLE_NAME +
                    " where script = ? and expiry = ? and strike_price = ?  and type = ? and date = ? and time between ? and ? order by time");
            preparedStatement.setString(1, openingTrade.getScript());
            preparedStatement.setLong(2, getLongDate(openingTrade.getExpiry()));
            preparedStatement.setLong(3, openingTrade.getStrikePrice());
            preparedStatement.setString(4, openingTrade.getType());
            preparedStatement.setLong(5, getLongDate(openingTrade.getDateTime()));
            preparedStatement.setLong(6, startTime);
            preparedStatement.setLong(7, endTime);
            resultSet = preparedStatement.executeQuery();

            long closingTradePrice = -1;
            DateTime closingTradeDateTime = null;
            List<CandleStickOptionData> nextCandles = new ArrayList<>();
            while (resultSet.next()) {
                CandleStickOptionData nextCandle = CandleStickOptionData.builder()
                        .script(resultSet.getString(1))
                        .dateTime(getDateTime(resultSet.getLong(5), resultSet.getLong(6)))
                        .open(resultSet.getLong(7))
                        .high(resultSet.getLong(8))
                        .low(resultSet.getLong(9))
                        .close(resultSet.getLong(10))
                        .volume(resultSet.getLong(11))
                        .expiry(getDate(resultSet.getLong(2)))
                        .strikePrice(resultSet.getLong(3))
                        .type(resultSet.getString(4))
                        .openInterest(resultSet.getLong(12))
                        .build();
                nextCandles.add(nextCandle);
            }
            for (CandleStickOptionData candle : nextCandles) {
                if (candle.getHigh() > stopLoss * openingTrade.getClose() / 100) {
                    closingTradePrice = stopLoss * openingTrade.getClose() / 100;
                    closingTradeDateTime = candle.getDateTime();
                    break;
                } else if (getTimeFromDateTime(candle.getDateTime()) == endTime) {
                    closingTradePrice = candle.getClose();
                    closingTradeDateTime = candle.getDateTime();
                    break;
                }
            }
            if (closingTradePrice == -1) {
                return null;
            }

            ShortTrade trade = new ShortTrade("BANKNIFTY", openingTrade.getClose(), 40, openingTrade.getDateTime());
            trade.closeTrade(closingTradePrice, closingTradeDateTime);
            return trade;
        }
    }

    private long getTimeFromDateTime(DateTime dateTime) {
        return dateTime.getHourOfDay() * 100 + dateTime.getMinuteOfHour();
    }

    private String getMapKey(String type, Long date, Long time){
        return type + "_" + String.valueOf(date) + "_" + String.valueOf(time);
    }

    private List<CandleStickOptionData> getCandles(String type, Long date, Long time) throws SQLException, ParseException {
        if(candleStickMap.containsKey(getMapKey(type, date, time))){
            return candleStickMap.get(getMapKey(type, date, time));
        }
        List<CandleStickOptionData> result = new ArrayList<>();
        try(Connection connection = ScriptDataCommands.getConncection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("select * from " + TABLE_NAME + " where type = ? and date = ? and time = ?");
            preparedStatement.setString(1, type);
            preparedStatement.setLong(2, date);
            preparedStatement.setLong(3, time);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                CandleStickOptionData candle = CandleStickOptionData.builder()
                        .script(resultSet.getString(1))
                        .dateTime(getDateTime(resultSet.getLong(5), resultSet.getLong(6)))
                        .open(resultSet.getLong(7))
                        .high(resultSet.getLong(8))
                        .low(resultSet.getLong(9))
                        .close(resultSet.getLong(10))
                        .volume(resultSet.getLong(11))
                        .expiry(getDate(resultSet.getLong(2)))
                        .strikePrice(resultSet.getLong(3))
                        .type(resultSet.getString(4))
                        .openInterest(resultSet.getLong(12))
                        .build();
                result.add(candle);
            }
            resultSet.close();
            preparedStatement.close();
            connection.close();
            candleStickMap.put(getMapKey(type, date, time), result);
            return result;
        }
    }

    private long getLongDate(DateTime expiry) {
        return Long.parseLong(new SimpleDateFormat("yyyyMMdd").format(expiry.toDate()));
    }

    private DateTime getDate(long date) throws ParseException {
        Date d = new SimpleDateFormat("yyyyMMdd")
                .parse(String.valueOf(date));
        return new DateTime(d);
    }

    private static DateTime getDateTime(long date, long time) throws ParseException {
        Date d = new SimpleDateFormat("yyyyMMddHHmm")
                .parse(String.valueOf(date) + (time < 1000 ? "0" : "") + String.valueOf(time));
        return new DateTime(d);
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort((o2, o1) -> o1.getValue().compareTo(o2.getValue()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
