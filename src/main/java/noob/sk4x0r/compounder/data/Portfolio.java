package noob.sk4x0r.compounder.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Portfolio {
    Long entryDate;
    Long exitDate;
    Long profit;
    Long investment;
    List<String> stocks;
}
