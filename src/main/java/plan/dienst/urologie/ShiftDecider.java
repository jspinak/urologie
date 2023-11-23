package plan.dienst.urologie;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static java.time.DayOfWeek.*;

@Component
public class ShiftDecider {

    private final Jobs jobs;
    private final Doctors doctors;
    private final DataFinder dataFinder;
    private final Dienstplan dienstplan;
    private final Explanations explanations;
    private final Dates dates;

    public ShiftDecider(Jobs jobs, Doctors doctors, DataFinder dataFinder, Dienstplan dienstplan, Explanations explanations,
                        Dates dates) {
        this.jobs = jobs;
        this.doctors = doctors;
        this.dataFinder = dataFinder;
        this.dienstplan = dienstplan;
        this.explanations = explanations;
        this.dates = dates;
    }

    public boolean canWorkShift(Doctor doctor, LocalDate date) {
        boolean shiftAvailable = isShiftAvailable(date);
        boolean couldWorkOP = couldWorkOP(doctor, date);
        boolean weekday = !dates.isWeekend(date);
        boolean weekend = dates.isWeekend(date);
        boolean workedTooManyShiftsThisMonth = hasWorkedTooManyShiftsThisMonth(doctor, date);
        boolean workedTooManyWeekendShiftsThisMonth = hasWorkedTooManyWeekendShiftsThisMonth(doctor, date);
        boolean workedShiftDayBefore = isWorkedShiftDayBefore(doctor, date);
        boolean workedShiftLastWeekend = isWorkedShiftLastWeekend(doctor, date);
        boolean okForDoctorThatDay = isOkForDoctorThatDay(doctor, date);
        boolean hasVacation = dienstplan.isDoctorWorking(date, jobs.getUrlaub(), doctor);
        if (!shiftAvailable) {
            explanations.addExplanation(date, Jobs.JobName.DIENST, doctor.getDocEnum(), "Someone is already assigned for today's shift.");
            return false;
        }
        if (weekday && !couldWorkOP) {
            explanations.addExplanation(date, Jobs.JobName.DIENST, doctor.getDocEnum(), "Doc has vacation or is working a weekly day job.");
            return false;
        }
        if (weekend && workedTooManyWeekendShiftsThisMonth) {
            explanations.addExplanation(date, Jobs.JobName.DIENST, doctor.getDocEnum(), "Doc worked too many weekend shifts already this month.");
            return false;
        }
        if (workedTooManyShiftsThisMonth) {
            explanations.addExplanation(date, Jobs.JobName.DIENST, doctor.getDocEnum(), "Doc worked too many shifts already this month.");
            return false;
        }
        if (workedShiftDayBefore) {
            explanations.addExplanation(date, Jobs.JobName.DIENST, doctor.getDocEnum(), "Doc worked a shift yesterday.");
            return false;
        }
        if (!okForDoctorThatDay) {
            explanations.addExplanation(date, Jobs.JobName.DIENST, doctor.getDocEnum(), "Doc doens't work shifts this day of the week.");
            return false;
        }
        if (weekend && hasVacation) {
            explanations.addExplanation(date, Jobs.JobName.DIENST, doctor.getDocEnum(), "It's the weekend and the Doc has vacation today.");
            return false;
        }
        if (weekend && workedShiftLastWeekend) {
            explanations.addExplanation(date, Jobs.JobName.DIENST, doctor.getDocEnum(), "It's the weekend and the Doc did a shift last weekend.");
            return false;
        }
        return true;
    }

    private boolean isWorkedShiftLastWeekend(Doctor doctor, LocalDate date) {
        LocalDate sat;
        LocalDate sun;
        if (date.getDayOfWeek() == SATURDAY) sat = date.minusDays(7);
        else if (date.getDayOfWeek() == SUNDAY) sat = date.minusDays(8);
        else sat = dates.getNearestPreviousDayTo(date, SATURDAY);
        sun = sat.plusDays(1);
        return dienstplan.isDoctorWorking(sat, jobs.getDienst(), doctor) ||
                dienstplan.isDoctorWorking(sun, jobs.getDienst(), doctor);
    }

    private boolean isShiftAvailable(LocalDate date) {
        return !dienstplan.isAssigned(date, jobs.getDienst());
    }

    private boolean couldWorkOP(Doctor doctor, LocalDate date) {
        if (dienstplan.getAssignedDoctors(date, jobs.getEaz()).contains(doctor)) return false;
        if (dienstplan.getAssignedDoctors(date, jobs.getZna()).contains(doctor)) return false;
        if (dienstplan.getAssignedDoctors(date, jobs.getStation()).contains(doctor)) return false;
        if (dienstplan.getAssignedDoctors(date, jobs.getUrlaub()).contains(doctor)) return false;
        return true;
    }

    private boolean hasWorkedTooManyShiftsThisMonth(Doctor doctor, LocalDate date) {
        int timesWorked = dataFinder.getTimesDoctorScheduledThisMonth(date, doctor, jobs.getDienst());
        int timesAllowed = jobs.getDienst().getMaxPerMonthPerDoctor();
        int maxForDoctor = doctor.getMaxDiensteImMonat();
        return timesWorked >= Math.min(timesAllowed, maxForDoctor);
    }

    private boolean hasWorkedTooManyWeekendShiftsThisMonth(Doctor doctor, LocalDate date) {
        int timesWorked = dataFinder.getTimesDoctorScheduledThisMonth(date, doctor, jobs.getDienst(), SATURDAY, SUNDAY);
        int timesAllowed = jobs.getDienst().getMaxWeekendJobsPerMonth();
        return timesWorked >= timesAllowed;
    }

    private boolean isWorkedShiftDayBefore(Doctor doctor, LocalDate date) {
                    return dataFinder.getDoctorsDayBefore(date, 1, jobs.getDienst()).contains(doctor);
    }

    private boolean isOkForDoctorThatDay(Doctor doctor, LocalDate date) {
        return doctor.getVerfugbareTageDienst().contains(date.getDayOfWeek());
    }


}
