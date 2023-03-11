package lsdi.edgeworker.Listeners;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.runtime.client.EPRuntime;
import com.espertech.esper.runtime.client.EPStatement;
import com.espertech.esper.runtime.client.UpdateListener;
import lsdi.edgeworker.Services.MqttService;

public class EventListener implements UpdateListener {
    MqttService mqttService = MqttService.getInstance();

    @Override
    public void update(EventBean[] newData, EventBean[] oldEvents, EPStatement statement, EPRuntime runtime) {
        String topic = "cdpo/edgeEvent/" + mqttService.getMqttClientId() + "/" + statement.getDeploymentId();
        mqttService.publish(topic, newData[0].getUnderlying().toString().getBytes());
    }
}
