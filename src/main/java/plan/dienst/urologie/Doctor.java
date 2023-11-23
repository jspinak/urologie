package plan.dienst.urologie;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Doctor {

    private Doctors.DocName docEnum;
    private String name;
    private boolean vollzeit = true;
    private List<DayOfWeek> verfugbareTage = new ArrayList<>();
    private List<DayOfWeek> verfugbareTageDienst = new ArrayList<>();
    private int maxDiensteImMonat = 4;
    private double shiftPrioritizationFactor = 1;
    private int calc; // for temp calculations (i.e. how many weekends in last month worked a shift)

    public boolean isAvailableOn(DayOfWeek dayOfWeek, Jobs.JobName jobName) {
        if (jobName == Jobs.JobName.DIENST) return verfugbareTageDienst.contains(dayOfWeek);
        return verfugbareTage.contains(dayOfWeek);
    }

    public boolean isAvailableEveryDay(Jobs.JobName jobName) {
        for (DayOfWeek day : DayOfWeek.values())
            if (!isAvailableOn(day, jobName)) return false;
        return true;
    }

    public static class Builder {

        private Doctors.DocName docEnum;
        private String name;
        private boolean vollzeit;
        private List<DayOfWeek> verfugbareTage;
        private List<DayOfWeek> verfugbareTageDienst;
        private int maxDiensteImMonat;

        public Builder setEnum(Doctors.DocName docEnum) {
            this.docEnum = docEnum;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setVollzeit(boolean isVollzeit) {
            this.vollzeit = isVollzeit;
            return this;
        }

        public Builder setVerfugbareTage(DayOfWeek... daysOfWeek) {
            this.verfugbareTage = List.of(daysOfWeek);
            return this;
        }

        public Builder setVerfugbareTageDienst(DayOfWeek... daysOfWeek) {
            this.verfugbareTageDienst = List.of(daysOfWeek);
            return this;
        }

        public Builder setMaxDiensteImMonat(int maxDiensteImMonat) {
            this.maxDiensteImMonat = maxDiensteImMonat;
            return this;
        }

        public Doctor build() {
            Doctor doctor = new Doctor();
            doctor.docEnum = docEnum;
            doctor.name = name;
            doctor.vollzeit = vollzeit;
            doctor.verfugbareTage = verfugbareTage;
            doctor.verfugbareTageDienst = verfugbareTageDienst;
            doctor.maxDiensteImMonat = maxDiensteImMonat;
            return doctor;
        }

    }

}
