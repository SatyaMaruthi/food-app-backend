package com.foodapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest {

    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;
    private Double latitude;
    private Double longitude;
}