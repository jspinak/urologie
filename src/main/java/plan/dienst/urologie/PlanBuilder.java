package plan.dienst.urologie;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import static plan.dienst.urologie.Jobs.JobName.URLAUB;

@Component
public class PlanBuilder {

    private final Dienstplan dienstplan;
    private final Jobs jobs;
    private final Doctors doctors;
    private final DataFinder dataFinder;
    private final DayJobDecider dayJobDecider;
    private final ShiftDecider shiftDecider;
    private final ShiftOrganizer shiftOrganizer;
    private final Explanations explanations;

    public PlanBuilder(Dienstplan dienstplan, Jobs jobs, Doctors doctors, DataFinder dataFinder,
                       DayJobDecider dayJobDecider, ShiftDecider shiftDecider, ShiftOrganizer shiftOrganizer,
                       Explanations explanations) {
        this.dienstplan = dienstplan;
        this.jobs = jobs;
        this.doctors = doctors;
        this.dataFinder = dataFinder;
        this.dayJobDecider = dayJobDecider;
        this.shiftDecider = shiftDecider;
        this.shiftOrganizer = shiftOrganizer;
        this.explanations = explanations;
    }

    public void dienst(Doctors.DocName doctor, LocalDate date) {
        dienstplan.assignDoctors(date, jobs.getDienst(), doctors.getDoctorMap().get(doctor));
    }

    public void urlaub(Doctors.DocName doctor, LocalDate date) {
        dienstplan.assignDoctors(date, jobs.getUrlaub(), doctors.getDoctorMap().get(doctor));
    }

    public void makePlan(int jahr, int quartil) {
        dienstplan.initDates(jahr, quartil);
        explanations.init(dienstplan.getStartDate(), dienstplan.getEndDate());
        for (int i=quartil*3-2; i<=quartil*3; i++) shiftOrganizer.scheduleShifts(jahr, i);
        for (LocalDate date : dienstplan.getDates()) {
            assignDoctorsToDayJob(date, jobs.getEaz());
            assignDoctorsToDayJob(date, jobs.getZna());
            assignDoctorsToDayJob(date, jobs.getStation());
            assignDoctorsToDayJob(date, jobs.getOp());
            //assignDoctorsToShift(date);
        }
        dienstplan.printSchedule();
        printDoctorStats(quartil*3-2, quartil*3-1, quartil*3);
    }

    private void assignDoctorsToDayJob(LocalDate date, Job job) {
        List<Doctor> docsSortedByJobsDoneLast30Days = dataFinder.sortByScheduledJobsThisMonth(
                date, doctors.getAllDoctors(), job);
        for (Doctor doc : docsSortedByJobsDoneLast30Days) {
            if (dayJobDecider.canWork(doc, date, job)) {
                dienstplan.assignDoctors(date, job, doc);
            }
        }
    }

    private void assignDoctorsToShift(LocalDate date) {
        List<Doctor> docsSortedByJobsDoneLast30Days = dataFinder.sortByScheduledJobsThisMonth(
                date, doctors.getAllDoctors(), jobs.getDienst());
        for (Doctor doc : docsSortedByJobsDoneLast30Days) {
            if (shiftDecider.canWorkShift(doc, date)) {
                dienstplan.assignDoctors(date, jobs.getDienst(), doc);
                return;
            }
        }
    }

    public void printDoctorStats(int... months) {
        int fieldWidthText = 10;
        int fieldWidthNumber = 3;
        System.out.printf("\n\t %-" + fieldWidthText + "s", "Month");
        for (int month : months) System.out.printf("%-" + fieldWidthNumber + "s", month);
        System.out.printf("%-" + fieldWidthNumber + "s%s", "T", "D");
        System.out.println();
        for (Doctor doctor : new ArrayList<>(doctors.getAllDoctors())) {
            System.out.println(doctor.getName());
            for (Job job : jobs.getAllJobs()) {
                System.out.printf("\t %-" + fieldWidthText + "s", job.getName());
                int[] timesWorked = new int[months.length];
                int sum = 0;
                for (int i=0; i<months.length; i++) {
                    LocalDate startDate = LocalDate.of(2024, months[i], 1);
                    dataFinder.sortByScheduledJobsThisMonth(startDate, doctors.getAllDoctors(), job);
                    timesWorked[i] = doctor.getCalc();
                    sum += timesWorked[i];
                }
                for (int j : timesWorked) System.out.printf("%-" + fieldWidthNumber + "s", j);
                System.out.printf("%-" + fieldWidthNumber + "s%.1f", sum, (double)sum/timesWorked.length);
                System.out.println();
            }
            System.out.println();
        }
        printPlanDeficiencies(months);
    }

    private void printPlanDeficiencies(int... months) {
        LocalDate startDate = LocalDate.of(2024, months[0], 1);
        LocalDate endDate = LocalDate.of(2024, months[months.length-1], 1);
        System.out.println("Plan Deficiencies");
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Set<Doctor> docsAssigned = new HashSet<>();
            for (Job job : jobs.getAllJobs()) {
                printIfDeficient(date, job);
                List<Doctor> assigned = dienstplan.getAssignedDoctors(date, job);
                docsAssigned.addAll(assigned);
            }
            docsAssigned.addAll(dienstplan.getAssignedDoctors(date, jobs.getUrlaub()));
            DayOfWeek d = date.getDayOfWeek();
            for (Doctor doc : doctors.getAllDoctors()) {
                if (!docsAssigned.contains(doc) &&
                        (doc.getVerfugbareTage().contains(d) || doc.getVerfugbareTageDienst().contains(d))
                && !dienstplan.getAssignedDoctors(date.minusDays(1), jobs.getDienst()).contains(doc)
                && d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY) {
                    System.out.println(d + " doc should be working: " + doc.getName());
                    for (Job j : jobs.getAllJobs()) printExplanations(date, j);
                }
            }
        }
    }

    private void printIfDeficient(LocalDate date, Job job) {
        boolean jobScheduled = job.getVerfugbareTage().contains(date.getDayOfWeek());
        List<Doctor> assignedDoctors = dienstplan.getAssignedDoctors(date, job);
        double fullness = 0;
        for (Doctor doctor : assignedDoctors) {
            if (doctor.isVollzeit()) fullness++;
            else fullness += .5;
        }
        boolean jobNotFull = job.getJobEnum().equals(Jobs.JobName.DIENST) ? fullness < .5 : fullness < 1;
        if (jobScheduled && jobNotFull) {
            for (Job j : jobs.getAllJobs()) printExplanations(date, j);
        }
    }

    private void printExplanations(LocalDate date, Job job) {
        List<Doctor> assignedDoctors = dienstplan.getAssignedDoctors(date, job);
        System.out.print("\n" + job.getName() + " " + date + " " + date.getDayOfWeek());
        assignedDoctors.forEach(doc -> System.out.print(" " + doc.getName()));
        System.out.println();
        for (Map.Entry<Doctors.DocName, List<String>> exp :
                explanations.getExplanations(date, job.getJobEnum()).entrySet()) {
            System.out.print("  " + exp.getKey() + " ");
            for (String str : exp.getValue()) System.out.print("\t" + str);
            System.out.println();
        }
    }

}
