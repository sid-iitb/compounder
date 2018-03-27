package noob.sk4x0r.compounder.backtesting;

import lombok.Builder;
import org.joda.time.DateTime;

public class LongTrade extends Trade {
    @Builder
    public LongTrade(String script, Long price, Integer quantity, DateTime ts) {
        super(TradeType.LONG, script, quantity);
        this.buyPrice = price;
        this.buyTs = ts;
    }

    @Override
    public void closeTrade(Long price, DateTime ts) {
        this.sellPrice = price;
        this.sellTs = ts;
    }
}
