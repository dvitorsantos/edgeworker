package lsdi.edgeworker;

import lsdi.edgeworker.DataTransferObjects.IoTGatewayRequest;
import lsdi.edgeworker.DataTransferObjects.TaggedObjectRequest;
import lsdi.edgeworker.Services.IotCatalogerService;
import lsdi.edgeworker.Services.TaggerService;
import lsdi.edgeworker.Threads.DatasetReaderThread;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

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
	public void initCombed() {
		selfRegister();
//		selfTag();
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
}
