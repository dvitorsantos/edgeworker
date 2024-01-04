package lsdi.edgeworker.DataTransferObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.util.List;

@Data
public class DeployRequest {
    @JsonProperty("host_uuid")
    private String hostUuid;
    @JsonProperty("host_type")
    private String hostType;
    @JsonProperty("webhook_url")
    private String webhookUrl;
    private RuleRequestResponse rule;
}