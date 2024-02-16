package lsdi.edgeworker.Requests;

import lombok.Data;

import java.util.Map;

@Data
public class TaggedObjectRequest {
    private String uuid;
    private String type;
    private Map<String, String> tags;
}
