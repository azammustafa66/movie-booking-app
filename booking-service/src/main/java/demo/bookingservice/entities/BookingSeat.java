package demo.bookingservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "booking_seats")
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", updatable = false, nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private Long seatId;

    @Column(nullable = false)
    private Long showId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
}