package noob.sk4x0r.compounder.backtesting;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.joda.time.DateTime;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Strategy {
	List<ShortStrangle> shortStrangleList = new ArrayList<>();


	public void printDetails(){
        shortStrangleList.sort((a, b) -> a.getTime().isBefore(b.getTime()) ? -1 : a.getTime().isAfter(b.getTime()) ? 1 : 0);
        Map<Integer, List<ShortStrangle>> dayWiseShortStranglesMap = new HashMap<>();
		Double maxDrawDown = getMaxDrawdown(shortStrangleList);
        System.out.println("Max drawdown: " + getMaxDrawdown(shortStrangleList));
        SummaryStatistics summaryStatistics2 = new SummaryStatistics();
        shortStrangleList.forEach(shortStrangle -> summaryStatistics2.addValue(shortStrangle.getProfit()));
        System.out.println("Profit: " + summaryStatistics2.getSum());
		System.out.println("StopLoss: " + shortStrangleList.get(0).getStopLoss());
		System.out.println("Profit to max drawdown ratio: " + summaryStatistics2.getSum()/maxDrawDown);
        System.out.println("Std: " + summaryStatistics2.getStandardDeviation());

        for(ShortStrangle shortStrangle: shortStrangleList){
            dayWiseShortStranglesMap.computeIfAbsent(shortStrangle.getTime().getDayOfWeek(), k -> new ArrayList<>());
            dayWiseShortStranglesMap.get(shortStrangle.getTime().getDayOfWeek()).add(shortStrangle);
        }
        for(int key: dayWiseShortStranglesMap.keySet()){
            List<ShortStrangle> dayWiseShortStrangleList = dayWiseShortStranglesMap.get(key);
            SummaryStatistics summaryStatistics = new SummaryStatistics();
            dayWiseShortStrangleList.forEach(shortStrangle -> summaryStatistics.addValue(shortStrangle.getProfit()));
            System.out.printf("%20d%20.2f%20.2f%20.2f\n", key, summaryStatistics.getSum(), getMaxDrawdown(dayWiseShortStrangleList), summaryStatistics.getStandardDeviation());
        }
    }


	protected void addTrade(ShortStrangle shortStrangle)
	{
	    if(null != shortStrangle && null != shortStrangle.getCall() && null != shortStrangle.getPut()) {
            shortStrangleList.add(shortStrangle);
        }
	}

	public Double getMaxDrawdown(List<ShortStrangle> shortStrangles)
	{
		Double maxProfit = 0D;
		Double totalProfit = 0D;
		Double maxDrawdown = 0D;
        if(null == shortStrangles || shortStrangles.isEmpty()){
            return maxDrawdown;
        }
		for (ShortStrangle shortStrangle: shortStrangles) {
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
		stringBuffer.append(String.format("%-20s%10.2f\n", "max drawdown", getMaxDrawdown(shortStrangleList)));
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
