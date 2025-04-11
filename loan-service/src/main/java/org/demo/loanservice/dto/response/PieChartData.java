package org.demo.loanservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class PieChartData {
    private String key;
    private BigDecimal value;
    private String realValue;

    public PieChartData(String key, BigDecimal value, String realValue) {
        this.key = key.concat(": ").concat(realValue);
        this.value = value;
        this.realValue = realValue;
    }
}
