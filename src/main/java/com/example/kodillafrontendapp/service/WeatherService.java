package com.example.kodillafrontendapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    private final RestTemplate response = new RestTemplate();

    public String getWeather(String location) {
        try {
            String weatherBackendUrl = "http://localhost:8080/api/weather/";
            return response.getForObject(weatherBackendUrl + location, String.class);
        } catch (Exception e) {
            return "Aby móc zobaczyć pogodę, proszę najpierw wybrać lokalizację ";
        }
    }
}
