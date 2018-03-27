package noob.sk4x0r.compounder.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Data
public class CandleStickFutureData extends CandleStickData {
    private final DateTime expiry;
    private final Long openInterest;

    @Builder
    public CandleStickFutureData(String script,
                                 DateTime dateTime,
                                 Long open,
                                 Long high,
                                 Long low,
                                 Long close,
                                 Long volume,
                                 DateTime expiry,
                                 Long openInterest){
        super(script, dateTime, open, high, low, close, volume);
        this.expiry = expiry;
        this.openInterest = openInterest;

    }
}
