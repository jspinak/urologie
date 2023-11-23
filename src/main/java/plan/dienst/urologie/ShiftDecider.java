package plan.dienst.urologie;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static java.time.DayOfWeek.*;

@Component
public class ShiftDecider {

    private final Jobs jobs;
    private final DataFinder dataFinder;
    private final Explanations explanations;

    public ShiftDecider(Jobs jobs, DataFinder dataFinder, Explanations explanations) {
        this.jobs = jobs;
        this.dataFinder = dataFinder;
        this.explanations = explanations;
    }

    public boolean canWorkShift(Dienstplan dienstplan, Doctor doctor, LocalDate date) {
        boolean shiftAvailable = isShiftAvailable(dienstplan, date);
        boolean couldWorkOP = couldWorkOP(dienstplan, doctor, date);
        boolean weekday = !Dates.isWeekend(date);
        boolean weekend = Dates.isWeekend(date);
        boolean workedTooManyShiftsThisMonth = hasWorkedTooManyShiftsThisMonth(dienstplan, doctor, date);
        boolean workedTooManyWeekendShiftsThisMonth = hasWorkedTooManyWeekendShiftsThisMonth(dienstplan, doctor, date);
        boolean workedShiftDayBefore = isWorkedShiftDayBefore(dienstplan, doctor, date);
        boolean workedShiftLastWeekend = isWorkedShiftLastWeekend(dienstplan, doctor, date);
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

    private boolean isWorkedShiftLastWeekend(Dienstplan dienstplan, Doctor doctor, LocalDate date) {
        LocalDate sat;
        LocalDate sun;
        if (date.getDayOfWeek() == SATURDAY) sat = date.minusDays(7);
        else if (date.getDayOfWeek() == SUNDAY) sat = date.minusDays(8);
        else sat = Dates.getNearestPreviousDayTo(date, SATURDAY);
        sun = sat.plusDays(1);
        return dienstplan.isDoctorWorking(sat, jobs.getDienst(), doctor) ||
                dienstplan.isDoctorWorking(sun, jobs.getDienst(), doctor);
    }

    private boolean isShiftAvailable(Dienstplan dienstplan, LocalDate date) {
        return !dienstplan.isAssigned(date, jobs.getDienst());
    }

    private boolean couldWorkOP(Dienstplan dienstplan, Doctor doctor, LocalDate date) {
        if (dienstplan.getAssignedDoctors(date, jobs.getEaz()).contains(doctor)) return false;
        if (dienstplan.getAssignedDoctors(date, jobs.getZna()).contains(doctor)) return false;
        if (dienstplan.getAssignedDoctors(date, jobs.getStation()).contains(doctor)) return false;
        if (dienstplan.getAssignedDoctors(date, jobs.getUrlaub()).contains(doctor)) return false;
        return true;
    }

    private boolean hasWorkedTooManyShiftsThisMonth(Dienstplan dienstplan, Doctor doctor, LocalDate date) {
        int timesWorked = dataFinder.getTimesDoctorScheduledThisMonth(dienstplan, date, doctor, jobs.getDienst());
        int timesAllowed = jobs.getDienst().getMaxPerMonthPerDoctor();
        int maxForDoctor = doctor.getMaxDiensteImMonat();
        return timesWorked >= Math.min(timesAllowed, maxForDoctor);
    }

    private boolean hasWorkedTooManyWeekendShiftsThisMonth(Dienstplan dienstplan, Doctor doctor, LocalDate date) {
        int timesWorked = dataFinder.getTimesDoctorScheduledThisMonth(dienstplan, date, doctor, jobs.getDienst(), SATURDAY, SUNDAY);
        int timesAllowed = jobs.getDienst().getMaxWeekendJobsPerMonth();
        return timesWorked >= timesAllowed;
    }

    private boolean isWorkedShiftDayBefore(Dienstplan dienstplan, Doctor doctor, LocalDate date) {
                    return dataFinder.getDoctorsDayBefore(dienstplan, date, 1, jobs.getDienst()).contains(doctor);
    }

    private boolean isOkForDoctorThatDay(Doctor doctor, LocalDate date) {
        return doctor.getVerfugbareTageDienst().contains(date.getDayOfWeek());
    }


}
