package com.example.kodillafrontendapp.service;

import com.example.kodillafrontendapp.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.Arrays;
import java.util.List;

@Service
public class EventService {

    private final RestTemplate restTemplate;
    private final String eventsBackendUrl = "http://localhost:8080/api/events";

    @Autowired
    public EventService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Event[] getEvents() {
        return restTemplate.getForObject(eventsBackendUrl, Event[].class);
    }

    public List<Event> getAllEvents() {
        Event[] eventsArray = restTemplate.getForObject(eventsBackendUrl, Event[].class);
        assert eventsArray != null;
        return Arrays.asList(eventsArray);
    }

    public void createEvent(Event event) {
        restTemplate.postForObject(eventsBackendUrl, event, Event.class);
    }

    public void updateEvent(Event event) {
        restTemplate.put(eventsBackendUrl + "/" + event.getId(), event);
    }

    public void deleteEvent(Long eventId) {
        restTemplate.delete(eventsBackendUrl + "/" + eventId);
    }
}
