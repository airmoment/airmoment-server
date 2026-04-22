package com.github.airmoment.flight.dto;

import java.util.List;

public record CachedFlightResult(
	List<CachedFlightItem> flights,
	Integer typicalPriceMin,
	Integer typicalPriceMax
) {
}
