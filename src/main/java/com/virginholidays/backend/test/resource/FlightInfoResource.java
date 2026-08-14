package com.virginholidays.backend.test.resource;

import com.virginholidays.backend.test.api.Flight;
import com.virginholidays.backend.test.service.FlightInfoService;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static org.springframework.http.CacheControl.noCache;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.springframework.http.ResponseEntity.status;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;

/**
 * @author Geoff Perks
 *
 * The FlightInfoResource
 */
@RestController
public class FlightInfoResource {

    private final FlightInfoService flightInfoService;

    /**
     * The constructor
     *
     * @param flightInfoService the flightInfoService
     */
    public FlightInfoResource(FlightInfoService flightInfoService) {
        this.flightInfoService = flightInfoService;
    }

    /**
     * Resource method for returning flight results
     *
     * @param date the chosen date
     * @return flights for the day of the chosen date
     */
    @RequestMapping(method = RequestMethod.GET, path = "/{date}/results")
    public CompletionStage<ResponseEntity<?>> getResults(@PathVariable("date") @NotEmpty String date) {

        LocalDate parsedDate = LocalDate.parse(date);

        return flightInfoService.findFlightByDate(parsedDate).thenApply(maybeResults -> {

            // no results, no content
            if (maybeResults.isEmpty()) {
                return status(HttpStatus.NO_CONTENT).cacheControl(noCache()).build();
            }

            List<Flight> results = maybeResults.get();

            return status(HttpStatus.OK).cacheControl(noCache()).body(results);
        });
    }
}
