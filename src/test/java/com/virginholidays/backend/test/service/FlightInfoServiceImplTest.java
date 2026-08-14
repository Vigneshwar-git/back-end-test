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

    // FIXME - applicant to complete
    @Mock
    private FlightInfoRepository flightInfoRepository;

    private FlightInfoServiceImpl flightInfoService;

    @BeforeEach
    public void setUp() {
        // Manually initialize to avoid Java 18 ByteBuddy @InjectMocks configuration bugs
        flightInfoService = new FlightInfoServiceImpl(flightInfoRepository);
    }

    @Test
    public void testFindFlightByDate_ShouldReturnOnlyFlightsMatchingDayOfWeek() throws ExecutionException, InterruptedException {
        // Arrange

        String inputDateStr = "2026-08-14";
        LocalDate outboundDate = LocalDate.parse(inputDateStr);
        // Mock flight 1: Operates on Friday (Should match)
        Flight matchingFlight = mock(Flight.class);
        when(matchingFlight.days()).thenReturn(List.of(DayOfWeek.FRIDAY));

        // Mock flight 2: Operates on Monday (Should be filtered out)
        Flight nonMatchingFlight = mock(Flight.class);
        when(nonMatchingFlight.days()).thenReturn(List.of(DayOfWeek.MONDAY));

        List<Flight> allFlights = List.of(matchingFlight, nonMatchingFlight);
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
        assertThat(filteredFlights, hasSize(1));
        assertThat(filteredFlights.get(0), equalTo(matchingFlight));

        verify(flightInfoRepository, times(2)).findAll(); // Twice due to System.out.printf logging line
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

        verify(flightInfoRepository, times(2)).findAll();
    }
}