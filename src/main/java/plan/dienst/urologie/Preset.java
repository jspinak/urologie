package plan.dienst.urologie;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Preset {

    private Jobs.JobName jobName;
    private Doctors.DocName docName;
    private LocalDate date;

    public Preset(Jobs.JobName jobName, Doctors.DocName docName, LocalDate date) {
        this.jobName = jobName;
        this.docName = docName;
        this.date = date;
    }
}
