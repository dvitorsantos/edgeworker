package lsdi.edgeworker.Requests;

import lombok.Data;

import java.util.Map;

@Data
public class EventRequest {
    private String webhookUrl;
    private String taggerExpression;
    private String target;
    private Map<String, Object> event;
}
