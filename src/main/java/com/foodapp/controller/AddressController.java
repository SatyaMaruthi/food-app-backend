package com.foodapp.controller;

import com.foodapp.domain.entity.Address;
import com.foodapp.dto.AddressRequest;
import com.foodapp.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/addresses")
    public List<Address> getAddresses(
            @RequestHeader("X-User-Id") Long userId) {

        return addressService.getAddresses(userId);
    }

    @DeleteMapping("/addresses/{addressId}")
    public void deleteAddress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long addressId) {

        addressService.deleteAddress(userId, addressId);
    }

    @PostMapping("/addresses")
    public Address addAddress(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody AddressRequest request) {

        return addressService.addAddress(
                userId,
                request.getLine1(),
                request.getLine2(),
                request.getCity(),
                request.getState(),
                request.getPostalCode(),
                request.getLatitude(),
                request.getLongitude()
        );
    }
}