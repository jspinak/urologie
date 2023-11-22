package plan.dienst.urologie;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Component
public class DayJobDecider {
    private final Jobs jobs;
    private final Doctors doctors;
    private final DataFinder dataFinder;
    private final Dienstplan dienstplan;
    private final Dates dates;
    private final Explanations explanations;

    public DayJobDecider(Jobs jobs, Doctors doctors, DataFinder dataFinder, Dienstplan dienstplan, Dates dates,
                         Explanations explanations) {
        this.jobs = jobs;
        this.doctors = doctors;
        this.dataFinder = dataFinder;
        this.dienstplan = dienstplan;
        this.dates = dates;
        this.explanations = explanations;
    }

    public boolean canWork(Doctor doctor, LocalDate date, Job job) {
        boolean shiftFull = isJobFull(date, job);
        boolean needsPartTime = isNeedsPartTime(date, job);
        boolean docWorkingOtherJob = isWorkingAnotherJob(date, doctor);
        boolean jobDoneThisDay = isJobDoneThisDay(date, job);
        boolean workingDayBefore = isWorkingDayBefore(doctor, date, job);
        boolean needSameDocAllWeek = shouldBeSameDoctorAllWeek(job);
        boolean jobDoneDayBefore = isJobDoneDayBefore(date, job);
        boolean workedJobTooMuch = workedThisJobTooMuchThisMonth(date, job, doctor);
        boolean availableThisDay = isAvailableThisDay(date, doctor);
        boolean vacationThisWeek = isDoingJobThisWeek(date, doctor, jobs.getUrlaub());
        boolean vacationToday = dienstplan.getAssignedDoctors(date, jobs.getUrlaub()).contains(doctor);
        boolean jobOP = job == jobs.getOp();
        boolean doingShiftThisWeek = isDoingJobThisWeek(date, doctor, jobs.getDienst());
        boolean doesShiftDayBefore = isDoingShiftDayBefore(date, doctor);
        boolean partTimeDoc = !doctor.isVollzeit();
        boolean onePartTimeAvailableEveryDayThisWeek = atLeastOnePartTimeAvailableEveryDayThisWeek(date);
        boolean docHasThisDayAvailableForShifts = isShiftOK(doctor, date); // Doctors shouldn't work OP when they can't do a shift. On the other hand, some doctors are unavailable to work shifts on certain days but available for day jobs.
        boolean didAnotherWeeklyJobDayBefore = isDoingOtherWeeklyJobDayBefore(doctor, date, job);
        boolean docOnThisJobDayBeforeIsDifferentAndFullTime = isDocOnThisJobDayBeforeIsDifferentAndFullTime(doctor, date, job);

        if (!jobDoneThisDay) {
            explanations.addExplanation(date, job.getJobEnum(), doctor.getDocEnum(),
                    "This job isn't done today.");
            return false;
        }
        if (shiftFull) {
            explanations.addExplanation(date, job.getJobEnum(), doctor.getDocEnum(),
                    "Job is full (has enough doctors).");
            return false;
        }
        if (needsPartTime && doctor.isVollzeit()) {
            explanations.addExplanation(date, job.getJobEnum(), doctor.getDocEnum(),
                    "Requires a part-time doctor.");
            return false;
        }
        if (docWorkingOtherJob) {
            if (vacationToday) explanations.addExplanation(date, job.getJobEnum(), doctor.getDocEnum(),
                    "Doc is on vacation.");
            else explanations.addExplanation(date, job.getJobEnum(), doctor.getDocEnum(),
                    "Doc is working a different job today.");
            return false;
        }
        if (needSameDocAllWeek && jobDoneDayBefore && (!workingDayBefore && doctor.isVollzeit())) { // part-time doctors can share EAZ, ZNA, or Station and don't need to work every day of the week.
            explanations.addExplanation(date, job.getJobEnum(), doctor.getDocEnum(),
                    "Job requires the same doc every day this week.");
            return false;
        }
        if (workedJobTooMuch) {
            explanations.addExplanation(date, job.getJobEnum(), doctor.getDocEnum(),
                    "Doc has worked this job too much this month.");
            return false;
        }
        if (!availableThisDay) {
            explanations.addExplanation(date, job.getJobEnum(), doctor.getDocEnum(),
                    "Doc isn't available for this job this day of the week.");
            return false;
        }
        if ((vacationThisWeek || doingShiftThisWeek) && !jobOP && !partTimeDoc) {
            explanations.addExplanation(date, job.getJobEnum(), doctor.getDocEnum(),
                    "This job requires the same doc all week and this doc is working a shift or has vacation this week.");
            return false;
        }
        if (doesShiftDayBefore) {
            explanations.addExplanation(date, job.getJobEnum(), doctor.getDocEnum(),
                    "Doc did a shift yesterday.");
            return false;
        }
        if (!jobOP && partTimeDoc && !onePartTimeAvailableEveryDayThisWeek) {
            explanations.addExplanation(date, job.getJobEnum(), doctor.getDocEnum(),
                    "No part-time doctors are available at least 1 day this week.");
            return false;
        }
        //if (jobOP && !docHasThisDayAvailableForShifts) {
        //    explanations.addExplanation(date, job.getJobEnum(), doctor.getDocEnum(), "Doc can't work shift today.");
        //    return false;
        //}
        if (didAnotherWeeklyJobDayBefore) {
            explanations.addExplanation(date, job.getJobEnum(), doctor.getDocEnum(),
                    "Doc doing a different weekly job this week.");
            return false;
        }
        if (docOnThisJobDayBeforeIsDifferentAndFullTime && !jobOP) {
            explanations.addExplanation(date, job.getJobEnum(), doctor.getDocEnum(),
                    "Different and full-time doctor on this weekly job already.");
            return false;
        }
        return true;
    }

    private boolean isDocOnThisJobDayBeforeIsDifferentAndFullTime(Doctor doctor, LocalDate date, Job job) {
        List<Doctor> docs = dienstplan.getAssignedDoctors(date.minusDays(1), job);
        if (docs.isEmpty() || docs.contains(doctor)) return false;
        return docs.get(0).isVollzeit();
    }

    private boolean isDoingOtherWeeklyJobDayBefore(Doctor doctor, LocalDate date, Job job) {
        List<Job> otherWeeklyJobs = jobs.getAllJobs().stream().filter(j -> job.isOneDoctorPerWeek() && j != job).toList();
        for (Job j : otherWeeklyJobs) if (dienstplan.getAssignedDoctors(date, j).contains(doctor)) return true;
        return false;
    }

    private boolean isShiftOK(Doctor doctor, LocalDate date) {
        return doctor.getVerfugbareTageDienst().contains(date.getDayOfWeek());
    }

    private boolean atLeastOnePartTimeAvailableEveryDayThisWeek(LocalDate date) {
        LocalDate monday = dates.getNearestPreviousMondayTo(date);
        LocalDate friday = dates.getNearestNextDayTo(date, DayOfWeek.FRIDAY);
        List<Doctor> partTimers = doctors.getAllDoctors().stream().filter(doctor -> !doctor.isVollzeit()).toList();
        for (LocalDate d = monday; !d.isAfter(friday); d = d.plusDays(1)) {
            int available = 0;
            List<Doctor> notAvailable = dienstplan.getAssignedDoctors(d, jobs.getDienst(), jobs.getUrlaub());
            for (Doctor doctor : partTimers) {
                if (doctor.getVerfugbareTage().contains(d.getDayOfWeek())
                        && !notAvailable.contains(doctor)
                        && !dienstplan.getAssignedDoctors(d.minusDays(1), jobs.getDienst()).contains(doctor))
                    available++;
            }
            if (available == 0) return false;
        }
        return true;
    }

    private boolean isDoingShiftDayBefore(LocalDate date, Doctor doctor) {
        LocalDate dayBefore = date.minusDays(1);
        return dienstplan.getAssignedDoctors(dayBefore, jobs.getDienst()).contains(doctor);
    }

    private boolean isJobFull(LocalDate date, Job job) {
        return getJobFullness(date, job) >= job.getMaxDoctorsPerDay();
    }

    private double getJobFullness(LocalDate date, Job job) {
        List<Doctor> docsWorking = dienstplan.getAssignedDoctors(date, job);
        double full = 0;
        for (Doctor doc : docsWorking) full += fullTimeMultiplier(doc);
        return full;
    }

    private double fullTimeMultiplier(Doctor doctor) {
        if (doctor.isVollzeit()) return 1;
        return 0.5;
    }

    private boolean isNeedsPartTime(LocalDate date, Job job) {
        double fullness = getJobFullness(date, job);
        double jobMax = job.getMaxDoctorsPerDay();
        return (fullness > jobMax - 1) && (fullness < jobMax);
    }

    private boolean isWorkingAnotherJob(LocalDate date, Doctor doctor) {
        for (Job job : jobs.getDayJobs()) {
            if (dienstplan.getAssignedDoctors(date, job).contains(doctor)) return true;
        }
        return false;
    }

    private boolean isJobDoneThisDay(LocalDate date, Job job) {
        return job.getVerfugbareTage().contains(date.getDayOfWeek());
    }

    private boolean isWorkingDayBefore(Doctor doctor, LocalDate date, Job job) {
        return dataFinder.getDoctorsDayBefore(date, 1, job).contains(doctor);
    }

    private boolean shouldBeSameDoctorAllWeek(Job job) {
        return job.isOneDoctorPerWeek();
    }

    private boolean workedThisJobTooMuchThisMonth(LocalDate date, Job job, Doctor doc) {
        int timesDidJob = dataFinder.getTimesDoctorScheduledThisMonth(date, doc, job);
        return timesDidJob >= job.getMaxPerMonthPerDoctor();
    }

    private boolean isJobDoneDayBefore(LocalDate date, Job job) {
        return job.getVerfugbareTage().contains(date.minusDays(1).getDayOfWeek());
    }

    private boolean isAvailableThisDay(LocalDate date, Doctor doctor) {
        return doctor.getVerfugbareTage().contains(date.getDayOfWeek());
    }

    private boolean isDoingJobThisWeek(LocalDate date, Doctor doctor, Job job) {
        LocalDate monday = dates.getNearestPreviousMondayTo(date);
        LocalDate friday = dates.getNearestNextDayTo(date, DayOfWeek.FRIDAY);
        for (LocalDate d = monday; !d.isAfter(friday); d = d.plusDays(1)) {
            if (dienstplan.getAssignedDoctors(d, job).contains(doctor)) return true;
        }
        return false;
    }



}
