package org.demo.loanservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PieChartData {
    private String key;
    private BigDecimal value;
    private String realValue;
}
