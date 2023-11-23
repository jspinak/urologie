package plan.dienst.urologie;

import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Presets {

    private List<Preset> presets = new ArrayList<>();

    public static class Builder {
        private List<Preset> presets = new ArrayList<>();
        public Builder dienst(Doctors.DocName doc, int monat, int tag) {
            addJob(Jobs.JobName.DIENST, doc, monat, tag, monat, tag);
            return this;
        }

        public Builder dienst(Doctors.DocName doc, int beginMonat, int beginTag, int endMonat, int endTag) {
            addJob(Jobs.JobName.DIENST, doc, beginMonat, beginTag, endMonat, endTag);
            return this;
        }

        public Builder urlaub(Doctors.DocName doc, int monat, int tag) {
            addJob(Jobs.JobName.URLAUB, doc, monat, tag, monat, tag);
            return this;
        }

        public Builder urlaub(Doctors.DocName doc, int beginMonat, int beginTag, int endMonat, int endTag) {
            addJob(Jobs.JobName.URLAUB, doc, beginMonat, beginTag, endMonat, endTag);
            return this;
        }

        private Builder addJob(Jobs.JobName jobName, Doctors.DocName doc, int beginMonat, int beginTag, int endMonat, int endTag) {
            LocalDate startDate = LocalDate.of(2024, beginMonat, beginTag);
            LocalDate endDate = LocalDate.of(2024, endMonat, endTag);
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                Preset preset = new Preset(jobName, doc, date);
                presets.add(preset);
            }
            return this;
        }

        public Presets build() {
            Presets p = new Presets();
            p.presets = presets;
            return p;
        }
    }
}
