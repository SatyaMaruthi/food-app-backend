package com.foodapp.service;

import com.foodapp.domain.entity.Address;
import com.foodapp.domain.entity.User;
import com.foodapp.repository.AddressRepository;
import com.foodapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public List<Address> getAddresses(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    @Transactional
    public Address addAddress(
            Long userId,
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            Double latitude,
            Double longitude) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Address address = new Address();
        address.setUser(user);
        address.setLine1(line1);
        address.setLine2(line2);
        address.setCity(city);
        address.setState(state);
        address.setPostalCode(postalCode);
        address.setLatitude(latitude);
        address.setLongitude(longitude);

        return addressRepository.save(address);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Address does not belong to user");
        }

        addressRepository.delete(address);
    }
}