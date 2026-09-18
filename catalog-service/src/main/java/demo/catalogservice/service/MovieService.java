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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
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
    public List<MovieResponseDto> getMovies() {
        log.info("Fetching all movies");
        List<MovieResponseDto> movies = movieRepository.findAll().stream().map(this::toDto).toList();
        log.info("Found {} movies", movies.size());
        return movies;
    }

    /**
     * @throws MovieNotFoundException if no movie exists with the given id
     */
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

    /** Search-bar lookup as the user types a movie title; matches anywhere in the title, case-insensitively. */
    public List<MovieResponseDto> getMovieByTitle(String title) {
        log.info("Searching for movies with title containing '{}'", title);
        List<MovieResponseDto> movies = movieRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::toDto)
                .toList();
        log.info("Found {} movies matching title '{}'", movies.size(), title);
        return movies;
    }

    /** Movies shown on the "now showing" / "upcoming" home screen listings. */
    public List<MovieResponseDto> getMoviesByStatus(MovieStatus status) {
        log.info("Fetching movies with status {}", status);
        List<MovieResponseDto> movies = movieRepository.findByStatus(status).stream().map(this::toDto).toList();
        log.info("Found {} movies with status {}", movies.size(), status);
        return movies;
    }

    /** Browsing movies filtered by a genre name (e.g. "Action", "Comedy"), case-insensitively. */
    public List<MovieResponseDto> getMoviesByGenre(String genre) {
        log.info("Fetching movies with genre '{}'", genre);
        List<MovieResponseDto> movies = movieRepository.findByGenres_NameIgnoreCase(genre).stream()
                .map(this::toDto)
                .toList();
        log.info("Found {} movies with genre '{}'", movies.size(), genre);
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
