package lsdi.edgeworker.Services;

import org.springframework.web.client.RestTemplate;

import lsdi.edgeworker.Requests.TaggedObjectRequest;

public class TaggerService {
    private final RestTemplate restTemplate;
    private String url;
    public TaggerService(String url) {
        this.url = url;
        this.restTemplate = new RestTemplate();
    }

    public void tagObject(TaggedObjectRequest request) {
        restTemplate.postForObject(url + "/taggedObject", request, TaggedObjectRequest.class);
    }
}
