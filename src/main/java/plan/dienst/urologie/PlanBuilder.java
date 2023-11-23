package plan.dienst.urologie;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import static plan.dienst.urologie.Jobs.JobName.URLAUB;

@Component
public class PlanBuilder {
    private final Jobs jobs;
    private final Doctors doctors;
    private final DataFinder dataFinder;
    private final DayJobDecider dayJobDecider;
    private final ShiftDecider shiftDecider;
    private final ShiftOrganizer shiftOrganizer;
    private final Explanations explanations;
    private final Statistics statistics;

    public PlanBuilder(Jobs jobs, Doctors doctors, DataFinder dataFinder,
                       DayJobDecider dayJobDecider, ShiftDecider shiftDecider, ShiftOrganizer shiftOrganizer,
                       Explanations explanations, Statistics statistics) {
        this.jobs = jobs;
        this.doctors = doctors;
        this.dataFinder = dataFinder;
        this.dayJobDecider = dayJobDecider;
        this.shiftDecider = shiftDecider;
        this.shiftOrganizer = shiftOrganizer;
        this.explanations = explanations;
        this.statistics = statistics;
    }

    private void addPreset(Dienstplan dienstplan, Preset preset) {
        dienstplan.assignDoctors(preset.getDate(),
                jobs.getJobMap().get(preset.getJobName()), doctors.getDoctorMap().get(preset.getDocName()));
    }

    private void addPresets(Dienstplan dienstplan, Presets presets) {
        presets.getPresets().forEach(preset -> addPreset(dienstplan, preset));
    }

    public void dienst(Dienstplan dienstplan, Doctors.DocName doctor, LocalDate date) {
        dienstplan.assignDoctors(date, jobs.getDienst(), doctors.getDoctorMap().get(doctor));
    }

    public void urlaub(Dienstplan dienstplan, Doctors.DocName doctor, LocalDate date) {
        dienstplan.assignDoctors(date, jobs.getUrlaub(), doctors.getDoctorMap().get(doctor));
    }

    public void makePlans(Presets presets, int jahr, int quartil) {
        List<Dienstplan> dienstplans = new ArrayList<>();
        Dienstplan bestPlan = makePlan(presets, jahr, quartil);
        double bestScore = bestPlan.getScore();
        for (int i=0; i<100; i++) {
            Dienstplan dienstplan = makePlan(presets, jahr, quartil);
            dienstplans.add(dienstplan);
            System.out.println("score " + dienstplan.getScore());
            if (dienstplan.getScore() < bestScore) {
                bestPlan = dienstplan;
                bestScore = dienstplan.getScore();
            }
        }
        bestPlan.printSchedule();
        printDoctorStats(bestPlan,quartil*3-2, quartil*3-1, quartil*3);
        System.out.println("best score = " + bestScore);
    }

    public Dienstplan makePlan(Presets presets, int jahr, int quartil) {
        Dienstplan dienstplan = new Dienstplan();
        dienstplan.initDates(jahr, quartil);
        explanations.init(dienstplan.getStartDate(), dienstplan.getEndDate());
        addPresets(dienstplan, presets);
        shiftOrganizer.scheduleShifts(dienstplan, jahr, quartil);
        for (LocalDate date : dienstplan.getDates()) {
            assignDoctorsToDayJob(dienstplan, date, jobs.getEaz());
            assignDoctorsToDayJob(dienstplan, date, jobs.getZna());
            assignDoctorsToDayJob(dienstplan, date, jobs.getStation());
            assignDoctorsToDayJob(dienstplan, date, jobs.getOp());
        }
        dienstplan.setScore(statistics.getScore(dienstplan, jahr, quartil));
        return dienstplan;
    }

    private void assignDoctorsToDayJob(Dienstplan dienstplan, LocalDate date, Job job) {
        List<Doctor> docsSortedByJobsDoneLast30Days = dataFinder.sortByScheduledJobsThisMonth(dienstplan,
                date, doctors.getAllDoctors(), job);
        for (Doctor doc : docsSortedByJobsDoneLast30Days) {
            if (dayJobDecider.canWork(dienstplan, doc, date, job)) {
                dienstplan.assignDoctors(date, job, doc);
            }
        }
    }

    private void assignDoctorsToShift(Dienstplan dienstplan, LocalDate date) {
        List<Doctor> docsSortedByJobsDoneLast30Days = dataFinder.sortByScheduledJobsThisMonth(dienstplan,
                date, doctors.getAllDoctors(), jobs.getDienst());
        for (Doctor doc : docsSortedByJobsDoneLast30Days) {
            if (shiftDecider.canWorkShift(dienstplan, doc, date)) {
                dienstplan.assignDoctors(date, jobs.getDienst(), doc);
                return;
            }
        }
    }

    public void printDoctorStats(Dienstplan dienstplan, int... months) {
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
                    dataFinder.sortByScheduledJobsThisMonth(dienstplan, startDate, doctors.getAllDoctors(), job);
                    timesWorked[i] = doctor.getCalc();
                    sum += timesWorked[i];
                }
                for (int j : timesWorked) System.out.printf("%-" + fieldWidthNumber + "s", j);
                System.out.printf("%-" + fieldWidthNumber + "s%.1f", sum, (double)sum/timesWorked.length);
                System.out.println();
            }
            System.out.println();
        }
        printPlanDeficiencies(dienstplan, months);
    }

    private void printPlanDeficiencies(Dienstplan dienstplan, int... months) {
        boolean deficienciesExist = false;
        LocalDate startDate = LocalDate.of(2024, months[0], 1);
        LocalDate endDate = LocalDate.of(2024, months[months.length-1], 1);
        System.out.println("Plan Deficiencies");
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Set<Doctor> docsAssigned = new HashSet<>();
            for (Job job : jobs.getAllJobs()) {
                if (printIfDeficient(dienstplan, date, job)) deficienciesExist = true;
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
                    deficienciesExist = true;
                    System.out.println(d + " doc should be working: " + doc.getName());
                    for (Job j : jobs.getAllJobs()) printExplanations(dienstplan, date, j);
                }
            }
        }
        if (!deficienciesExist) System.out.println("(no deficiencies found)");
    }

    private boolean printIfDeficient(Dienstplan dienstplan, LocalDate date, Job job) {
        boolean deficienciesExist = false;
        boolean jobScheduled = job.getVerfugbareTage().contains(date.getDayOfWeek());
        List<Doctor> assignedDoctors = dienstplan.getAssignedDoctors(date, job);
        double fullness = 0;
        for (Doctor doctor : assignedDoctors) {
            if (doctor.isVollzeit()) fullness++;
            else fullness += .5;
        }
        boolean jobEmpty = fullness == 0;
        boolean jobNotFull = job.getJobEnum().equals(Jobs.JobName.DIENST) ? fullness < .5 : fullness < 1;
        if (jobScheduled && jobEmpty) { //jobNotFull) {
            deficienciesExist = true;
            System.out.println("_________________");
            System.out.println("\n" + date + " " + date.getDayOfWeek());
            for (Job j : jobs.getAllJobs()) printExplanations(dienstplan, date, j);
        }
        return deficienciesExist;
    }

    private void printExplanations(Dienstplan dienstplan, LocalDate date, Job job) {
        List<Doctor> assignedDoctors = dienstplan.getAssignedDoctors(date, job);
        System.out.print("\n" + job.getName() + " |");
        assignedDoctors.forEach(doc -> System.out.print(" " + doc.getName()));
        System.out.println();
        for (Map.Entry<Doctors.DocName, List<String>> exp :
                explanations.getExplanations(date, job.getJobEnum()).entrySet()) {
            System.out.print("  " + exp.getKey() + " ");
            for (String str : exp.getValue()) System.out.print("  " + str);
            System.out.println();
        }
    }

}
