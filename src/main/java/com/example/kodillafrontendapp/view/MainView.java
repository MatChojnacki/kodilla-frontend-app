package com.example.kodillafrontendapp.view;

import com.example.kodillafrontendapp.model.Event;
import com.example.kodillafrontendapp.service.TomTomService;
import com.example.kodillafrontendapp.service.WeatherService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Route("")
public class MainView extends VerticalLayout {

    private final RestTemplate restTemplate;
    private final String eventsBackendUrl = "http://localhost:8080/api/events";
    private final Grid<Event> grid;
    private final TextField filter = new TextField();
    @Autowired
    public MainView(RestTemplate restTemplate, TomTomService tomTomService, WeatherService weatherService) {
        this.restTemplate = restTemplate;

        filter.setPlaceholder("Filter...");
        filter.setClearButtonVisible(true);
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> fetchEvents());

        Button addEventButton = new Button("Add Event", e -> openEventForm(new Event()));

        grid = new Grid<>(Event.class);
        grid.setColumns("name", "location", "startTime", "endTime", "description", "price", "registeredAttendees");

        // Set minimum widths for columns to ensure the buttons are visible
        grid.getColumnByKey("name").setWidth("150px").setFlexGrow(0);
        grid.getColumnByKey("location").setWidth("120px").setFlexGrow(0);
        grid.getColumnByKey("startTime").setWidth("200px").setFlexGrow(0);
        grid.getColumnByKey("endTime").setWidth("200px").setFlexGrow(0);
        grid.getColumnByKey("description").setWidth("150px").setFlexGrow(0);
        grid.getColumnByKey("price").setWidth("100px").setFlexGrow(0);
        grid.getColumnByKey("registeredAttendees").setWidth("135px").setFlexGrow(0);

        grid.addComponentColumn(event -> {
            Button editButton = new Button("Edit", click -> openEventForm(event));
            Button deleteButton = new Button("Delete", click -> deleteEvent(event.getId()));
            Button weatherButton = new Button("Get Weather", click -> {
                String weather = weatherService.getWeather(event.getLocation());
                Notification.show(weather, 3000, Notification.Position.MIDDLE);
            });
            Button locationButton = new Button("Get Location", click -> {
                try {
                    String location = tomTomService.getLocation(event.getLocation());
                    Notification.show(location, 3000, Notification.Position.MIDDLE);
                } catch (HttpClientErrorException.NotFound ex) {
                    Notification.show("Location not found: " + ex.getMessage(), 3000, Notification.Position.MIDDLE);
                } catch (Exception ex) {
                    Notification.show(" Aby móc zobaczyć dokładny adres, proszę najpierw wybrać lokalizację", 3000, Notification.Position.MIDDLE);
                }
            });
            return new HorizontalLayout(editButton, deleteButton, weatherButton, locationButton);
        });

        add(filter, addEventButton, grid);

        fetchEvents(); // Wywołanie fetchEvents() w konstruktorze
    }

    private void fetchEvents() {
        Event[] eventsArray = restTemplate.getForObject(eventsBackendUrl, Event[].class);
        assert eventsArray != null;
        List<Event> events = Arrays.asList(eventsArray);

        // Filtrowanie
        String filterText = filter.getValue().trim().toLowerCase();
        if (!filterText.isEmpty()) {
            events = events.stream()
                    .filter(event -> event.getName().toLowerCase().contains(filterText) ||
                            event.getLocation().toLowerCase().contains(filterText) ||
                            event.getDescription().toLowerCase().contains(filterText) ||
                            String.valueOf(event.getPrice()).toLowerCase().contains(filterText) ||
                            event.getStartTime().toString().toLowerCase().contains(filterText) ||
                            event.getEndTime().toString().toLowerCase().contains(filterText))
                    .toList();
        }

        grid.setItems(events);
    }

    private void openEventForm(Event event) {
        Dialog dialog = new Dialog();

        TextField nameField = new TextField("Name");
        nameField.setValue(event.getName() != null ? event.getName() : "");

        // Use ComboBox instead of TextField for location
        ComboBox<String> locationComboBox = new ComboBox<>("Location");
        locationComboBox.setItems("Warszawa", "Kraków", "Łódź", "Poznań", "Wrocław", "Gdańsk", "Szczecin", "Bydgoszcz", "Lublin", "Białystok","Katowice", "Kielce");
        locationComboBox.setValue(event.getLocation() != null ? event.getLocation() : "");

        DatePicker startDatePicker = new DatePicker("Start Date");
        startDatePicker.setValue(event.getStartTime() != null ? event.getStartTime().toLocalDate() : LocalDate.now());

        TimePicker startTimePicker = new TimePicker("Start Time");
        startTimePicker.setValue(event.getStartTime() != null ? event.getStartTime().toLocalTime() : LocalTime.now());

        DatePicker endDatePicker = new DatePicker("End Date");
        endDatePicker.setValue(event.getEndTime() != null ? event.getEndTime().toLocalDate() : LocalDate.now());

        TimePicker endTimePicker = new TimePicker("End Time");
        endTimePicker.setValue(event.getEndTime() != null ? event.getEndTime().toLocalTime() : LocalTime.now());

        TextField descriptionField = new TextField("Description");
        descriptionField.setValue(event.getDescription() != null ? event.getDescription() : "");

        TextField priceField = new TextField("Price");
        priceField.setValue(String.valueOf(event.getPrice()));

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        Button saveButton = new Button("Save", e -> {
            event.setName(nameField.getValue());
            event.setLocation(locationComboBox.getValue());
            try {
                LocalDate startDate = startDatePicker.getValue();
                LocalTime startTime = startTimePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();
                LocalTime endTime = endTimePicker.getValue();

                if (startDate != null && startTime != null) {
                    event.setStartTime(LocalDateTime.of(startDate, startTime));
                }

                if (endDate != null && endTime != null) {
                    event.setEndTime(LocalDateTime.of(endDate, endTime));
                }
            } catch (Exception ex) {
                Notification.show("Invalid date/time format.", 3000, Notification.Position.MIDDLE);
                return;
            }
            event.setDescription(descriptionField.getValue());

            try {
                double price = Double.parseDouble(priceField.getValue());
                event.setPrice(price);
            } catch (NumberFormatException ex) {
                Notification.show("Invalid price format.", 3000, Notification.Position.MIDDLE);
                return;
            }

            if (event.getId() == null) {
                createEvent(event);
            } else {
                updateEvent(event);
            }
            dialog.close();
            fetchEvents(); // Odświeża siatkę po zapisaniu wydarzenia
        });

        FormLayout formLayout = new FormLayout();
        formLayout.add(nameField, locationComboBox, startDatePicker, startTimePicker, endDatePicker, endTimePicker,
                descriptionField, priceField, new HorizontalLayout(saveButton, cancelButton));

        dialog.add(formLayout);
        dialog.open();
    }

    private void createEvent(Event event) {
        try {
            restTemplate.postForObject(eventsBackendUrl, event, Event.class);
            Notification.show("Event created");
            fetchEvents(); // Odświeża siatkę po utworzeniu wydarzenia
        } catch (Exception e) {
            Notification.show("Error creating event: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void updateEvent(Event event) {
        try {
            restTemplate.put(eventsBackendUrl + "/" + event.getId(), event);
            Notification.show("Event updated");
            fetchEvents(); // Odświeża siatkę po zaktualizowaniu wydarzenia
        } catch (Exception e) {
            Notification.show("Error updating event: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }

    private void deleteEvent(Long eventId) {
        try {
            restTemplate.delete(eventsBackendUrl + "/" + eventId);
            Notification.show("Event deleted");
            fetchEvents(); // Odświeża siatkę po usunięciu wydarzenia
        } catch (Exception e) {
            Notification.show("Error deleting event: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        }
    }
}
