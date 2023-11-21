package plan.dienst.urologie;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static plan.dienst.urologie.Jobs.JobName.DIENST;
import static plan.dienst.urologie.Jobs.JobName.URLAUB;

public class DailyPlan {

    private LocalDate date;
    private Map<Job, List<Doctor>> plan = new HashMap<>();

    public DailyPlan(LocalDate date) {
        this.date = date;
    }

    public void addDoctors(Job job, List<Doctor> doctors) {
        if (plan.get(job) == null) {
            List<Doctor> docs = new ArrayList<>(doctors);
            plan.put(job, docs);
        } else {
            plan.get(job).addAll(doctors);
        }
    }

    public void addDoctors(Job job, Doctor... doctors) {
        addDoctors(job, List.of(doctors));
    }

    public List<Doctor> getDoctors(Job job) {
        if (!plan.containsKey(job)) return new ArrayList<>();
        return plan.get(job);
    }

    public boolean hasJob(Job job) {
        return plan.containsKey(job);
    }

    public List<Doctor> getDoctors(Jobs.JobName jobName) {
        for (Job job : plan.keySet()) {
            if (job.getJobEnum() == jobName) return plan.get(job);
        }
        return new ArrayList<>();
    }

    public void print() {
        int fieldWidth = 10;
        for (Jobs.JobName jobName : Jobs.JobName.values()) {
            boolean isVacation = jobName == URLAUB;
            boolean isDayJob = jobName != DIENST && jobName != URLAUB;
            boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
            if (!isVacation && (!isDayJob || !isWeekend)) {
                System.out.printf("\t %-" + fieldWidth + "s", jobName);
                for (Doctor doc : getDoctors(jobName)) {
                    System.out.print(" " + doc.getName());
                }
                System.out.println();
            }
        }
        List<Doctor> urlauber = getDoctors(URLAUB);
        if (urlauber.isEmpty()) return;
        System.out.printf("\t %-" + fieldWidth + "s", "Urlaub");
        for (Doctor doc : urlauber) {
            System.out.print(" " + doc.getName());
        }
        System.out.println();
    }

}
