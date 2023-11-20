package plan.dienst.urologie;

import lombok.Getter;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Job {

    private Jobs.JobName jobEnum;
    private String name;
    private List<DayOfWeek> verfugbareTage = new ArrayList<>();
    private boolean oneDoctorPerWeek = true;
    private boolean differentDoctorsOnConsecutiveDays = false;
    private int maxPerMonthPerDoctor = 10;
    private int maxWeekendJobsPerMonth = 2;
    private int maxDoctorsPerDay = 1;
    private boolean dayJob = true;

    public static class Builder {
        private Jobs.JobName jobEnum;
        private String name;
        private List<DayOfWeek> verfugbareTage;
        private boolean oneDoctorPerWeek;
        private boolean differentDoctorsOnConsecutiveDays;
        private int maxPerMonthPerDoctor;
        private int maxWeekendJobsPerMonth;
        private int maxDoctorsPerDay;
        private boolean dayJob;

        public Builder setEnum(Jobs.JobName jobEnum) {
            this.jobEnum = jobEnum;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setVerfugbareTage(DayOfWeek... dayOfWeeks) {
            this.verfugbareTage = new ArrayList<>();
            Collections.addAll(this.verfugbareTage, dayOfWeeks);
            return this;
        }

        public Builder setOneDoctorPerWeek(boolean oneDoctorPerWeek) {
            this.oneDoctorPerWeek = oneDoctorPerWeek;
            return this;
        }

        public Builder setDifferentDoctorsOnConsecutiveDays(boolean differentDoctorsOnConsecutiveDays) {
            this.differentDoctorsOnConsecutiveDays = differentDoctorsOnConsecutiveDays;
            return this;
        }

        public Builder setMaxPerMonthPerDoctor(int maxPerMonthPerDoctor) {
            this.maxPerMonthPerDoctor = maxPerMonthPerDoctor;
            return this;
        }

        public Builder setMaxPerMonthPerDoctorOnWeekends(int maxWeekendJobsPerMonth) {
            this.maxWeekendJobsPerMonth = maxWeekendJobsPerMonth;
            return this;
        }

        public Builder setMaxDoctorsPerDay(int maxDoctorsPerDay) {
            this.maxDoctorsPerDay = maxDoctorsPerDay;
            return this;
        }

        public Builder setDayJob(boolean dayJob) {
            this.dayJob = dayJob;
            return this;
        }

        public Job build() {
            Job job = new Job();
            job.jobEnum = jobEnum;
            job.name = name;
            job.verfugbareTage = verfugbareTage;
            job.oneDoctorPerWeek = oneDoctorPerWeek;
            job.differentDoctorsOnConsecutiveDays = differentDoctorsOnConsecutiveDays;
            job.maxPerMonthPerDoctor = maxPerMonthPerDoctor;
            job.maxWeekendJobsPerMonth = maxWeekendJobsPerMonth;
            job.maxDoctorsPerDay = maxDoctorsPerDay;
            job.dayJob = dayJob;
            return job;
        }
    }

}
