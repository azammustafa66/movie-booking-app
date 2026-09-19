package demo.catalogservice.service;

import demo.catalogservice.dto.MovieResponseDto;
import demo.catalogservice.entities.Genre;
import demo.catalogservice.entities.Movie;
import demo.catalogservice.enums.MovieStatus;
import demo.catalogservice.exceptions.MovieNotFoundException;
import demo.catalogservice.repos.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.TreeSet;
import java.util.stream.Collectors;

/** Read-only movie catalog lookups backing the browse/search screens. */
@Service
@RequiredArgsConstructor
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    private final ModelMapper modelMapper;

    /** All movies in the catalog, regardless of status. */
    public Page<MovieResponseDto> getMovies(Pageable pageable) {
        log.info("Fetching movies (page {}, size {})", pageable.getPageNumber(), pageable.getPageSize());
        Page<MovieResponseDto> movies = movieRepository.findAll(pageable).map(this::toDto);
        log.info("Found {} movies (page {} of {})", movies.getNumberOfElements(), movies.getNumber() + 1, movies.getTotalPages());
        return movies;
    }

    /**
     * @throws MovieNotFoundException if no movie exists with the given id
     */
    @Cacheable(key = "#movieId", value = "movies")
    public MovieResponseDto getMovieById(Long movieId) {
        log.info("Fetching movie with id {}", movieId);
        Movie movie = movieRepository
                .findById(movieId)
                .orElseThrow(() -> {
                    log.warn("No movie found with id {}", movieId);
                    return new MovieNotFoundException("No movie found with id " + movieId);
                });
        return toDto(movie);
    }

    /** Search-bar lookup as the user types a movie title; matches anywhere in the movieTitle, case-insensitively. */
    @Cacheable(
            key = "#movieTitle.trim().toLowerCase() + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort",
            value = "movieSearch")
    public Page<MovieResponseDto> getMovieByTitle(String movieTitle, Pageable pageable) {
        log.info("Searching for movies with movieTitle containing '{}'", movieTitle);
        Page<MovieResponseDto> movies = movieRepository.findByTitleContainingIgnoreCase(movieTitle, pageable).map(this::toDto);
        log.info("Found {} movies matching movieTitle '{}'", movies.getTotalElements(), movieTitle);
        return movies;
    }

    /** Movies shown on the "now showing" / "upcoming" home screen listings. */
    public Page<MovieResponseDto> getMoviesByStatus(MovieStatus status, Pageable pageable) {
        log.info("Fetching movies with status {}", status);
        Page<MovieResponseDto> movies = movieRepository.findByStatus(status, pageable).map(this::toDto);
        log.info("Found {} movies with status {}", movies.getTotalElements(), status);
        return movies;
    }

    /** Browsing movies filtered by a genre name (e.g. "Action", "Comedy"), case-insensitively. */
    public Page<MovieResponseDto> getMoviesByGenre(String genre, Pageable pageable) {
        log.info("Fetching movies with genre '{}'", genre);
        Page<MovieResponseDto> movies = movieRepository.findByGenres_NameIgnoreCase(genre, pageable).map(this::toDto);
        log.info("Found {} movies with genre '{}'", movies.getTotalElements(), genre);
        return movies;
    }

    /**
     * Maps the scalar fields via {@link ModelMapper} and flattens genres to their
     * names by hand, since ModelMapper has no notion of how to turn a
     * {@code Genre} into a {@code String}.
     */
    private MovieResponseDto toDto(Movie movie) {
        MovieResponseDto dto = modelMapper.map(movie, MovieResponseDto.class);
        dto.setGenres(movie.getGenres().stream()
                .map(Genre::getName)
                .collect(Collectors.toCollection(TreeSet::new)));
        return dto;
    }
}
