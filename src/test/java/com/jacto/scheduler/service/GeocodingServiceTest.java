package com.jacto.scheduler.service;

import com.jacto.scheduler.payload.response.GeoLocationDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeocodingServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private GeocodingService geocodingService;

    private Double latitude;
    private Double longitude;
    private String nominatimBaseUrl;

    @BeforeEach
    void setUp() {
        latitude = -23.5505;
        longitude = -46.6333;
        nominatimBaseUrl = "https://nominatim.openstreetmap.org";
        geocodingService = new GeocodingService(nominatimBaseUrl, restTemplate);
    }

    @Test
    void getLocationDetails_ShouldReturnLocationDetails() {
        // Arrange
        Map<String, Object> response = new HashMap<>();
        response.put("display_name", "Praça da Sé, Rua Venceslau Brás, Glicério, Sé, São Paulo, Região Imediata de São Paulo, Região Metropolitana de São Paulo, São Paulo, Região Sudeste, 01016-010, Brasil");

        Map<String, String> address = new HashMap<>();
        address.put("city", "São Paulo");
        address.put("state", "São Paulo");
        address.put("country", "Brasil");
        address.put("postcode", "01016-010");
        response.put("address", address);

        when(restTemplate.getForObject(anyString(), any())).thenReturn(response);

        // Act
        GeoLocationDetails result = geocodingService.getLocationDetails(latitude, longitude);

        // Assert
        assertNotNull(result);
        assertEquals("Praça da Sé, Rua Venceslau Brás, Glicério, Sé, São Paulo, Região Imediata de São Paulo, Região Metropolitana de São Paulo, São Paulo, Região Sudeste, 01016-010, Brasil", result.getFormattedAddress());
        assertEquals("São Paulo", result.getCity());
        assertEquals("São Paulo", result.getState());
        assertEquals("Brasil", result.getCountry());
        assertEquals("01016-010", result.getPostalCode());
    }

    @Test
    void getLocationDetails_WhenServiceFails_ShouldThrowException() {
        // Arrange
        when(restTemplate.getForObject(anyString(), any())).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () ->
            geocodingService.getLocationDetails(latitude, longitude)
        );
        assertEquals("Resposta vazia do serviço de geocodificação", exception.getMessage());
    }
}
