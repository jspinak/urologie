package plan.dienst.urologie;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class Dates {

    public static LocalDate getNearestPreviousMondayTo(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.MONDAY) {
            return date; // date is already monday
        } else {
            // Find the nearest previous Monday using TemporalAdjusters
            return date.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        }
    }

    public static LocalDate getNearestPreviousMondayTo(int jahr, int monat, int tag) {
        LocalDate inputDate = LocalDate.of(jahr, monat, tag);
        return getNearestPreviousMondayTo(inputDate);
    }

    public static LocalDate getNearestNextSundayTo(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return date; // date is already sunday
        } else {
            // Find the nearest next Sunday using TemporalAdjusters
            return date.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        }
    }

    public static LocalDate getNearestPreviousDayTo(LocalDate date, DayOfWeek dayOfWeek) {
        if (date.getDayOfWeek() == dayOfWeek) {
            return date;
        } else return date.with(TemporalAdjusters.previous(dayOfWeek));
    }

    public static LocalDate getNearestNextDayTo(LocalDate date, DayOfWeek dayOfWeek) {
        if (date.getDayOfWeek() == dayOfWeek) {
            return date;
        } else return date.with(TemporalAdjusters.next(dayOfWeek));
    }

    public static LocalDate getNearestNextSundayTo(int jahr, int monat, int tag) {
        LocalDate inputDate = LocalDate.of(jahr, monat, tag);
        return getNearestPreviousMondayTo(inputDate);
    }

    public static LocalDate getStartDate(int jahr, int quartil) {
        return getNearestPreviousMondayTo(jahr, quartil*3-2, 1);
    }

    public static LocalDate getEndDate(int jahr, int quartil) {
        return LocalDate.of(jahr, quartil*3, 1).plusMonths(1).minusDays(1);
    }

    public static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

}
