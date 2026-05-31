package com.github.airmoment.flight.dto;

import java.time.LocalDate;

public record DailyOilPrice(LocalDate date, double price) {
}
