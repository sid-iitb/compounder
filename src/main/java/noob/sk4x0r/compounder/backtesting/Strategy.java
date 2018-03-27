package noob.sk4x0r.compounder.backtesting;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Strategy {

	static Map<Integer, List<ShortStrangle>> dayWiseMaxProfitShortStranglesMap = new ConcurrentHashMap<>();
	static Map<Integer, Double> dayWiseMaxProfitDrawdownMap = new ConcurrentHashMap<>();
	static Map<Integer, Double> dayWiseMaxProfitMap = new ConcurrentHashMap<>();
	static {
		for(int i=1;i<=5;i++){
			dayWiseMaxProfitMap.put(i, -1000000D);
		}
	}

	List<ShortStrangle> shortStrangleList = new ArrayList<>();

	protected void addTrade(ShortStrangle shortStrangle)
	{
	    if(null != shortStrangle && null != shortStrangle.getCall() && null != shortStrangle.getPut()) {
            shortStrangleList.add(shortStrangle);
        }
	}

	public Double getMaxDrawdown()
	{
		Double maxProfit = 0D;
		Double totalProfit = 0D;
		Double maxDrawdown = 0D;
        if(null == shortStrangleList || shortStrangleList.isEmpty()){
            return maxDrawdown;
        }
		for (ShortStrangle shortStrangle: shortStrangleList) {
			totalProfit += shortStrangle.getProfit();
			if(totalProfit > maxProfit)
			{
				maxProfit = totalProfit;
			}
			if(maxDrawdown < (maxProfit - totalProfit))
			{
				maxDrawdown = maxProfit - totalProfit;
			}
		}
		return maxDrawdown;
	}

    public Double getMaxDrawdown(List<ShortStrangle> shortStrangleList)
    {
        Double maxProfit = 0D;
        Double totalProfit = 0D;
        Double maxDrawdown = 0D;
        if(null == shortStrangleList || shortStrangleList.isEmpty()){
            return maxDrawdown;
        }
        for (ShortStrangle shortStrangle: shortStrangleList) {
            totalProfit += shortStrangle.getProfit();
            if(totalProfit > maxProfit)
            {
                maxProfit = totalProfit;
            }
            if(maxDrawdown < (maxProfit - totalProfit))
            {
                maxDrawdown = maxProfit - totalProfit;
            }
        }
        return maxDrawdown;
    }
	
	private Double getCurrentDrawdown()
	{
		Double maxProfit = 0D;
        Double totalProfit = 0D;
        Double maxDrawdown = 0D;
        if(null == shortStrangleList || shortStrangleList.isEmpty()){
            return maxDrawdown;
        }
		for (ShortStrangle shortStrangle : shortStrangleList) {
			totalProfit += shortStrangle.getProfit();
			if(totalProfit > maxProfit)
			{
				maxProfit = totalProfit;
			}
			if(maxDrawdown < (maxProfit - totalProfit))
			{
				maxDrawdown = maxProfit - totalProfit;
			}
		}
		return maxProfit - totalProfit;
	}

    public double getPercentProfitable() {
        Double profitTrades = 0D;
        if (null == shortStrangleList || shortStrangleList.isEmpty()) {
            return profitTrades;
        }
        for (ShortStrangle shortStrangle : shortStrangleList) {
            Double profit = shortStrangle.getProfit();
            if (profit > 0) {
                profitTrades++;
            }
        }
        return (double) profitTrades / (double) shortStrangleList.size();
    }
	
	public Double getAverageWinningTrade() {
		Double profit = 0D;
		int index = 0;
        for (ShortStrangle shortStrangle : shortStrangleList) {
			if ( shortStrangle.getProfit() >= 0) {
				profit += shortStrangle.getProfit();
				index++;
			}
		}
		return profit/index;
	}
	
	public Double getAverageLosingTrade()
	{
		Double profit = 0D;
		int index = 0;
        if(null == shortStrangleList || shortStrangleList.isEmpty()){
            return profit;
        }
        for (ShortStrangle shortStrangle : shortStrangleList) {
			if ( shortStrangle.getProfit() < 0)
			{
				profit += shortStrangle.getProfit();
				index++;
			}
		}
		return profit/index;
	}
	
	public void getMonthlyProfits()
	{

		Map<String, Double> profitMap = new TreeMap<>();
        for (ShortStrangle shortStrangle : shortStrangleList) {
			DateTime ts = shortStrangle.getTime();
			String month = String.valueOf(ts.getYear()) + (ts.getMonthOfYear() < 10 ? "0" : "") +String.valueOf(ts.getMonthOfYear());
			Double profit = null == profitMap.get(month)? 0D : profitMap.get(month);
            profit += shortStrangle.getProfit();
			profitMap.put(month, profit);
		}
		Double cumProfit = 0D;
		for (Entry<String, Double> entry : profitMap.entrySet()) {
			cumProfit += entry.getValue();
			System.out.printf("%6s\t%10.2f\t%10.2f\n", entry.getKey(),  entry.getValue(), cumProfit);
			if (entry.getKey().endsWith("12") )
			{
				cumProfit = 0D;
			}
		}
    }
	
	public void getYearWiseProfit()
	{
		Map<String, Double> profitMap = new TreeMap<>();
        for (ShortStrangle shortStrangle : shortStrangleList) {
            DateTime ts = shortStrangle.getTime();
            String year = String.valueOf(ts.getYear());
            Double profit = null == profitMap.get(year)? 0D : profitMap.get(year);
            profit += shortStrangle.getProfit();
            profitMap.put(year, profit);
		}
        for (Entry<String, Double> entry : profitMap.entrySet()) {
            System.out.printf("%6s\t%10.2f\n", entry.getKey(), entry.getValue());
        }
	}

    public void printSummary(long startTime,
                             long endTime,
                             long premium,
                             long stopLoss,
                             boolean squareOffBothPositions,
                             boolean tradeCurrentExpiryOnThursday,
                             boolean tradeOnFriday){
        SummaryStatistics summaryStatistics = new SummaryStatistics();
        for (ShortStrangle shortStrangle : shortStrangleList) {
            summaryStatistics.addValue(shortStrangle.getProfit());
        }
        System.out.printf("%5d\t%5d\t%3d\t%3d\t%5s\t%5s\t%5s\t%10.2f\t%10.2f\t%10.2f\n",
                startTime,
                endTime,
                premium,
                stopLoss,
                squareOffBothPositions,
                tradeCurrentExpiryOnThursday,
                tradeOnFriday,
                summaryStatistics.getSum(),
                getMaxDrawdown(),
                summaryStatistics.getStandardDeviation()
        );
    }
    private long getDate(DateTime dateTime) {
        return Long.parseLong(String.valueOf(dateTime.getYear())
                + (dateTime.getMonthOfYear() < 10 ? "0": "") + String.valueOf(dateTime.getMonthOfYear())
                + (dateTime.getDayOfMonth() < 10 ? "0": "") +String.valueOf(dateTime.getDayOfMonth()));
    }


    public void printTrades() {
        for(ShortStrangle shortStrangle:shortStrangleList){
            System.out.println(getDate(shortStrangle.getTime())+"\t"+shortStrangle.getProfit());
        }
    }

    public void printDayWiseSummary(){
        Map<Integer, List<ShortStrangle>> dayWiseMap = new TreeMap<>();
        for(int i=1;i<=5;i++){
            dayWiseMap.put(i, new ArrayList<>());
        }
        for (ShortStrangle shortStrangle : shortStrangleList) {
            dayWiseMap.get(shortStrangle.getTime().getDayOfWeek()).add(shortStrangle);
        }
        for(int day:dayWiseMap.keySet()){
            SummaryStatistics summaryStatistics = new SummaryStatistics();
            List<ShortStrangle> shortStrangles = dayWiseMap.get(day);
            for(ShortStrangle shortStrangle:shortStrangles){
                summaryStatistics.addValue(shortStrangle.getProfit());
            }
            if(summaryStatistics.getSum() > dayWiseMaxProfitMap.get(day)){
            	dayWiseMaxProfitMap.put(day, summaryStatistics.getSum());
            	dayWiseMaxProfitShortStranglesMap.put(day, shortStrangles);
            	dayWiseMaxProfitDrawdownMap.put(day, getMaxDrawdown(shortStrangles));
			}
            System.out.printf("%d\t%10.2f\t%10.2f\t%10.2f\n", day, summaryStatistics.getSum(), getMaxDrawdown(shortStrangles), summaryStatistics.getStandardDeviation());
        }
    }

    public static void printDayWiseMaxProfitSummary(){
        System.out.println("Max profit summary");
        Map<Integer, String> dayWiseMaxProfitDetailsMap = new TreeMap<>();
        for(Entry<Integer, List<ShortStrangle>> e: dayWiseMaxProfitShortStranglesMap.entrySet()){
            SummaryStatistics summaryStatistics = new SummaryStatistics();
            for(ShortStrangle shortStrangle: e.getValue()){
                summaryStatistics.addValue(shortStrangle.getProfit());
            }
            ShortStrangle shortStrangle = e.getValue().get(0);
            dayWiseMaxProfitDetailsMap.put(e.getKey(),
                    String.format("%10.2f%10d\t%10d\t%10d", summaryStatistics.getStandardDeviation(), shortStrangle.getMoneyNess(), shortStrangle.getStopLoss(), getTimeFromDateTime(shortStrangle.getTime())));
        }
        for(Entry<Integer, Double> e:dayWiseMaxProfitMap.entrySet()){
            System.out.printf("%10d\t%10.2f\t%10.2f\t%30s\n", e.getKey(), e.getValue(), dayWiseMaxProfitDrawdownMap.get(e.getKey()), dayWiseMaxProfitDetailsMap.get(e.getKey()));
        }
    }

	public String summarize() {
        SummaryStatistics summaryStatistics = new SummaryStatistics();

		int consLossCount = 0;
		int maxConsLossCount = 0;
		int consProfCount = 0;
		int maxConsProfCount = 0;
        for (ShortStrangle shortStrangle : shortStrangleList) {
            summaryStatistics.addValue(shortStrangle.getProfit());
            //shortStrangle.printDetails();
			if( shortStrangle.getProfit() > 0)
			{
				consProfCount++;
				if ( consProfCount > maxConsProfCount)
				{
					maxConsProfCount = consProfCount;
				}
				consLossCount = 0;
			} else
			{
				consLossCount++;
				if ( consLossCount > maxConsLossCount)
				{
					maxConsLossCount = consLossCount;
				}
				consProfCount = 0;
			}
		}
		this.getMonthlyProfits();
		this.getYearWiseProfit();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(String.format("%-20s%10d\n", "Total trades", summaryStatistics.getN()));
        stringBuffer.append(String.format("%-20s%10.2f\n", "Total profit", summaryStatistics.getSum()));
        stringBuffer.append(String.format("%-20s%10.2f\n", "Avg   profit", summaryStatistics.getMean()));
        stringBuffer.append(String.format("%-20s%10.2f\n", "Std dev", summaryStatistics.getStandardDeviation()));
        stringBuffer.append(String.format("%-20s%10.2f\n", "Percent profit", getPercentProfitable()));
        stringBuffer.append(String.format("%-20s%10.2f\n", "Max   Profit", summaryStatistics.getMax()));
		stringBuffer.append(String.format("%-20s%10.2f\n", "Max   Loss", summaryStatistics.getMin()));
		stringBuffer.append(String.format("%-20s%10.2f\n", "max drawdown", getMaxDrawdown()));
		stringBuffer.append(String.format("%-20s%10.2f\n", "Current drawdown", getCurrentDrawdown()));
		stringBuffer.append(String.format("%-20s%10d\n", "Max cons loss", maxConsLossCount));
		stringBuffer.append(String.format("%-20s%10d\n", "Max cons Profit", maxConsProfCount));
		stringBuffer.append(String.format("%-20s%10.2f\n", "Avg Win Trade", getAverageWinningTrade()));
		stringBuffer.append(String.format("%-20s%10.2f\n", "Avg Loss Trade", getAverageLosingTrade()));
		return stringBuffer.toString();
	}
    private static long getTimeFromDateTime(DateTime dateTime) {
        return dateTime.getHourOfDay() * 100 + dateTime.getMinuteOfHour();
    }
}
