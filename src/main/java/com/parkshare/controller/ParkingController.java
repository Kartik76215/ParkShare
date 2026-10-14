package com.parkshare.controller;

import com.parkshare.dto.ParkingRequest;
import com.parkshare.entity.Availability;
import com.parkshare.entity.ParkingSpace;
import com.parkshare.entity.VehicleType;
import com.parkshare.service.ParkingService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parking")
@CrossOrigin
public class ParkingController {
    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @GetMapping
    public List<ParkingSpace> getParkingSpaces(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) VehicleType vehicleType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) LocalTime startTime,
            @RequestParam(required = false) LocalTime endTime
    ) {
        return parkingService.search(location, vehicleType, minPrice, maxPrice, date, startTime, endTime);
    }

    @GetMapping("/{id}")
    public ParkingSpace getParkingSpace(@PathVariable Long id) {
        return parkingService.getParkingSpace(id);
    }

    @GetMapping("/host/{hostId}")
    public List<ParkingSpace> getHostSpaces(@PathVariable Long hostId) {
        return parkingService.getHostSpaces(hostId);
    }

    @GetMapping("/{id}/availability")
    public List<Availability> getAvailability(@PathVariable Long id) {
        return parkingService.getAvailability(id);
    }

    @PostMapping
    public ParkingSpace createParkingSpace(@RequestBody ParkingRequest request) {
        return parkingService.createParkingSpace(request);
    }

    @PutMapping("/{id}")
    public ParkingSpace updateParkingSpace(@PathVariable Long id, @RequestBody ParkingRequest request) {
        return parkingService.updateParkingSpace(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteParkingSpace(@PathVariable Long id, @RequestParam Long hostId) {
        parkingService.deleteParkingSpace(id, hostId);
    }
}
