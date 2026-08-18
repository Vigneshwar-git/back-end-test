package com.virginholidays.backend.test.service;

import com.virginholidays.backend.test.api.Flight;
import com.virginholidays.backend.test.repository.FlightInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;

/**
 * The FlightInfoServiceImpl unit tests
 *
 * @author Geoff Perks
 */

@ExtendWith(MockitoExtension.class)
class FlightInfoServiceImplTest {

    @Mock
    private FlightInfoRepository flightInfoRepository;

    private FlightInfoServiceImpl flightInfoService;

    @BeforeEach
    public void setUp() {
        // Manually initialize to avoid Java 18 ByteBuddy @InjectMocks configuration bugs
        flightInfoService = new FlightInfoServiceImpl(flightInfoRepository);
    }

    @Test
    public void testFindFlightByDate_ShouldReturnOnlyFlightsMatchingDayOfWeekInChronologicalOrder() throws ExecutionException, InterruptedException {
        // Arrange
        String inputDateStr = "2026-08-14"; // This is a Friday
        LocalDate outboundDate = LocalDate.parse(inputDateStr);

        // Mock flight 1: Later flight on Friday (Should match, but appear second)
        Flight lateFlight = mock(Flight.class);
        when(lateFlight.days()).thenReturn(List.of(DayOfWeek.FRIDAY));
        when(lateFlight.departureTime()).thenReturn(LocalTime.of(18, 30)); // 18:30

        // Mock flight 2: Earlier flight on Friday (Should match, and appear first)
        Flight earlyFlight = mock(Flight.class);
        when(earlyFlight.days()).thenReturn(List.of(DayOfWeek.FRIDAY));
        when(earlyFlight.departureTime()).thenReturn(LocalTime.of(8, 15)); // 08:15

        // Mock flight 3: Operates on Monday (Should be filtered out entirely)
        Flight nonMatchingFlight = mock(Flight.class);
        when(nonMatchingFlight.days()).thenReturn(List.of(DayOfWeek.MONDAY));

        // Put them in the repository out of order (late flight first)
        List<Flight> allFlights = List.of(lateFlight, earlyFlight, nonMatchingFlight);
        CompletableFuture<Optional<List<Flight>>> repositoryResponse =
                CompletableFuture.completedFuture(Optional.of(allFlights));

        when(flightInfoRepository.findAll()).thenReturn(repositoryResponse);

        // Act
        CompletionStage<Optional<List<Flight>>> resultStage = flightInfoService.findFlightByDate(outboundDate);
        Optional<List<Flight>> maybeFilteredFlights = resultStage.toCompletableFuture().get();

        // Assert
        assertThat(maybeFilteredFlights, notNullValue());
        assertThat(maybeFilteredFlights.isPresent(), equalTo(true));

        List<Flight> filteredFlights = maybeFilteredFlights.get();

        // Assert size (excluding the Monday flight)
        assertThat(filteredFlights, hasSize(2));

        // Chronological Assertions
        assertThat(filteredFlights.get(0), equalTo(earlyFlight)); // 08:15 comes first
        assertThat(filteredFlights.get(1), equalTo(lateFlight));  // 18:30 comes second

        verify(flightInfoRepository, times(1)).findAll();
    }

    @Test
    public void testFindFlightByDate_WhenRepositoryReturnsEmptyOptional_ShouldReturnEmptyOptional() throws ExecutionException, InterruptedException {
        // Arrange
        LocalDate outboundDate = LocalDate.of(2026, 8, 14);

        CompletableFuture<Optional<List<Flight>>> emptyRepositoryResponse =
                CompletableFuture.completedFuture(Optional.empty());

        when(flightInfoRepository.findAll()).thenReturn(emptyRepositoryResponse);

        // Act
        CompletionStage<Optional<List<Flight>>> resultStage = flightInfoService.findFlightByDate(outboundDate);
        Optional<List<Flight>> maybeFilteredFlights = resultStage.toCompletableFuture().get();

        // Assert
        assertThat(maybeFilteredFlights, notNullValue());
        assertThat(maybeFilteredFlights.isPresent(), equalTo(false));

        verify(flightInfoRepository, times(1)).findAll();
    }
}