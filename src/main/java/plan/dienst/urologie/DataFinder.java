package plan.dienst.urologie;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Component
public class DataFinder {
    private final Jobs jobs;

    public DataFinder(Jobs jobs) {
        this.jobs = jobs;
    }

    public List<Doctor> getDoctorsDayBefore(Dienstplan dienstplan, LocalDate date, Job job) {
        return dienstplan.ifAssignedGetDoctors(date.minusDays(1), job);
    }

    public List<Doctor> getDoctorsDayBefore(Dienstplan dienstplan, LocalDate date, int daysBefore, Job job) {
        return dienstplan.ifAssignedGetDoctors(date.minusDays(daysBefore), job);
    }

    public int getTimesDoctorScheduledThisQuarter(Dienstplan dienstplan, int year, int quartil, Doctor doctor, Job job) {
        int timesScheduled = 0;
        for (int i=0; i<3; i++) {
            timesScheduled += getTimesDoctorScheduledThisMonth(dienstplan,
                    LocalDate.of(year, quartil*3-2+i, 1),
                    doctor, job);
        }
        return timesScheduled;
    }

    public int getTimesDoctorScheduledThisMonth(Dienstplan dienstplan, LocalDate date, Doctor doctor, Job job) {
        return getTimesDoctorScheduledThisMonth(dienstplan, date, doctor, job, DayOfWeek.values());
    }

    // can calculate weekend shifts, for example
    public int getTimesDoctorScheduledThisMonth(Dienstplan dienstplan, LocalDate date, Doctor doctor, Job job, DayOfWeek... daysToUse) {
        int didJob = 0;
        int month = date.getMonthValue();
        int year = date.getYear();
        LocalDate startDate = LocalDate.of(year, month, 1);
        int year2 = month == 12 ? year + 1 : year;
        int month2 = month == 12 ? 1 : month + 1;
        LocalDate endDate = LocalDate.of(year2, month2, 1).minusDays(1);
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            if (List.of(daysToUse).contains(d.getDayOfWeek())) {
                if (dienstplan.isDoctorWorking(d, job, doctor)) didJob++;
            }
        }
        return didJob;
    }

    public List<Doctor> sortByScheduledJobsThisMonth(Dienstplan dienstplan, LocalDate date, List<Doctor> doctors, Job job) {
        doctors.forEach(doc -> doc.setCalc(getTimesDoctorScheduledThisMonth(dienstplan, date, doc, job)));
        doctors.sort(Comparator.comparingDouble(Doctor::getCalc));
        return doctors;
    }

    public List<Doctor> sortDoctorsByShiftAvailability(Dienstplan dienstplan, int year, int month, List<Doctor> doctors) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        doctors.forEach(doc -> {
            int maxShifts = Math.min(doc.getMaxDiensteImMonat(), jobs.getDienst().getMaxPerMonthPerDoctor());
            doc.setCalc(maxShifts - getTimesDoctorScheduledThisMonth(dienstplan, startDate, doc, jobs.getDienst()));
        });
        doctors.sort(Comparator.comparingDouble(Doctor::getCalc).reversed());
        return doctors;
    }

    public int getAvailableJobsLeftThisMonth(Dienstplan dienstplan, LocalDate date, Doctor doctor, Job job) {
        int maxShifts = job.getMaxPerMonthPerDoctor();
        if (job == jobs.getDienst()) maxShifts = Math.min(doctor.getMaxDiensteImMonat(), maxShifts);
        return maxShifts - getTimesDoctorScheduledThisMonth(dienstplan, date, doctor, job);
    }

}
