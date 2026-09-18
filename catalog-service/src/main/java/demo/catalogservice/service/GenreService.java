package demo.catalogservice.service;

import demo.catalogservice.dto.GenreResponseDto;
import demo.catalogservice.entities.Genre;
import demo.catalogservice.exceptions.GenreNotFoundException;
import demo.catalogservice.repos.GenreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** Read-only genre lookups backing the movie browse/filter screens. */
@Service
@RequiredArgsConstructor
@Slf4j
public class GenreService {

    private final GenreRepository genreRepository;
    private final ModelMapper modelMapper;

    /** All genres, e.g. to populate a "filter by genre" dropdown. */
    public List<GenreResponseDto> getGenres() {
        log.info("Fetching all genres");
        List<GenreResponseDto> genres = genreRepository.findAll().stream().map(this::toDto).toList();
        log.info("Found {} genres", genres.size());
        return genres;
    }

    /**
     * @throws GenreNotFoundException if no genre exists with the given id
     */
    public GenreResponseDto getGenreById(Long genreId) {
        log.info("Fetching genre with id {}", genreId);
        Genre genre = genreRepository
                .findById(genreId)
                .orElseThrow(() -> {
                    log.warn("No genre found with id {}", genreId);
                    return new GenreNotFoundException("No genre found with id " + genreId);
                });
        return toDto(genre);
    }

    /**
     * @throws GenreNotFoundException if no genre exists with the given name
     */
    public GenreResponseDto getGenreByName(String name) {
        log.info("Fetching genre with name '{}'", name);
        Genre genre = genreRepository
                .findByName(name)
                .orElseThrow(() -> {
                    log.warn("No genre found with name '{}'", name);
                    return new GenreNotFoundException("No genre found with name " + name);
                });
        return toDto(genre);
    }

    private GenreResponseDto toDto(Genre genre) {
        return modelMapper.map(genre, GenreResponseDto.class);
    }
}
