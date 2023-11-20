package plan.dienst.urologie;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Explanations {
    private Map<LocalDate, Map<Jobs.JobName, Map<Doctors.DocName, List<String>>>> explanations = new HashMap<>();

    public void init(LocalDate startDate, LocalDate endDate) {
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            explanations.put(date, new HashMap<>());
            for (Jobs.JobName job : Jobs.JobName.values()) {
                explanations.get(date).put(job, new HashMap<>());
                for (Doctors.DocName doc : Doctors.DocName.values()) {
                    explanations.get(date).get(job).put(doc, new ArrayList<>());
                }
            }
        }
    }

    public void addExplanation(LocalDate date, Jobs.JobName job, Doctors.DocName doc, String explanation) {
        if (explanations.get(date) == null) init(date, date);
        Map<Jobs.JobName, Map<Doctors.DocName, List<String>>> dateExplanations = explanations.get(date);
        Map<Doctors.DocName, List<String>> jobExplanations = dateExplanations.get(job);
        List<String> docExplanations = jobExplanations.get(doc);
        docExplanations.add(explanation);
    }

    public List<String> getExplanations(LocalDate date, Jobs.JobName job, Doctors.DocName doc) {
        return explanations.get(date).get(job).get(doc);
    }

    public Map<Doctors.DocName, List<String>> getExplanations(LocalDate date, Jobs.JobName job) {
        return explanations.get(date).get(job);
    }
}
