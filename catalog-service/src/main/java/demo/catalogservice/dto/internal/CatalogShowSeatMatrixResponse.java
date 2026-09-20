package demo.catalogservice.dto.internal;

import java.util.List;

public record CatalogShowSeatMatrixResponse(
        Long showId,
        Long screenId,
        List<CatalogSeatDto> seats
) {}