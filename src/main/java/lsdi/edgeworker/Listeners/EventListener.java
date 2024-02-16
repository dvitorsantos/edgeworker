package lsdi.edgeworker.Listeners;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lsdi.edgeworker.Requests.EventRequest;
import lsdi.edgeworker.Requests.RuleRequest;
import lsdi.edgeworker.Services.MqttService;

import java.util.Map;

import org.springframework.web.client.RestTemplate;


public class EventListener implements UpdateListener {
    MqttService mqttService = MqttService.getInstance();

    RestTemplate restTemplate = new RestTemplate();
    private final String secureResourceAdaptorUrl = System.getenv("SECURE_RESOURCE_ADAPTOR_URL");

    private final RuleRequest rule;

    private final String webhookUrl;

    public EventListener(RuleRequest rule, String webhookUrl) {
        this.rule = rule;
        this.webhookUrl = webhookUrl;
    }

    @Override
    public void update(EventBean[] newData, EventBean[] oldEvents, EPStatement statement, EPRuntime runtime) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            Map<String, Object> event = mapper.readValue(mapper.writeValueAsString(newData[0].getUnderlying()), Map.class);
            
            System.out.println("LOG: Received event: " + event);
            switch(rule.getTarget()) {
                case "EDGE" -> new Thread(() -> {
                    try {
                        mqttService.publish("cdpo/EDGE/event/" + rule.getOutputEventType(), mapper.writeValueAsBytes(event));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                case "FOG" -> new Thread(() -> {
                    try {
                        mqttService.publish("cdpo/FOG/event/" + rule.getOutputEventType(), mapper.writeValueAsBytes(event));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                case "CLOUD" -> new Thread(() -> {
                    try {
                        mqttService.publish("cdpo/CLOUD/event/" + rule.getOutputEventType(), mapper.writeValueAsBytes(event));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                case "WEBHOOK" -> new Thread(() -> {
                    EventRequest eventRequest = new EventRequest();
                    eventRequest.setWebhookUrl(webhookUrl);
                    eventRequest.setEvent(event);
                    eventRequest.setTarget("WEBHOOK");
                    restTemplate.postForObject(eventRequest.getWebhookUrl() + "/event", eventRequest.getEvent(), Map.class);
                }).start();
                case "INTERSCITY" -> new Thread(() -> {
                    Map<String, String> outputEventAttributes = rule.getOutputEventAttributes();

                    for (Map.Entry<String, String> entry : outputEventAttributes.entrySet()) {
                        String capability = entry.getKey();
                        Object data = event.get(capability);

                        if (data != null) {
                            String url = secureResourceAdaptorUrl + "/adaptor/capability/" + capability + "/data/" + data.toString();
                            EventRequest eventRequest = new EventRequest();
                            eventRequest.setWebhookUrl(url);
                            eventRequest.setTaggerExpression(rule.getTagFilter());
                            eventRequest.setEvent(event);
                            eventRequest.setTarget("INTERSCITY");
                            restTemplate.postForObject(eventRequest.getWebhookUrl() + "/event", eventRequest.getTarget(), Map.class);
                        }
                    }

                }).start();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
