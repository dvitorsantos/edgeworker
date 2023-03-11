package lsdi.edgeworker.DataTransferObjects;

import lombok.Data;

@Data
public class DeployRequest {
    public String name;
    public String rule;
}
