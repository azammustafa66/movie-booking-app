package demo.catalogservice.entities;

import demo.catalogservice.enums.Certification;
import demo.catalogservice.enums.MovieStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private LocalDate releaseDate;

    @Enumerated(EnumType.STRING)
    private Certification certification;

    private Integer durationInMinutes;

    private String posterUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovieStatus status;

    // Batch-fetched rather than @EntityGraph'd: this collection gets touched once per
    // row by every paged movie-list DTO conversion, but @EntityGraph on a @ManyToMany
    // combined with Pageable forces Hibernate into in-memory pagination (fetching every
    // matching row before paging in Java) — worse than the N+1 it would fix. @BatchSize
    // instead loads genres for up to 20 movies in one IN-clause query, keeping the
    // original query's DB-level paging intact.
    @ManyToMany
    @JoinTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @BatchSize(size = 20)
    private Set<Genre> genres = new HashSet<>();
}