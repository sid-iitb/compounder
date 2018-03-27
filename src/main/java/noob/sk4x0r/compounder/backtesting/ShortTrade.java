package noob.sk4x0r.compounder.backtesting;

import lombok.Builder;
import org.joda.time.DateTime;

public class ShortTrade extends Trade {
    @Builder
    public ShortTrade(String script, Long price, Integer quantity, DateTime ts) {
        super(TradeType.SHORT, script, quantity);
        this.sellPrice = price;
        this.sellTs = ts;
    }

    @Override
    public void closeTrade(Long price, DateTime ts) {
        this.buyPrice = price;
        this.buyTs = ts;
    }
}
