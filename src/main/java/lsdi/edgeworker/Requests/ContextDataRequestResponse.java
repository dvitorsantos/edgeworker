package lsdi.edgeworker.Requests;

import lombok.Data;
import lsdi.edgeworker.Models.Location;

@Data
public class ContextDataRequestResponse {
    private String hostUuid;
    private Location location;
    private String timestamp;
}
