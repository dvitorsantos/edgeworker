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
    @JsonProperty("input_event_type")
    String inputEventType;
    @JsonProperty("input_event_attributes")
    Map<String, String> inputEventAttributes;
    @JsonProperty("output_event_type")
    String outputEventType;
}
