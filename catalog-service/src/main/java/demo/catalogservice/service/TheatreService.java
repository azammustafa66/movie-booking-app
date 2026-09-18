package demo.catalogservice.service;

import demo.catalogservice.dto.TheatreResponseDto;
import demo.catalogservice.entities.Theatre;
import demo.catalogservice.exceptions.TheatreNotFoundException;
import demo.catalogservice.repos.TheatreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** Read-only theatre lookups backing the "pick a theatre" booking flow. */
@Service
@RequiredArgsConstructor
@Slf4j
public class TheatreService {

    private final TheatreRepository theatreRepository;
    private final ModelMapper modelMapper;

    /** All theatres in the catalog. */
    public Page<TheatreResponseDto> getTheatres(Pageable pageable) {
        log.info("Fetching theatres (page {}, size {})", pageable.getPageNumber(), pageable.getPageSize());
        Page<TheatreResponseDto> theatres = theatreRepository.findAll(pageable).map(this::toDto);
        log.info("Found {} theatres (page {} of {})", theatres.getNumberOfElements(), theatres.getNumber() + 1, theatres.getTotalPages());
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
    public Page<TheatreResponseDto> getTheatresByCity(String city, Pageable pageable) {
        log.info("Fetching theatres in city '{}'", city);
        Page<TheatreResponseDto> theatres = theatreRepository.findByCityIgnoreCase(city, pageable).map(this::toDto);
        log.info("Found {} theatres in city '{}'", theatres.getTotalElements(), city);
        return theatres;
    }

    /** Search-bar lookup as the user types a theatre name. */
    public Page<TheatreResponseDto> getTheatresByName(String name, Pageable pageable) {
        log.info("Searching for theatres with name containing '{}'", name);
        Page<TheatreResponseDto> theatres = theatreRepository.findByNameContainingIgnoreCase(name, pageable).map(this::toDto);
        log.info("Found {} theatres matching name '{}'", theatres.getTotalElements(), name);
        return theatres;
    }

    private TheatreResponseDto toDto(Theatre theatre) {
        return modelMapper.map(theatre, TheatreResponseDto.class);
    }
}
