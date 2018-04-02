package noob.sk4x0r.compounder.backtesting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.joda.time.DateTime;

@Data
@AllArgsConstructor
@Builder
public class ShortStrangle {
    private ShortTrade call;
    private ShortTrade put;
    private DateTime time;
    private Long stopLoss;
    private Long amount;

    public Double getProfit() {
        return (call.getProfit() + put.getProfit()) / 100d;
    }

    public void printDetails() {
        call.printDetails();
        put.printDetails();
    }
}
