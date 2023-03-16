package lsdi.edgeworker;

import lsdi.edgeworker.DataTransferObjects.IoTGatewayRequest;
import lsdi.edgeworker.Threads.DatasetReaderThread;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class EdgeworkerApplication {
	@Value("${iotcataloger.url}")
	private String iotCatalogerUrl;
	public static void main(String[] args) {
		SpringApplication.run(EdgeworkerApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void initCombed() {
		selfRegister();
		new DatasetReaderThread().start();
	}

	private void selfRegister() {
		RestTemplate restTemplate = new RestTemplate();

		IoTGatewayRequest request = new IoTGatewayRequest();
		request.setUuid("edgeworker");
		request.setDistinguishedName("edgeworker");
		request.setUrl("http://localhost:9696/");
		request.setLatitude(1.0);
		request.setLongitude(1.0);

		HttpHeaders headers = new HttpHeaders();
		headers.set("X-SSL-Client-DN", request.getDistinguishedName());

		HttpEntity<Object> entity = new HttpEntity<>(request, headers);
		restTemplate.postForObject(iotCatalogerUrl + "/gateway", entity, IoTGatewayRequest.class);
	}
}
