package demo.bookingservice.service;

import demo.bookingservice.clients.CatalogClient;
import demo.bookingservice.dtos.CreateBookingRequestDto;
import demo.bookingservice.dtos.internal.SeatValidationRequest;
import demo.bookingservice.dtos.internal.ShowSeatValidationResponse;
import demo.bookingservice.dtos.internal.ValidatedSeatDto;
import demo.bookingservice.entities.Booking;
import demo.bookingservice.entities.ShowSeat;
import demo.bookingservice.enums.BookingStatus;
import demo.bookingservice.enums.Role;
import demo.bookingservice.enums.SeatType;
import demo.bookingservice.enums.ShowSeatStatus;
import demo.bookingservice.exceptions.SeatUnavailableException;
import demo.bookingservice.messaging.BookingEventProducer;
import demo.bookingservice.repos.BookingRepository;
import demo.bookingservice.repos.ShowSeatRepository;
import demo.bookingservice.security.AuthContextHolder;
import demo.bookingservice.security.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private ShowSeatRepository showSeatRepository;
    @Mock
    private CatalogClient catalogClient;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingEventProducer bookingEventProducer;

    @InjectMocks
    private BookingService bookingService;
    
    private MockedStatic<AuthContextHolder> mockedAuth;

    @BeforeEach
    void setUp() {
        mockedAuth = mockStatic(AuthContextHolder.class);
        mockedAuth.when(AuthContextHolder::getCurrentUser).thenReturn(new AuthenticatedUser(1L, Role.CUSTOMER, "user@test.com"));
    }

    @AfterEach
    void tearDown() {
        if (mockedAuth != null) {
            mockedAuth.close();
        }
    }

    @Test
    void testCheckoutSuccess() {
        CreateBookingRequestDto request = new CreateBookingRequestDto(10L, List.of(201L, 202L));
        
        List<ValidatedSeatDto> validatedSeats = List.of(
                new ValidatedSeatDto(201L, "REGULAR", new BigDecimal("10.0")),
                new ValidatedSeatDto(202L, "REGULAR", new BigDecimal("10.0"))
        );
        ShowSeatValidationResponse validationResponse = new ShowSeatValidationResponse(10L, 100L, validatedSeats);
        when(catalogClient.validateSeats(eq(10L), any(SeatValidationRequest.class))).thenReturn(validationResponse);

        Booking savedBooking = new Booking();
        savedBooking.setId(1001L);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        when(showSeatRepository.findByShowIdAndSeatId(10L, 201L)).thenReturn(Optional.empty());
        when(showSeatRepository.findByShowIdAndSeatId(10L, 202L)).thenReturn(Optional.empty());

        var response = bookingService.checkout(request);

        assertNotNull(response);
        assertEquals(1001L, response.bookingId());
        
        verify(showSeatRepository, times(2)).save(any(ShowSeat.class));
    }
    
    @Test
    void testCheckoutSeatUnavailable() {
        CreateBookingRequestDto request = new CreateBookingRequestDto(10L, List.of(201L));
        
        List<ValidatedSeatDto> validatedSeats = List.of(
                new ValidatedSeatDto(201L, "REGULAR", new BigDecimal("10.0"))
        );
        ShowSeatValidationResponse validationResponse = new ShowSeatValidationResponse(10L, 100L, validatedSeats);
        when(catalogClient.validateSeats(eq(10L), any(SeatValidationRequest.class))).thenReturn(validationResponse);

        Booking savedBooking = new Booking();
        savedBooking.setId(1001L);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        ShowSeat lockedSeat = new ShowSeat();
        lockedSeat.setStatus(ShowSeatStatus.LOCKED);
        lockedSeat.setLockedUntil(LocalDateTime.now().plusMinutes(5));
        when(showSeatRepository.findByShowIdAndSeatId(10L, 201L)).thenReturn(Optional.of(lockedSeat));

        assertThrows(SeatUnavailableException.class, () -> bookingService.checkout(request));
    }
}
