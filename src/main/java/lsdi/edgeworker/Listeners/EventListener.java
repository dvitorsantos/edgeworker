package lsdi.edgeworker.Listeners;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lsdi.edgeworker.Services.MqttService;
import org.springframework.web.client.RestTemplate;

public class EventListener implements UpdateListener {
    MqttService mqttService = MqttService.getInstance();

    RestTemplate restTemplate = new RestTemplate();

    private final String outputEventType;

    private final String webhookUrl;

    public EventListener(String outputEventType, String webhookUrl) {
        this.outputEventType = outputEventType;
        this.webhookUrl = webhookUrl;
    }

    @Override
    public void update(EventBean[] newData, EventBean[] oldEvents, EPStatement statement, EPRuntime runtime) {
        String topic = "cdpo/event/" + outputEventType;
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (webhookUrl != null) restTemplate.postForObject(webhookUrl, mapper.writeValueAsString((newData[0]).getUnderlying()), String.class);
            else mqttService.publish(topic, mapper.writeValueAsBytes((newData[0]).getUnderlying()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
