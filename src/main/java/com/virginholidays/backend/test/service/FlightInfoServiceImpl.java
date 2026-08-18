package com.virginholidays.backend.test.service;

import com.virginholidays.backend.test.api.Flight;
import com.virginholidays.backend.test.repository.FlightInfoRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

/**
 * The service implementation of FlightInfoService
 *
 * @author Geoff Perks
 */
@Service
public class FlightInfoServiceImpl implements FlightInfoService {

    private final FlightInfoRepository flightInfoRepository;

    /**
     * The constructor
     *
     * @param flightInfoRepository the flightInfoRepository
     */
    public FlightInfoServiceImpl(FlightInfoRepository flightInfoRepository) {
        this.flightInfoRepository = flightInfoRepository;
    }

    @Override
    public CompletionStage<Optional<List<Flight>>> findFlightByDate(LocalDate outboundDate) {


        DayOfWeek dayOfWeek = outboundDate.getDayOfWeek();

        return flightInfoRepository.findAll().thenApply(optionalFlights -> optionalFlights.map(flights ->
                flights.stream()
                        .filter(flight -> flight.days().contains(dayOfWeek))
                        .sorted(Comparator.comparing(Flight::departureTime)) // Chronological sort
                        .collect(Collectors.toList())
        ));
    }
}
