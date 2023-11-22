package plan.dienst.urologie;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Component
public class Dates {

    public LocalDate getNearestPreviousMondayTo(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.MONDAY) {
            return date; // date is already monday
        } else {
            // Find the nearest previous Monday using TemporalAdjusters
            return date.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        }
    }

    public LocalDate getNearestPreviousMondayTo(int jahr, int monat, int tag) {
        LocalDate inputDate = LocalDate.of(jahr, monat, tag);
        return getNearestPreviousMondayTo(inputDate);
    }

    public LocalDate getNearestNextSundayTo(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return date; // date is already sunday
        } else {
            // Find the nearest next Sunday using TemporalAdjusters
            return date.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        }
    }

    public LocalDate getNearestPreviousDayTo(LocalDate date, DayOfWeek dayOfWeek) {
        if (date.getDayOfWeek() == dayOfWeek) {
            return date;
        } else return date.with(TemporalAdjusters.previous(dayOfWeek));
    }

    public LocalDate getNearestNextDayTo(LocalDate date, DayOfWeek dayOfWeek) {
        if (date.getDayOfWeek() == dayOfWeek) {
            return date;
        } else return date.with(TemporalAdjusters.next(dayOfWeek));
    }

    public LocalDate getNearestNextSundayTo(int jahr, int monat, int tag) {
        LocalDate inputDate = LocalDate.of(jahr, monat, tag);
        return getNearestPreviousMondayTo(inputDate);
    }

    public LocalDate getStartDate(int jahr, int quartil) {
        return getNearestPreviousMondayTo(jahr, quartil*3-2, 1);
    }

    public LocalDate getEndDate(int jahr, int quartil) {
        return getNearestNextSundayTo(jahr, quartil*3, 30); // could be more specific with 31 on some dates
    }

}
