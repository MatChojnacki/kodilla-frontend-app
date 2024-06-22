package com.example.kodillafrontendapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TomTomService {

    @Value("${tomtom.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public TomTomService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getLocation(String locationName) {
        String url = "https://api.tomtom.com/search/2/geocode/{location}.json?key={apiKey}";

        return restTemplate.getForObject(url, String.class, locationName, apiKey);
    }
}
