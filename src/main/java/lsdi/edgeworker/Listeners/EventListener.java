package lsdi.edgeworker.Listeners;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lsdi.edgeworker.Services.MqttService;

public class EventListener implements UpdateListener {
    MqttService mqttService = MqttService.getInstance();

    private final String ruleUuid;

    public EventListener(String ruleUuid) {
        this.ruleUuid = ruleUuid;
    }

    @Override
    public void update(EventBean[] newData, EventBean[] oldEvents, EPStatement statement, EPRuntime runtime) {
        String topic = "cdpo/event/" + ruleUuid;
        ObjectMapper mapper = new ObjectMapper();
        try {
            mqttService.publish(topic, mapper.writeValueAsBytes((newData[0]).getUnderlying()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
