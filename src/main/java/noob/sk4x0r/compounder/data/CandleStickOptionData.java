package noob.sk4x0r.compounder.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.joda.time.DateTime;

import java.sql.Time;
import java.util.Date;

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
