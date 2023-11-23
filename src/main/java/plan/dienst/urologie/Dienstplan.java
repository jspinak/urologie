package plan.dienst.urologie;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
public class Dienstplan {

    private Map<LocalDate, DailyPlan> schedule = new HashMap<>();
    private LocalDate startDate;
    private LocalDate endDate;
    private double score;

    public void initDates(int jahr, int quartil) {
        startDate = Dates.getStartDate(jahr, quartil);
        endDate = Dates.getEndDate(jahr, quartil);
        // Iterate over the range of dates
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDate finalDate = date;
            schedule.computeIfAbsent(date, k -> new DailyPlan(finalDate));
        }
    }

    public List<LocalDate> getDates() {
        List<LocalDate> dates = new ArrayList<>();
        for (Map.Entry<LocalDate, DailyPlan> entry : getSortedEntries()) {
            LocalDate date = entry.getKey();
            dates.add(date);
        }
        return dates;
    }

    public boolean isValidDate(LocalDate date) {
        return schedule.containsKey(date);
    }

    public boolean isAssigned(LocalDate date, Job job) {
        if (!isValidDate(date)) return false;
        return schedule.get(date).hasJob(job);
    }

    public boolean areAllAssignedInMonth(LocalDate date, Job job) {
        boolean everyJobDone = true;
        LocalDate startDate = LocalDate.of(date.getYear(), date.getMonth(), 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            if (!isAssigned(d, job)) everyJobDone = false;
        }
        return everyJobDone;
    }

    public List<Doctor> ifAssignedGetDoctors(LocalDate date, Job job) {
        if (!isAssigned(date, job)) return new ArrayList<>();
        return schedule.get(date).getDoctors(job);
    }

    public boolean isDoctorWorking(LocalDate date, Job job, Doctor doctor) {
        return getAssignedDoctors(date, job).contains(doctor);
    }

    public void assignDoctors(LocalDate date, Job job, Doctor... doctors) {
        // If the date is not in the schedule, create a new map
        schedule.computeIfAbsent(date, k -> new DailyPlan(date));
        // Assign the doctor to the shift for the given date
        schedule.get(date).addDoctors(job, doctors);
        //System.out.print(date + " " + job.getJobEnum());
        //for (Doctor doctor : doctors) System.out.print(" " + doctor.getName()); System.out.println();
    }

    // Method to get the doctor assigned to a specific shift on a given date
    public List<Doctor> getAssignedDoctors(LocalDate date, Job job) {
        DailyPlan dailyPlan = schedule.get(date);
        if (dailyPlan == null) return new ArrayList<>();
        return dailyPlan.getDoctors(job);
    }

    public List<Doctor> getAssignedDoctors(LocalDate date, Job... jobs) {
        DailyPlan dailyPlan = schedule.get(date);
        if (dailyPlan == null) return new ArrayList<>();
        List<Doctor> assigned = new ArrayList<>();
        for (Job job : jobs) {
            for (Doctor doctor : dailyPlan.getDoctors(job)) {
                if (!assigned.contains(doctor)) assigned.add(doctor);
            }
        }
        return assigned;
    }

    // Example method to print the entire schedule
    public void printSchedule() {
        System.out.println();
        for (Map.Entry<LocalDate, DailyPlan> entry : getSortedEntries()) {
            LocalDate date = entry.getKey();
            System.out.println(date + " " + date.getDayOfWeek());
            entry.getValue().print();
        }
    }

    private List<Map.Entry<LocalDate, DailyPlan>> getSortedEntries() {
        // Convert the map entries to a list for sorting
        List<Map.Entry<LocalDate, DailyPlan>> sortedEntries = new ArrayList<>(schedule.entrySet());
        // Sort the list based on LocalDate
        sortedEntries.sort(Map.Entry.comparingByKey());
        return sortedEntries;
    }

}
