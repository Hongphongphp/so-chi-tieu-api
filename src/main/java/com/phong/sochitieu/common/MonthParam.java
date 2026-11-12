package com.phong.sochitieu.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

/** Đọc tham số tháng dạng "2026-11"; nếu bỏ trống thì lấy tháng hiện tại theo giờ Hàn Quốc. */
@Component
public class MonthParam {

    private final ZoneId zone;

    public MonthParam(@Value("${app.timezone}") String timezone) {
        this.zone = ZoneId.of(timezone);
    }

    public YearMonth parse(String month) {
        return (month == null || month.isBlank()) ? YearMonth.now(zone) : YearMonth.parse(month.trim());
    }

    public LocalDate today() {
        return LocalDate.now(Clock.system(zone));
    }
}
