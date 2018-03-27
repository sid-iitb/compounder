package noob.sk4x0r.compounder.backtesting;

import lombok.Data;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@Data
public abstract class Trade {
    private final TradeType tradeType;
    private final String script;
    private final Integer quantity;
    protected Long buyPrice = -1L;
    protected Long sellPrice = -1L;
    protected Long slippage = 20L;
    protected DateTime buyTs;
    protected DateTime sellTs;
    protected Trade(TradeType tradeType,
                    String script,
                    Integer quantity){
        this.tradeType = tradeType;
        this.script = script;
        this.quantity = quantity;
    }

    public abstract void closeTrade(Long price, DateTime ts);

    public Long getCharges(){
        Long brokerage = 4000L;
        Long stt = 5L * sellPrice * quantity / 10000L;
        Long transactionCharges = 53 * (buyPrice + sellPrice) * quantity / 100000L;
        Long gst = 18L * (brokerage + transactionCharges) / 100L;
        Long sebiCharges = 15L * (buyPrice + sellPrice) * quantity / 10000000L;
        return brokerage + stt + transactionCharges + gst +sebiCharges;
    }

    public Long getProfit()
    {
        if( sellPrice > -1 && buyPrice > -1)
        {
            return (sellPrice-buyPrice) * quantity - getCharges() - slippage * quantity;
        }
        return 0L;
    }

    public boolean isShortTrade() {
        return tradeType == TradeType.SHORT;
    }

    public boolean isLongTrade() {
        return tradeType == TradeType.LONG;
    }

    public void printDetails(){
        DateTimeFormatter dtf = DateTimeFormat.forPattern("dd/MM/yyyy hh:mm");
        StringBuilder tradeDetails = new StringBuilder();
        tradeDetails.append(getTradeType());
        tradeDetails.append(",");
        if(tradeType == TradeType.LONG){
            tradeDetails.append(dtf.print(getBuyTs()));
            tradeDetails.append(", ");
            tradeDetails.append(getBuyPrice()/(double) 100);
            tradeDetails.append(", ");
            tradeDetails.append(dtf.print(getSellTs()));
            tradeDetails.append(", ");
            tradeDetails.append(getSellPrice()/(double) 100);
            tradeDetails.append(", ");
        }else{
            tradeDetails.append(dtf.print(getSellTs()));
            tradeDetails.append(", ");
            tradeDetails.append(getSellPrice()/(double) 100);
            tradeDetails.append(", ");
            tradeDetails.append(dtf.print(getBuyTs()));
            tradeDetails.append(", ");
            tradeDetails.append(getBuyPrice()/(double) 100);
            tradeDetails.append(", ");
        }
        tradeDetails.append(getProfit()/(double) 100);
        System.out.println(tradeDetails.toString());
    }
}
