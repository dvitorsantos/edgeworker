package lsdi.edgeworker.DataTransferObjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.util.Map;

@Data
public class RuleRequestResponse {
    @Nullable
    String uuid;
    String name;
    String description;
    String definition;
    String level;
    String target;
    @Nullable
    String qos;
    @JsonProperty("tag_filter")
    String tagFilter;
    @JsonProperty("event_type")
    String eventType;
    @JsonProperty("event_attributes")
    Map<String, String> eventAttributes;
    @Nullable
    @JsonProperty("webhook_url")
    String webhookUrl;
    @JsonProperty("output_event_type")
    String outputEventType;
}
