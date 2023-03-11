package lsdi.edgeworker;

import lsdi.edgeworker.Services.MqttService;
import lsdi.edgeworker.Threads.DatasetReaderThread;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class EdgeworkerApplication {
	MqttService mqttService = MqttService.getInstance();
	public static void main(String[] args) {
		SpringApplication.run(EdgeworkerApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void initCombed() {
		new DatasetReaderThread().start();
	}
}
