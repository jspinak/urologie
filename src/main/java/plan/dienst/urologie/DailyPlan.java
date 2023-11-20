package plan.dienst.urologie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyPlan {

    private Map<Job, List<Doctor>> plan = new HashMap<>();

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

    public void print() {
        int fieldWidth = 10;
        for (Map.Entry<Job, List<Doctor>> entry : plan.entrySet()) {
            System.out.printf("\t %-" + fieldWidth + "s", entry.getKey().getName());
            for (Doctor doc : entry.getValue()) {
                System.out.print(" " + doc.getName());
            }
            System.out.println();
        }
    }

}
