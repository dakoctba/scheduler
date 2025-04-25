package com.jacto.scheduler.payload.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GeoLocationDetails {

    private Double latitude;
    private Double longitude;
    private String displayName;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String formattedAddress;
    private double distance;

    // Construtor padrão para deserialização JSON
    public GeoLocationDetails() {
    }

    // Construtor com todos os campos
    @JsonCreator
    public GeoLocationDetails(
            @JsonProperty("latitude") Double latitude,
            @JsonProperty("longitude") Double longitude,
            @JsonProperty("displayName") String displayName,
            @JsonProperty("city") String city,
            @JsonProperty("state") String state,
            @JsonProperty("country") String country,
            @JsonProperty("postalCode") String postalCode,
            @JsonProperty("formattedAddress") String formattedAddress,
            @JsonProperty("distance") double distance) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.displayName = displayName;
        this.city = city;
        this.state = state;
        this.country = country;
        this.postalCode = postalCode;
        this.formattedAddress = formattedAddress;
        this.distance = distance;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
