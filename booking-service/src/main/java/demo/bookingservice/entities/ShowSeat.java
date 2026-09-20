package demo.bookingservice.entities;

import demo.bookingservice.enums.ShowSeatStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "show_seats",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"show_id", "seat_id"}
        )
)
@Getter
@Setter
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long showId;

    @Column(nullable = false)
    private Long seatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShowSeatStatus status;

    private Long bookingId;

    private LocalDateTime lockedUntil;
}