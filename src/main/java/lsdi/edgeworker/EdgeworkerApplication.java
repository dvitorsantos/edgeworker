package lsdi.edgeworker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lsdi.edgeworker.DataTransferObjects.DeployRequest;
import lsdi.edgeworker.DataTransferObjects.DeployResponse;
import lsdi.edgeworker.DataTransferObjects.IoTGatewayRequest;
import lsdi.edgeworker.DataTransferObjects.TaggedObjectRequest;
import lsdi.edgeworker.Services.DeployService;
import lsdi.edgeworker.Services.IotCatalogerService;
import lsdi.edgeworker.Services.MqttService;
import lsdi.edgeworker.Services.TaggerService;
import lsdi.edgeworker.Threads.DatasetReaderThread;
import lsdi.edgeworker.Threads.ContextDataReaderThread;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.DataInput;
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
		selfRegister();
//		selfTag();
		subscribeToDeploy();
		subscribeToUndeploy();
		new ContextDataReaderThread().start();
		new DatasetReaderThread().start();
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

	private void selfTag() {
		TaggerService taggerService = new TaggerService(taggerUrl);
		TaggedObjectRequest request = new TaggedObjectRequest();
		request.setUuid(edgeworkerUuid);
		request.setType("FogNode");

		Map<String, String> tags = new HashMap<>();
		tags.put("type", "fognode");

		request.setTags(tags);
		taggerService.tagObject(request);
	}

	private void subscribeToDeploy() {
		MqttService mqttService = MqttService.getInstance();
		DeployService deployService = new DeployService();
		mqttService.subscribe("/deploy/" + edgeworkerUuid, (topic, message) -> {
			new Thread(() -> {
				ObjectMapper mapper = new ObjectMapper();
				try {
					DeployRequest deployRequest = mapper.readValue(message.toString(), DeployRequest.class);
					deployRequest.getEdgeRules().forEach(edgeRule -> {
						new Thread(() -> {
							DeployResponse deployResponse = deployService.deploy(edgeRule);
							publishToDeployStatus(deployResponse);
						}).start();
					});
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

	private void publishToDeployStatus(DeployResponse deployResponse) {
		MqttService mqttService = MqttService.getInstance();
		ObjectMapper mapper = new ObjectMapper();
		try {
			new Thread(() -> {
				try {
					mqttService.publish("/deploy/" + deployResponse.getRuleUuid(), mapper.writeValueAsBytes(deployResponse));
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}
			}).start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
