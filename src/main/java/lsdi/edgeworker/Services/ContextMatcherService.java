package lsdi.edgeworker.Services;

import lsdi.edgeworker.DataTransferObjects.ContextDataRequestResponse;
import org.springframework.web.client.RestTemplate;

public class ContextMatcherService {
    private String url;
    private final RestTemplate restTemplate;

    public ContextMatcherService(String url) {
        this.url = url;
        this.restTemplate = new RestTemplate();
    }

    public void postContextData(ContextDataRequestResponse context) {
        restTemplate.postForObject(url + "/context", context, String.class);
    }
}
