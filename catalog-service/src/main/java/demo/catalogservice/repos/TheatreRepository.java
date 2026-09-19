package demo.catalogservice.repos;

import demo.catalogservice.entities.Theatre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TheatreRepository extends JpaRepository<Theatre, Long> {
    /** "Pick your city" step: theatres available in the city the user selected. */
    Page<Theatre> findByCityIgnoreCase(String city, Pageable pageable);

    /** Search-bar lookup as the user types a theatre name. */
    Page<Theatre> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /** A vendor's own theatres, for their "my theatres" management screen. */
    Page<Theatre> findByVendorId(Long vendorId, Pageable pageable);
}
