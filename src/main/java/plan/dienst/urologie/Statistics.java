package plan.dienst.urologie;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Component
public class Statistics {

    private final Jobs jobs;
    private final Doctors doctors;
    private final DataFinder dataFinder;

    public Statistics(Jobs jobs, Doctors doctors, DataFinder dataFinder) {
        this.jobs = jobs;
        this.doctors = doctors;
        this.dataFinder = dataFinder;
    }

    /**
     * Bewertung vom Dienstplan. Having empty job is very bad, having different workloads for doctors is not good.
     * @return one number summarizing the score
     */
    public double getScore(Dienstplan dienstplan, int year, int quartil) {
        // empty jobs
        double score = 0;
        int emptyJobScore = getEmptyJobs(dienstplan) * 100;
        // doctors without jobs per day
        int notWorkingScore = doctorsNotWorking(dienstplan) * 20;
        // uneven job distribution
        double shiftDistScore = getShiftDistributionScore(getShiftDistribution(dienstplan, year, quartil)) * 10;
        double OPdistScore = getOPdistributionScore(getOPdistribution(dienstplan, year, quartil));
        System.out.println("scores: empty jobs " + emptyJobScore + ", not working " + notWorkingScore + ", shift dist " + shiftDistScore + ", OP dist " + OPdistScore);
        score = OPdistScore; //shiftDistScore + emptyJobScore + notWorkingScore +
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

    public Map<Doctor, Double> getShiftDistribution(Dienstplan dienstplan, int year, int quartil) {
        Map<Doctor, Double> shiftDistribution = new HashMap<>();
        List<Doctor> docsWithAvailableDays = doctors.getAllDoctors().stream().filter(
                doctor -> !doctor.getVerfugbareTage().isEmpty()).toList();
        double ratio;
        double sum = 0;
        int amount = 0;
        for (Doctor doc : docsWithAvailableDays) {
            int totalAvailable = doc.getMaxDiensteImMonat() * 3;
            int totalWorked = dataFinder.getTimesDoctorScheduledThisQuarter(dienstplan, year, quartil, doc, jobs.getDienst());
            ratio = (double) totalWorked / (double) totalAvailable;
            shiftDistribution.put(doc, ratio);
            sum += ratio;
            amount++;
        }
        double average = sum / amount;
        for (Doctor doc : docsWithAvailableDays) {
            double distFromAve = Math.abs(shiftDistribution.get(doc) - average);
            shiftDistribution.put(doc, distFromAve);
        }
        return shiftDistribution;
    }

    public Map<Doctor, Double> getOPdistribution(Dienstplan dienstplan, int year, int quartil) {
        Map<Doctor, Double> OPdistribution = new HashMap<>();
        double ratio;
        double sum = 0;
        int amount = 0;
        List<Doctor> docsWithAvailableDays = doctors.getAllDoctors().stream().filter(
                doctor -> !doctor.getVerfugbareTage().isEmpty()).toList();
        for (Doctor doc : docsWithAvailableDays) {
            int totalWorked = dataFinder.getTimesDoctorScheduledThisQuarter(dienstplan, year, quartil, doc, jobs.getOp());
            ratio = (double) totalWorked;
            OPdistribution.put(doc, ratio);
            sum += ratio;
            amount++;
        }
        double average = sum / amount;
        for (Doctor doc : docsWithAvailableDays) {
            double distFromAve = Math.abs(OPdistribution.get(doc) - average);
            OPdistribution.put(doc, distFromAve);
        }
        return OPdistribution;
    }

    public Map<Doctor, Double> getOPdistributionNormalized(Dienstplan dienstplan, int year, int quartil) {
        Map<Doctor, Double> OPdistribution = new HashMap<>();
        int availableWeekdays = 0;
        double ratio;
        double sum = 0;
        int amount = 0;
        List<Doctor> docsWithAvailableDays = doctors.getAllDoctors().stream().filter(
                doctor -> !doctor.getVerfugbareTage().isEmpty()).toList();
        for (Doctor doc : docsWithAvailableDays) {
            if (doc.getVerfugbareTage().contains(DayOfWeek.MONDAY)) availableWeekdays++;
            if (doc.getVerfugbareTage().contains(DayOfWeek.TUESDAY)) availableWeekdays++;
            if (doc.getVerfugbareTage().contains(DayOfWeek.WEDNESDAY)) availableWeekdays++;
            if (doc.getVerfugbareTage().contains(DayOfWeek.THURSDAY)) availableWeekdays++;
            if (doc.getVerfugbareTage().contains(DayOfWeek.FRIDAY)) availableWeekdays++;
            int totalWorked = dataFinder.getTimesDoctorScheduledThisQuarter(dienstplan, year, quartil, doc, jobs.getOp());
            ratio = (double) totalWorked / (double) availableWeekdays;
            OPdistribution.put(doc, ratio);
            sum += ratio;
            amount++;
        }
        double average = sum / amount;
        for (Doctor doc : docsWithAvailableDays) {
            double distFromAve = Math.abs(OPdistribution.get(doc) - average);
            OPdistribution.put(doc, distFromAve);
        }
        return OPdistribution;
    }

    public int getEmptyJobs(Dienstplan dienstplan) {
        int empty = 0;
        for (LocalDate date = dienstplan.getStartDate(); !date.isAfter(dienstplan.getEndDate()); date = date.plusDays(1)) {
            for (Job job : jobs.getAllJobs())
                if (!dienstplan.isAssigned(date, job)) {
                    empty++;
                }
        }
        return empty;
    }

    public int doctorsNotWorking(Dienstplan dienstplan) {
        int notWorking = 0;
        for (LocalDate date = dienstplan.getStartDate(); !date.isAfter(dienstplan.getEndDate()); date = date.plusDays(1)) {
            if (!Dates.isWeekend(date)) {
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
