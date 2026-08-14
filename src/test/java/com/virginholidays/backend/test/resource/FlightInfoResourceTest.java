package com.virginholidays.backend.test.resource;

import com.virginholidays.backend.test.api.Flight;
import com.virginholidays.backend.test.service.FlightInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
/**
 * The FlightInfoResource unit tests
 *
 * @author Geoff Perks
 */
@ExtendWith(MockitoExtension.class)
class FlightInfoResourceTest {

    // FIXME - applicant to complete.
    @Mock
    private FlightInfoService flightInfoService;

    // Manually instantiate the class under test to avoid JDK 18 / Mockito reflection version conflicts
    private FlightInfoResource flightInfoResource;

    @BeforeEach
    public void setUp() {
        flightInfoResource = new FlightInfoResource(flightInfoService);
    }

    @Test
    public void testShouldReturnOkWithCacheControl() throws ExecutionException, InterruptedException {
        // Arrange
        String inputDateStr = "2026-08-14";
        LocalDate parsedDate = LocalDate.parse(inputDateStr);

        Flight mockFlight = mock(Flight.class); // Or build a real Flight instance if accessible
        List<Flight> flightList = List.of(mockFlight);

        CompletableFuture<Optional<List<Flight>>> serviceResponse =
                CompletableFuture.completedFuture(Optional.of(flightList));

        when(flightInfoService.findFlightByDate(parsedDate)).thenReturn(serviceResponse);

        // Act
        CompletionStage<ResponseEntity<?>> completionStage = flightInfoResource.getResults(inputDateStr);
        ResponseEntity<?> response = completionStage.toCompletableFuture().get();

        // Assert
        assertThat(response, notNullValue());
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody(), equalTo(flightList));
        assertThat(response.getHeaders().getCacheControl(), equalTo(CacheControl.noCache().getHeaderValue()));

        verify(flightInfoService, times(1)).findFlightByDate(parsedDate);
    }

    @Test
    public void testShouldReturnNoContentWithCacheControl() throws ExecutionException, InterruptedException {
        // Arrange
        String inputDateStr = "2026-08-14";
        LocalDate parsedDate = LocalDate.parse(inputDateStr);

        CompletableFuture<Optional<List<Flight>>> emptyResponse =
                CompletableFuture.completedFuture(Optional.empty());

        when(flightInfoService.findFlightByDate(parsedDate)).thenReturn(emptyResponse);

        // Act
        CompletionStage<ResponseEntity<?>> completionStage = flightInfoResource.getResults(inputDateStr);
        ResponseEntity<?> response = completionStage.toCompletableFuture().get();

        // Assert
        assertThat(response, notNullValue());
        assertThat(response.getStatusCode(), equalTo(HttpStatus.NO_CONTENT));
        assertThat(response.getBody(), equalTo(null));
        assertThat(response.getHeaders().getCacheControl(), equalTo(CacheControl.noCache().getHeaderValue()));

        verify(flightInfoService, times(1)).findFlightByDate(parsedDate);
    }

    @Test
    public void testShouldThrowDateTimeParseException() {
        // Arrange
        String invalidDateStr = "14-08-2026"; // Non ISO-8601 format

        // Act & Assert
        assertThrows(DateTimeParseException.class, () -> {
            flightInfoResource.getResults(invalidDateStr);
        });

        // Verify that the downstream service was never called due to the parsing crash
        verifyNoInteractions(flightInfoService);
    }
}


