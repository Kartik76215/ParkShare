package com.parkshare.service;

import com.parkshare.dto.ParkingRequest;
import com.parkshare.entity.Availability;
import com.parkshare.entity.ParkingSpace;
import com.parkshare.entity.User;
import com.parkshare.entity.UserRole;
import com.parkshare.entity.VehicleType;
import com.parkshare.exception.BadRequestException;
import com.parkshare.exception.ResourceNotFoundException;
import com.parkshare.repository.AvailabilityRepository;
import com.parkshare.repository.ParkingSpaceRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ParkingService {
    private final ParkingSpaceRepository parkingSpaceRepository;
    private final AvailabilityRepository availabilityRepository;
    private final UserService userService;

    public ParkingService(
            ParkingSpaceRepository parkingSpaceRepository,
            AvailabilityRepository availabilityRepository,
            UserService userService
    ) {
        this.parkingSpaceRepository = parkingSpaceRepository;
        this.availabilityRepository = availabilityRepository;
        this.userService = userService;
    }

    public List<ParkingSpace> getAllActiveSpaces() {
        return parkingSpaceRepository.findByActiveTrue();
    }

    public List<ParkingSpace> search(
            String location,
            VehicleType vehicleType,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    ) {
        return getAllActiveSpaces().stream()
                .filter(space -> matchesLocation(space, location))
                .filter(space -> vehicleType == null || space.getVehicleType() == vehicleType)
                .filter(space -> minPrice == null || space.getPricePerHour().compareTo(minPrice) >= 0)
                .filter(space -> maxPrice == null || space.getPricePerHour().compareTo(maxPrice) <= 0)
                .filter(space -> matchesAvailability(space, date, startTime, endTime))
                .toList();
    }

    public ParkingSpace getParkingSpace(Long id) {
        return parkingSpaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parking space not found."));
    }

    public List<ParkingSpace> getHostSpaces(Long hostId) {
        return parkingSpaceRepository.findByHostIdAndActiveTrue(hostId);
    }

    public ParkingSpace createParkingSpace(ParkingRequest request) {
        User host = userService.getUser(request.getHostId());
        if (host.getRole() != UserRole.HOST) {
            throw new BadRequestException("Only hosts can add parking spaces.");
        }

        ParkingSpace parkingSpace = new ParkingSpace();
        copyRequestToParkingSpace(request, parkingSpace);
        parkingSpace.setHost(host);
        ParkingSpace saved = parkingSpaceRepository.save(parkingSpace);
        saveAvailabilityIfPresent(request, saved);
        return saved;
    }

    public ParkingSpace updateParkingSpace(Long id, ParkingRequest request) {
        ParkingSpace parkingSpace = getParkingSpace(id);
        if (!parkingSpace.getHost().getId().equals(request.getHostId())) {
            throw new BadRequestException("Only the owner can edit this parking space.");
        }
        copyRequestToParkingSpace(request, parkingSpace);
        ParkingSpace saved = parkingSpaceRepository.save(parkingSpace);
        saveAvailabilityIfPresent(request, saved);
        return saved;
    }

    public void deleteParkingSpace(Long id, Long hostId) {
        ParkingSpace parkingSpace = getParkingSpace(id);
        if (!parkingSpace.getHost().getId().equals(hostId)) {
            throw new BadRequestException("Only the owner can delete this parking space.");
        }
        parkingSpace.setActive(false);
        parkingSpaceRepository.save(parkingSpace);
    }

    public List<Availability> getAvailability(Long parkingSpaceId) {
        return availabilityRepository.findByParkingSpaceId(parkingSpaceId);
    }

    private void copyRequestToParkingSpace(ParkingRequest request, ParkingSpace parkingSpace) {
        validateParkingRequest(request);
        parkingSpace.setTitle(request.getTitle());
        parkingSpace.setAddress(request.getAddress());
        parkingSpace.setCity(request.getCity());
        parkingSpace.setLocality(request.getLocality());
        parkingSpace.setDescription(request.getDescription());
        parkingSpace.setImageUrl(request.getImageUrl());
        parkingSpace.setLatitude(request.getLatitude());
        parkingSpace.setLongitude(request.getLongitude());
        parkingSpace.setPricePerHour(request.getPricePerHour());
        parkingSpace.setVehicleType(request.getVehicleType());
    }

    private void saveAvailabilityIfPresent(ParkingRequest request, ParkingSpace parkingSpace) {
        if (request.getAvailableDate() == null || request.getStartTime() == null || request.getEndTime() == null) {
            return;
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("Availability start time must be before end time.");
        }
        Availability availability = new Availability();
        availability.setParkingSpace(parkingSpace);
        availability.setDate(request.getAvailableDate());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availabilityRepository.save(availability);
    }

    private void validateParkingRequest(ParkingRequest request) {
        if (isBlank(request.getTitle()) || isBlank(request.getAddress())) {
            throw new BadRequestException("Parking title and address are required.");
        }
        if (request.getPricePerHour() == null || request.getPricePerHour().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Price per hour must be positive.");
        }
        if (request.getVehicleType() == null) {
            throw new BadRequestException("Vehicle type is required.");
        }
    }

    private boolean matchesLocation(ParkingSpace space, String location) {
        if (location == null || location.isBlank()) {
            return true;
        }
        String text = (safe(space.getAddress()) + " " + safe(space.getCity()) + " " + safe(space.getLocality()))
                .toLowerCase();
        return text.contains(location.toLowerCase());
    }

    private boolean matchesAvailability(ParkingSpace space, LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (date == null) {
            return true;
        }
        return availabilityRepository.findByParkingSpaceIdAndDate(space.getId(), date).stream()
                .anyMatch(availability -> {
                    if (startTime == null || endTime == null) {
                        return true;
                    }
                    return !startTime.isBefore(availability.getStartTime())
                            && !endTime.isAfter(availability.getEndTime())
                            && startTime.isBefore(endTime);
                });
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
