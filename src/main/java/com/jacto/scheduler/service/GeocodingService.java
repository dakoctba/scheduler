package com.jacto.scheduler.service;

import com.jacto.scheduler.payload.response.GeoLocationDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class GeocodingService {

    private final RestTemplate restTemplate;
    private final String nominatimBaseUrl;

    public GeocodingService(
            @Value("${geocoding.nominatim.base-url:https://nominatim.openstreetmap.org}") String nominatimBaseUrl) {
        this.restTemplate = new RestTemplate();
        this.nominatimBaseUrl = nominatimBaseUrl;
    }

    public GeoLocationDetails getLocationDetails(Double latitude, Double longitude) {
        String url = UriComponentsBuilder.fromHttpUrl(nominatimBaseUrl + "/reverse")
                .queryParam("format", "json")
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("zoom", 18)
                .queryParam("addressdetails", 1)
                .build()
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null) {
            return null;
        }

        GeoLocationDetails details = new GeoLocationDetails();
        details.setLatitude(latitude);
        details.setLongitude(longitude);

        if (response.containsKey("display_name")) {
            details.setFormattedAddress((String) response.get("display_name"));
        }

        if (response.containsKey("address")) {
            Map<String, String> address = (Map<String, String>) response.get("address");

            if (address.containsKey("city")) {
                details.setCity(address.get("city"));
            }
            if (address.containsKey("state")) {
                details.setState(address.get("state"));
            }
            if (address.containsKey("postcode")) {
                details.setPostalCode(address.get("postcode"));
            }
            if (address.containsKey("country")) {
                details.setCountry(address.get("country"));
            }
        }

        return details;
    }
}
