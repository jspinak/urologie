package plan.dienst.urologie;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Component
public class Statistics {

    private final Dienstplan dienstplan;
    private final Jobs jobs;
    private final Doctors doctors;
    private final Dates dates;
    private final DataFinder dataFinder;

    public Statistics(Dienstplan dienstplan, Jobs jobs, Doctors doctors, Dates dates, DataFinder dataFinder) {
        this.dienstplan = dienstplan;
        this.jobs = jobs;
        this.doctors = doctors;
        this.dates = dates;
        this.dataFinder = dataFinder;
    }

    /**
     * Bewertung vom Dienstplan. Having empty job is very bad, having different workloads for doctors is not good.
     * @return one number summarizing the score
     */
    public double getScore(int year, int quartil) {
        // empty jobs
        double score = 0;
        score += getEmptyJobs() * 100;
        // doctors without jobs per day
        score += doctorsNotWorking() * 20;
        // uneven job distribution
        score += getShiftDistributionScore(getShiftDistribution(year, quartil)) * 10;
        score += getOPdistributionScore(getOPdistribution(year, quartil));
        return score;
    }

    // perfect distribution returns 0
    public double getShiftDistributionScore(Map<Doctor, Double> shiftDistribution) {
        double score = 0;
        for (double dist : shiftDistribution.values()) score += dist;
        return score;
    }

    public double getOPdistributionScore(Map<Doctor, Double> opDistribution) {
        double score = 0;
        for (double dist : opDistribution.values()) score += dist;
        return score;
    }

    public Map<Doctor, Double> getShiftDistribution(int year, int quartil) {
        Map<Doctor, Double> shiftDistribution = new HashMap<>();
        for (Doctor doc : doctors.getAllDoctors()) {
            int totalAvailable = doc.getMaxDiensteImMonat() * 3;
            int totalWorked = dataFinder.getTimesDoctorScheduledThisQuarter(year, quartil, doc, jobs.getDienst());
            shiftDistribution.put(doc, (double) totalWorked / (double) totalAvailable);
        }
        OptionalDouble average = shiftDistribution.values().stream().mapToDouble(Double::doubleValue).average();
        for (Doctor doc : doctors.getAllDoctors()) {
            double distFromAve = shiftDistribution.get(doc) - average.getAsDouble();
            shiftDistribution.put(doc, distFromAve);
        }
        return shiftDistribution;
    }

    public Map<Doctor, Double> getOPdistribution(int year, int quartil) {
        Map<Doctor, Double> OPdistribution = new HashMap<>();
        int availableWeekdays = 0;
        for (Doctor doc : doctors.getAllDoctors()) {
            if (doc.getVerfugbareTage().contains(DayOfWeek.MONDAY)) availableWeekdays++;
            if (doc.getVerfugbareTage().contains(DayOfWeek.TUESDAY)) availableWeekdays++;
            if (doc.getVerfugbareTage().contains(DayOfWeek.WEDNESDAY)) availableWeekdays++;
            if (doc.getVerfugbareTage().contains(DayOfWeek.THURSDAY)) availableWeekdays++;
            if (doc.getVerfugbareTage().contains(DayOfWeek.FRIDAY)) availableWeekdays++;
            int totalWorked = dataFinder.getTimesDoctorScheduledThisQuarter(year, quartil, doc, jobs.getOp());
            OPdistribution.put(doc, (double) totalWorked / (double) availableWeekdays);
        }
        OptionalDouble average = OPdistribution.values().stream().mapToDouble(Double::doubleValue).average();
        for (Doctor doc : doctors.getAllDoctors()) {
            double distFromAve = OPdistribution.get(doc) - average.getAsDouble();
            OPdistribution.put(doc, distFromAve);
        }
        return OPdistribution;
    }

    public int getEmptyJobs() {
        int empty = 0;
        for (LocalDate date = dienstplan.getStartDate(); !date.isAfter(dienstplan.getEndDate()); date = date.plusDays(1)) {
            for (Job job : jobs.getAllJobs())
                if (!dienstplan.isAssigned(date, job)) empty++;
        }
        return empty;
    }

    public int doctorsNotWorking() {
        int notWorking = 0;
        for (LocalDate date = dienstplan.getStartDate(); !date.isAfter(dienstplan.getEndDate()); date = date.plusDays(1)) {
            if (!dates.isWeekend(date)) {
                for (Doctor doc : doctors.getAllDoctors()) {
                    if (!dienstplan.getAssignedDoctors(date.minusDays(1), jobs.getDienst()).contains(doc)
                            && (doc.getVerfugbareTage().contains(date.getDayOfWeek())
                            || doc.getVerfugbareTageDienst().contains(date.getDayOfWeek()))
                            && !dienstplan.getAssignedDoctors(date, jobs.getAllJobs().toArray(new Job[0])).contains(doc)) {
                        notWorking++;
                    }
                }
            }
        }
        return notWorking;
    }

    // show a table with the number of jobs done per doctor (doctors horizontally)
    public void printJobFrequencyPerDoctor() {

    }
}
