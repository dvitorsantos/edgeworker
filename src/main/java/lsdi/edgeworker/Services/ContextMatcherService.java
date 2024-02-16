package lsdi.edgeworker.Services;

import org.springframework.web.client.RestTemplate;

import lsdi.edgeworker.Requests.ContextDataRequestResponse;

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
