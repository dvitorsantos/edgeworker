package lsdi.edgeworker.Models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SmartMeterMeasurement {
    private Double power;
    private Double current;
    private Double energy;
}
