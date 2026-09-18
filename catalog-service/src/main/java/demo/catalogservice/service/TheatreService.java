package demo.catalogservice.service;

import demo.catalogservice.dto.TheatreResponseDto;
import demo.catalogservice.entities.Theatre;
import demo.catalogservice.exceptions.TheatreNotFoundException;
import demo.catalogservice.repos.TheatreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** Read-only theatre lookups backing the "pick a theatre" booking flow. */
@Service
@RequiredArgsConstructor
@Slf4j
public class TheatreService {

    private final TheatreRepository theatreRepository;
    private final ModelMapper modelMapper;

    /** All theatres in the catalog. */
    public List<TheatreResponseDto> getTheatres() {
        log.info("Fetching all theatres");
        List<TheatreResponseDto> theatres = theatreRepository.findAll().stream().map(this::toDto).toList();
        log.info("Found {} theatres", theatres.size());
        return theatres;
    }

    /**
     * @throws TheatreNotFoundException if no theatre exists with the given id
     */
    public TheatreResponseDto getTheatreById(Long theatreId) {
        log.info("Fetching theatre with id {}", theatreId);
        Theatre theatre = theatreRepository
                .findById(theatreId)
                .orElseThrow(() -> {
                    log.warn("No theatre found with id {}", theatreId);
                    return new TheatreNotFoundException("No theatre found with id " + theatreId);
                });
        return toDto(theatre);
    }

    /** "Pick your city" step: theatres available in the city the user selected. */
    public List<TheatreResponseDto> getTheatresByCity(String city) {
        log.info("Fetching theatres in city '{}'", city);
        List<TheatreResponseDto> theatres = theatreRepository.findByCityIgnoreCase(city).stream()
                .map(this::toDto)
                .toList();
        log.info("Found {} theatres in city '{}'", theatres.size(), city);
        return theatres;
    }

    /** Search-bar lookup as the user types a theatre name. */
    public List<TheatreResponseDto> getTheatresByName(String name) {
        log.info("Searching for theatres with name containing '{}'", name);
        List<TheatreResponseDto> theatres = theatreRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::toDto)
                .toList();
        log.info("Found {} theatres matching name '{}'", theatres.size(), name);
        return theatres;
    }

    private TheatreResponseDto toDto(Theatre theatre) {
        return modelMapper.map(theatre, TheatreResponseDto.class);
    }
}
