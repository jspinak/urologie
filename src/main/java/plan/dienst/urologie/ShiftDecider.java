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

    public ShiftDecider(Jobs jobs, Doctors doctors, DataFinder dataFinder, Dienstplan dienstplan, Explanations explanations) {
        this.jobs = jobs;
        this.doctors = doctors;
        this.dataFinder = dataFinder;
        this.dienstplan = dienstplan;
        this.explanations = explanations;
    }

    public boolean canWorkShift(Doctor doctor, LocalDate date) {
        boolean shiftAvailable = isShiftAvailable(date);
        boolean couldWorkOP = couldWorkOP(doctor, date);
        boolean weekday = isWeekday(date);
        boolean weekend = isWeekend(date);
        boolean workedTooManyShiftsThisMonth = hasWorkedTooManyShiftsThisMonth(doctor, date);
        boolean workedTooManyWeekendShiftsThisMonth = hasWorkedTooManyWeekendShiftsThisMonth(doctor, date);
        boolean workedShiftDayBefore = isWorkedShiftDayBefore(doctor, date);
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
        return true;
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

    private boolean isWeekday(LocalDate date) {
        return (date.getDayOfWeek() != SATURDAY) && (date.getDayOfWeek() != SUNDAY);
    }

    private boolean isWeekend(LocalDate date) {
        return !isWeekday(date);
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
