package demo.catalogservice.service;

import demo.catalogservice.dto.ScreenResponseDto;
import demo.catalogservice.entities.Screen;
import demo.catalogservice.exceptions.ScreenNotFoundException;
import demo.catalogservice.repos.ScreenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** Read-only screen lookups backing the "pick a screen" / seat-map booking flow. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScreenService {

    private final ScreenRepository screenRepository;
    private final ModelMapper modelMapper;

    /** All screens across every theatre. */
    public List<ScreenResponseDto> getScreens() {
        log.info("Fetching all screens");
        List<ScreenResponseDto> screens = screenRepository.findAll().stream().map(this::toDto).toList();
        log.info("Found {} screens", screens.size());
        return screens;
    }

    /**
     * @throws ScreenNotFoundException if no screen exists with the given id
     */
    public ScreenResponseDto getScreenById(Long screenId) {
        log.info("Fetching screen with id {}", screenId);
        Screen screen = screenRepository
                .findById(screenId)
                .orElseThrow(() -> {
                    log.warn("No screen found with id {}", screenId);
                    return new ScreenNotFoundException("No screen found with id " + screenId);
                });
        return toDto(screen);
    }

    /** All screens belonging to a theatre, e.g. for that theatre's own listing page. */
    public List<ScreenResponseDto> getScreensByTheatre(Long theatreId) {
        log.info("Fetching screens for theatre with id {}", theatreId);
        List<ScreenResponseDto> screens = screenRepository.findByTheatre_Id(theatreId).stream()
                .map(this::toDto)
                .toList();
        log.info("Found {} screens for theatre with id {}", screens.size(), theatreId);
        return screens;
    }

    private ScreenResponseDto toDto(Screen screen) {
        return modelMapper.map(screen, ScreenResponseDto.class);
    }
}
