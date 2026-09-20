package demo.catalogservice;

import demo.catalogservice.dto.request.AdminTheatreRequestDto;
import demo.catalogservice.dto.request.MovieRequestDto;
import demo.catalogservice.dto.request.ScreenRequestDto;
import demo.catalogservice.dto.request.ShowRequestDto;
import demo.catalogservice.dto.response.MovieResponseDto;
import demo.catalogservice.dto.response.ScreenResponseDto;
import demo.catalogservice.dto.response.TheatreResponseDto;
import demo.catalogservice.enums.Certification;
import demo.catalogservice.enums.MovieStatus;
import demo.catalogservice.enums.ScreenType;
import demo.catalogservice.service.admin.AdminCatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ShowOverlapTest {

    @Autowired
    private AdminCatalogService adminCatalogService;

    @Test
    @Transactional
    public void testOverlappingShowsNotPrevented() {
        // Create Theatre
        AdminTheatreRequestDto theatreReq = new AdminTheatreRequestDto();
        theatreReq.setName("Test Theatre");
        theatreReq.setAddress("123 Test St");
        theatreReq.setCity("Test City");
        theatreReq.setState("Test State");
        theatreReq.setPincode("123456");
        theatreReq.setVendorId(1L);
        TheatreResponseDto theatre = adminCatalogService.createTheatre(theatreReq);

        // Create Screen
        ScreenRequestDto screenReq = new ScreenRequestDto();
        screenReq.setName("Screen 1");
        screenReq.setTheatreId(theatre.getId());
        screenReq.setCapacity(100);
        screenReq.setScreenType(ScreenType.STANDARD);
        ScreenResponseDto screen = adminCatalogService.createScreen(screenReq);

        // Create Movie
        MovieRequestDto movieReq = new MovieRequestDto();
        movieReq.setTitle("Test Movie");
        movieReq.setDescription("A test movie");
        movieReq.setLanguage("English");
        movieReq.setReleaseDate(LocalDate.now());
        movieReq.setCertification(Certification.U);
        movieReq.setDurationInMinutes(120);
        movieReq.setPosterUrl("http://example.com/poster.jpg");
        movieReq.setStatus(MovieStatus.NOW_SHOWING);
        movieReq.setGenreIds(Collections.emptySet());
        MovieResponseDto movie = adminCatalogService.createMovie(movieReq);

        // Create Show 1
        ShowRequestDto show1 = new ShowRequestDto();
        show1.setMovieId(movie.getId());
        show1.setScreenId(screen.getId());
        show1.setStartTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        show1.setEndTime(LocalDateTime.now().plusDays(1).withHour(12).withMinute(0));
        var response1 = adminCatalogService.createShow(show1);
        assertNotNull(response1);

        // Create Show 2 (Overlapping: 11 AM to 1 PM overlaps with 10 AM to 12 PM)
        ShowRequestDto show2 = new ShowRequestDto();
        show2.setMovieId(movie.getId());
        show2.setScreenId(screen.getId());
        show2.setStartTime(LocalDateTime.now().plusDays(1).withHour(11).withMinute(0));
        show2.setEndTime(LocalDateTime.now().plusDays(1).withHour(13).withMinute(0));
        var response2 = adminCatalogService.createShow(show2);
        assertNotNull(response2);
        
        System.out.println("Both overlapping shows created successfully (No validation present!).");
    }
}
