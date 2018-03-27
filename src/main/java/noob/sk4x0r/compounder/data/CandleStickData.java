package noob.sk4x0r.compounder.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Data
@AllArgsConstructor
public class CandleStickData {
    private final String script;
    private final DateTime dateTime;
    private final Long open;
    private final Long high;
    private final Long low;
    private final Long close;
    private final Long volume;
}
