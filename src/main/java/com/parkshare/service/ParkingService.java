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

    public List<ParkingSpace> search(String location, VehicleType vehicleType, BigDecimal maxPrice) {
        return getAllActiveSpaces().stream()
                .filter(space -> location == null || location.isBlank()
                        || space.getAddress().toLowerCase().contains(location.toLowerCase()))
                .filter(space -> vehicleType == null || space.getVehicleType() == vehicleType)
                .filter(space -> maxPrice == null || space.getPricePerHour().compareTo(maxPrice) <= 0)
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
        parkingSpace.setTitle(request.getTitle());
        parkingSpace.setAddress(request.getAddress());
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
}
