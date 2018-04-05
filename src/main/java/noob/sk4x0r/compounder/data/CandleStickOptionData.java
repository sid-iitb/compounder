package noob.sk4x0r.compounder.data;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.joda.time.DateTime;

@Data
@ToString(callSuper = true)
public class CandleStickOptionData extends CandleStickData {
    private final DateTime expiry;
    private final Long strikePrice;
    private final String type;
    private final Long openInterest;
    @Builder
    public CandleStickOptionData(String script,
                                 DateTime dateTime,
                                 Long open,
                                 Long high,
                                 Long low,
                                 Long close,
                                 Long volume,
                                 DateTime expiry,
                                 Long strikePrice,
                                 String type,
                                 Long openInterest){
        super(script, dateTime, open, high, low, close, volume);
        this.expiry = expiry;
        this.strikePrice = strikePrice;
        this.type = type;
        this.openInterest = openInterest;
    }
}
