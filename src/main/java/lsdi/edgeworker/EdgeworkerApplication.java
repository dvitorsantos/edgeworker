package lsdi.edgeworker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lsdi.edgeworker.DataTransferObjects.*;
import lsdi.edgeworker.Models.Vehicle;
import lsdi.edgeworker.Services.*;
import lsdi.edgeworker.Threads.DatasetReaderThread;
import lsdi.edgeworker.Threads.ContextDataReaderThread;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.DataInput;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class EdgeworkerApplication {
    @Value("${edgeworker.uuid}")
    private String edgeworkerUuid;
    @Value("${edgeworker.name}")
    private String edgeworkerName;
    @Value("${edgeworker.url}")
    private String edgeworkerUrl;
    @Value("${iotcataloger.url}")
    private String iotCatalogerUrl;
    @Value("${tagger.url}")
    private String taggerUrl;

    public static void main(String[] args) {
        SpringApplication.run(EdgeworkerApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        // selfRegister();
        subscribeToDeploy();
        subscribeToUndeploy();
        subscribeToBusLocationEvents();
        subscribeToCarLocationEvents();
        // new ContextDataReaderThread().start();
        // new DatasetReaderThread().start();
    }

    private void selfRegister() {
        IotCatalogerService iotCatalogerService = new IotCatalogerService(iotCatalogerUrl);
        IoTGatewayRequest request = new IoTGatewayRequest();
        request.setUuid(edgeworkerUuid);
        request.setDistinguishedName(edgeworkerName);
        request.setUrl(edgeworkerUrl);
        request.setLatitude(1.0);
        request.setLongitude(1.0);

        iotCatalogerService.registerGateway(request);
    }

    private void subscribeToDeploy() {
        MqttService mqttService = MqttService.getInstance();
        DeployService deployService = new DeployService();

        mqttService.subscribe("/deploy/" + edgeworkerUuid, (topic, message) -> {
            new Thread(() -> {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    DeployRequest deployRequest = mapper.readValue(message.toString(), DeployRequest.class);
                    RuleRequestResponse rule = deployRequest.getRule();

                    new Thread(() -> {
                        DeployResponse deployResponse = deployService.deploy(rule);
                        System.out.println("INFO: Rule " + rule.getName() + " (" + rule.getDefinition() + ") deployed.");
                        publishToDeployStatus(deployResponse);
                    }).start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void subscribeToUndeploy() {
        MqttService mqttService = MqttService.getInstance();
        DeployService deployService = new DeployService();
        mqttService.subscribe("/undeploy", (topic, message) -> {
            ObjectMapper mapper = new ObjectMapper();
            try {
                String deployId = mapper.readValue(message.toString(), String.class);
                deployService.undeploy(deployId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void subscribeToBusLocationEvents() {
        MqttService mqttService = MqttService.getInstance();
        EsperService esperService = new EsperService();
        ObjectMapper mapper = new ObjectMapper();
        mqttService.subscribe("bus/" + edgeworkerUuid, (topic, message) -> {
            new Thread(() -> {
                try {
                    Vehicle vehicle = mapper.readValue(message.getPayload(), Vehicle.class);
                    esperService.sendEvent(vehicle, "Vehicle");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    public void subscribeToCarLocationEvents() {
        MqttService mqttService = MqttService.getInstance();
        EsperService esperService = new EsperService();
        ObjectMapper mapper = new ObjectMapper();
        mqttService.subscribe("carro/" + edgeworkerUuid, (topic, message) -> {
            new Thread(() -> {
                try {
                    Vehicle vehicle = mapper.readValue(message.getPayload(), Vehicle.class);
                    esperService.sendEvent(vehicle, "Vehicle");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void publishToDeployStatus(DeployResponse deployResponse) {
        MqttService mqttService = MqttService.getInstance();
        ObjectMapper mapper = new ObjectMapper();
        try {
            new Thread(() -> {
                try {
                    mqttService.publish("/deploy-status/" + deployResponse.getRuleUuid(),
                            mapper.writeValueAsBytes(deployResponse));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // public void publishToContextData(Vehicle vehicle) {
    // ContextMatcherService contextMatcherService = new
    // ContextMatcherService("http://contextmatcher:8080");
    // try {
    // new Thread(() -> {
    // try {
    // ContextDataRequestResponse contextDataRequestResponse = new
    // ContextDataRequestResponse();
    // contextDataRequestResponse.setHostUuid(edgeworkerUuid);
    // contextDataRequestResponse.setLocation(new Location(vehicle.getLatitude(),
    // vehicle.getLongitude()));
    // contextDataRequestResponse.setTimestamp(LocalDateTime.now().toString());
    // contextMatcherService.postContextData(contextDataRequestResponse);
    // Thread.sleep(10000);
    // } catch (InterruptedException e) {
    // throw new RuntimeException(e);
    // }
    // }).start();

    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
}
