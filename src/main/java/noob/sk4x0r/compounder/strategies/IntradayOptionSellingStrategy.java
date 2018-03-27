package noob.sk4x0r.compounder.strategies;

import noob.sk4x0r.compounder.ScriptDataCommands;
import noob.sk4x0r.compounder.backtesting.ShortStrangle;
import noob.sk4x0r.compounder.backtesting.ShortTrade;
import noob.sk4x0r.compounder.backtesting.Strategy;
import noob.sk4x0r.compounder.data.CandleStickOptionData;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class IntradayOptionSellingStrategy extends Strategy {
    static Map<String, List<CandleStickOptionData>> candleStickMap = new HashMap<>();
    public static void main(String[] args) throws Exception {
        ScriptDataCommands.initialize();
        IntradayOptionSellingStrategy strategy = new IntradayOptionSellingStrategy();
        strategy.test();
    }

    private static final String TABLE_NAME = "options_data";

    protected void test() throws Exception{
                List<Long> startTimes = new ArrayList<>();
        for(long i = 940; i <= 1020; i=i+10){
            if(i%100 < 60){
                startTimes.add(i);
            }
        }
        List<Long> endTimes = new ArrayList<>();
        for(long i = 1520; i <= 1520; i=i+10){
            if(i%100 < 60){
                endTimes.add(i);
            }
        }

        List<Long> stopLosses = new ArrayList<>();
        for(long i = 125; i <= 200; i=i+10){
            stopLosses.add(i);
        }

        List<Long> moneynessList = new ArrayList<>();
        for(long i = -50000; i <=50000 ; i=i+10000){
            moneynessList.add(i);
        }

        ExecutorService es = Executors.newFixedThreadPool(16);
        for(long startTime:startTimes){
            for(long endTime:endTimes){
                for(long stopLoss:stopLosses) {
                    for(long moneyness:moneynessList) {
                        for (boolean squareOffBothPositions : Arrays.asList(false)) {
                            for(boolean tradeCurrentExpiryOnThursday: Arrays.asList(true)) {
                                for (boolean tradeOnFriday : Arrays.asList(true)) {
                                    if (startTime < endTime) {
                                        es.execute(() -> {
                                            IntradayOptionSellingStrategy strategy = new IntradayOptionSellingStrategy();
                                            try {
                                                strategy.test(startTime, endTime, moneyness, stopLoss, squareOffBothPositions, tradeCurrentExpiryOnThursday, tradeOnFriday);
//                                                System.out.print(".");
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        es.shutdown();
        es.awaitTermination(1000, TimeUnit.DAYS);
//        System.out.println(".");
        printDayWiseMaxProfitSummary();

    }

    protected void test(long startTime,
                        long endTime,
                        long moneyness,
                        long stopLoss,
                        boolean squareOffBothPositions,
                        boolean tradeCurrentExpiryOnThursday,
                        boolean tradeOnFriday) throws Exception {
        try(Connection connection = ScriptDataCommands.getConncection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("Select distinct date from " + TABLE_NAME + " order by date");
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Long> dates = new ArrayList<>();
            while (resultSet.next()) {
                dates.add(resultSet.getLong(1));
            }
            if (!tradeOnFriday) {
                Iterator<Long> dateIterator = dates.iterator();
                while (dateIterator.hasNext()) {
                    Long date = dateIterator.next();
                    if (getDate(date).getDayOfWeek() == 5) {
                        dateIterator.remove();
                    }
                }
            }
            for (Long date : dates) {
                List<CandleStickOptionData> calls = getCandles("CE", date, startTime);
                List<CandleStickOptionData> puts = getCandles("PE", date, startTime);
                ShortStrangle shortStrangle = getShortStrangle(calls,
                        puts,
                        moneyness,
                        date,
                        startTime,
                        endTime,
                        stopLoss,
                        squareOffBothPositions,
                        tradeCurrentExpiryOnThursday);
                addTrade(shortStrangle);
            }
        }
//        printSummary(startTime, endTime, moneyness, stopLoss, squareOffBothPositions, tradeCurrentExpiryOnThursday, tradeOnFriday);
        printDayWiseSummary();
//        System.out.println(summarize());
//        System.out.println(startTime + " " + endTime + " " + premium + " " + stopLoss + " " + squareOffBothPositions + " " + getTotalProfit()/(double)100 + " " + getMaxDrawdown()/(double)100);
        printTrades();
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
}
