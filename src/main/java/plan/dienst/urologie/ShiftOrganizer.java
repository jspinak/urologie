package plan.dienst.urologie;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
public class ShiftOrganizer {

    private final ShiftDecider shiftDecider;
    private final Doctors doctors;
    private final Dienstplan dienstplan;
    private final Jobs jobs;
    private final DataFinder dataFinder;
    private final Explanations explanations;
    private final Dates dates;

    private List<int[]> docOrder = new ArrayList<>();
    {
        docOrder.add(new int[]{0, 1, 2});
        docOrder.add(new int[]{0, 2, 1});
        docOrder.add(new int[]{1, 0, 2});
        docOrder.add(new int[]{1, 2, 0});
        docOrder.add(new int[]{2, 0, 1});
        docOrder.add(new int[]{2, 1, 0});
    }

    private List<int[]> docPerDay = new ArrayList<>();
    {
        docPerDay.add(new int[]{0,1,0,2,1,2});
        docPerDay.add(new int[]{0,1,2,0,1,2});
        docPerDay.add(new int[]{0,1,2,1,0,2});
        docPerDay.add(new int[]{0,1,2,1,2,0});
    }

    public ShiftOrganizer(ShiftDecider shiftDecider, Doctors doctors, Dienstplan dienstplan,
                          Jobs jobs, DataFinder dataFinder, Explanations explanations, Dates dates) {
        this.shiftDecider = shiftDecider;
        this.doctors = doctors;
        this.dienstplan = dienstplan;
        this.jobs = jobs;
        this.dataFinder = dataFinder;
        this.explanations = explanations;
        this.dates = dates;
    }

    /*
    1. Decide who has the most constraints for Dienst
    2. Schedule those doctors first
    3. Look at who has the most amount of remaining Dienste (max - scheduled)
    4. Schedule those doctors. Try for max 3 doctors per week for Dienste.
    5. When all Dienste are scheduled, then do the day jobs
     */
    public void scheduleShifts(int year, int quartil) {
        LocalDate date = LocalDate.of(year, quartil*3-2, 1);
        //scheduleConstrainedDoctors(year, month);
        LocalDate sunday = dates.getNearestNextSundayTo(date);
        while (sunday.getMonthValue() <= quartil*3) {
            for (int i=0; i<200; i++)
                if (scheduleAllShiftsForOneWeek(sunday)) {
                    System.out.println(sunday + " " + i+1 + " iterations of weekly Dienst scheduling");
                    break;
                }
            sunday = sunday.plusDays(7);
        }
        // schedule the rest individually if needed
        scheduleIndividually(date, quartil);
    }

    private void scheduleIndividually(LocalDate startDate, int quartil) {
        LocalDate endDate = LocalDate.of(startDate.getYear(), quartil*3+1, 1).minusDays(1);
        for (LocalDate date=startDate; !date.isAfter(endDate.plusDays(5)); date=date.plusDays(1)) {
            if (!dienstplan.isAssigned(date, jobs.getDienst())) {
                for (Doctor doctor : doctors.getAllDoctors())
                    if (shiftDecider.canWorkShift(doctor, date)) dienstplan.assignDoctors(date, jobs.getDienst(), doctor);
            }
        }

    }

    private boolean scheduleShifts(LocalDate date) {
        if (scheduleAllShiftsForOneWeek(date, true)) return true;
        for (int i=0; i<100; i++) {
            if (scheduleAllShiftsForOneWeek(date, false)) return true;
        }
        return false;
    }

    private boolean scheduleAllShiftsForOneWeek(LocalDate sunday) {
        List<Doctor> docs3 = getDoctorsWithTotalAvailableDays(sunday, 6);
        if (docs3.size() < 3) return false;
        return tryCombination(sunday, docs3);
    }

    /*
    if the combination doesn't work, try a different set of doctors
     */
    private boolean scheduleAllShiftsForOneWeek(LocalDate date, boolean select1DocWithConstraints) {
        List<Doctor> docs3;
        if (select1DocWithConstraints) docs3 = get3DoctorsMax1WithConstraints(date);
        else docs3 = get3RandomDoctors(date);
        if (docs3.size() < 3) return false;
        return tryCombination(date, docs3);
    }

    private int getDoc(int day, int[] dOrder, int[] dPerDay) {
        return dOrder[dPerDay[day]];
    }

    /*
    find a way to fill 6 shifts with 3 doctors
    don't put doctors with constraints together, it makes it impossible to find a combination

     */
    private boolean tryCombination(LocalDate date, List<Doctor> docs) {
        Doctor[] schedule;
        for (int[] dOrder : docOrder) {
            for (int[] dPerDay : docPerDay) {
                schedule = new Doctor[6]; // get empty schedule
                for (int i=0; i<6; i++) {
                    Doctor doc = docs.get(getDoc(i, dOrder, dPerDay));
                    LocalDate dt = date.plusDays(i);
                    if (shiftDecider.canWorkShift(doc, dt)) schedule[i] = doc;
                }
                if (allCellsFull(schedule)) {
                    for (int i=0; i<6; i++) {
                        Doctor doc = docs.get(getDoc(i, dOrder, dPerDay));
                        LocalDate dt = date.plusDays(i);
                        dienstplan.assignDoctors(dt, jobs.getDienst(), doc);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean allCellsFull(Doctor[] docs) {
        for (Doctor doc : docs) if (doc == null) return false;
        return true;
    }

    private void assignDocs(LocalDate date, Doctor[] doctors) {
        for (int i=0; i<doctors.length; i++) {
            dienstplan.assignDoctors(date.plusDays(i), jobs.getDienst(), doctors[i]);
            dienstplan.assignDoctors(date.plusDays(i), jobs.getOp(), doctors[i]);
        }
    }

    public int availabilitySunToFri(LocalDate sunday, Doctor doctor) {
        int avail = 0;
        for (LocalDate date=sunday; !date.isAfter(sunday.plusDays(5)); date=date.plusDays(1)) {
            if (shiftDecider.canWorkShift(doctor, date)) avail++;
        }
        return avail;
    }

    /**
     * Get 3 Doctors to fill the shifts in a week. Each Doctor must have 2 free shifts.
     * @return the list of 3 doctors.
     */
    private List<Doctor> get3DoctorsMax1WithConstraints(LocalDate sunday) {
        List<Doctor> docs3 = new ArrayList<>();
        boolean listHasDocWithConstraints = false;
        int availableJobsThisMonth;
        boolean hasConstraints, constraintCondition, atLeast2JobsAvailable;
        sortDoctorsByShiftsWorkedLastMonth(sunday.getYear(), sunday.getMonthValue());
        for (Doctor doc : doctors.getAllDoctors()) {
            hasConstraints = !doc.isAvailableEveryDay(Jobs.JobName.DIENST);
            availableJobsThisMonth = dataFinder.getAvailableJobsLeftThisMonth(sunday, doc, jobs.getDienst());
            constraintCondition = !hasConstraints || !listHasDocWithConstraints;
            atLeast2JobsAvailable = availableJobsThisMonth > 1;
            if (constraintCondition && atLeast2JobsAvailable) {
                docs3.add(doc);
                if (hasConstraints) listHasDocWithConstraints = true;
            }
            if (docs3.size() > 2) break;
        }
        return docs3;
    }

    /*
    If the doctors selected don't work, we need another set of 3 doctors.
     */
    private List<Doctor> get3RandomDoctors(LocalDate date) {
        List<Doctor> availableDocs = new ArrayList<>();
        doctors.getAllDoctors().forEach(doc -> {
            if (dataFinder.getAvailableJobsLeftThisMonth(date, doc, jobs.getDienst()) > 2) availableDocs.add(doc);
        });
        List<Doctor> docs3 = new ArrayList<>();
        Set<Integer> uniqueNumbers = get3UniqueRandom(availableDocs.size());
        uniqueNumbers.forEach(n -> docs3.add(availableDocs.get(n)));
        return docs3;
    }

    private Set<Integer> get3UniqueRandom(int maxRange) {
        Set<Integer> uniqueNumbers = new HashSet<>();
        Random random = new Random();
        if (maxRange < 3) return new HashSet<>();
        while (uniqueNumbers.size() < 3) {
            int randomNumber = random.nextInt(maxRange);
            uniqueNumbers.add(randomNumber);
        }
        return uniqueNumbers;
    }

    /*
    This calculates the days available for a shift per doctor for this specific week.
    The total is the sum of all 3 doctors.
    Having a total > 14 should give a positive result (6 + 6 + 2 is guaranteed to work)
     */
    private List<Doctor> getDoctorsWithTotalAvailableDays(LocalDate sunday, int minTotalAvailable) {
        doctors.getAllDoctors().forEach(doc -> doc.setCalc(availabilitySunToFri(sunday, doc)));
        List<Doctor> potentialDocs = new ArrayList<>(doctors.getAllDoctors()).stream().filter(
                doc -> doc.getCalc() > 1).toList(); // each doc must have at least 2 days available
        for (int i=0; i<200; i++) {
            List<Doctor> docs3 = new ArrayList<>();
            Set<Integer> unique3 = get3UniqueRandom(potentialDocs.size());
            unique3.forEach(n -> {
                Doctor doc = potentialDocs.get(n);
                for (int j=0; j<Math.round(doc.getShiftPrioritizationFactor()); j++) docs3.add(doc); // adds more than once according to the prioritization factor
            });
            if (docs3.stream().mapToInt(Doctor::getCalc).sum() >= minTotalAvailable) return docs3;
        }
        return new ArrayList<>();
    }

    private List<Doctor> get3DoctorsPrioritizeConstraints(LocalDate date) {
        List<Doctor> docs3 = new ArrayList<>();
        // first get Doctors with constraints
        List<Doctor> constrainedDocs = new ArrayList<>(sortDoctorsByConstraints());
        List<Doctor> lessWorkedDocs = new ArrayList<>(sortDoctorsByShiftsWorkedLastMonth(date.getYear(), date.getMonthValue()));
        for (Doctor doc : constrainedDocs)
            if (dataFinder.getAvailableJobsLeftThisMonth(date, doc, jobs.getDienst()) >= 2)
                if (docs3.size() < 3) docs3.add(doc);
        if (docs3.size() == 3) return docs3;
        for (Doctor doc : lessWorkedDocs)
            if (dataFinder.getAvailableJobsLeftThisMonth(date, doc, jobs.getDienst()) >= 2)
                if (docs3.size() < 3) docs3.add(doc);
        return docs3;
    }

    private List<Doctor> sortDoctorsByConstraints() {
        for (Doctor doctor : doctors.getAllDoctors()) {
            doctor.setCalc(doctor.getVerfugbareTageDienst().size());
        }
        doctors.getAllDoctors().sort(Comparator.comparingInt(Doctor::getCalc));
        return doctors.getAllDoctors();
    }

    private List<Doctor> sortDoctorsByShiftsWorkedLastMonth(int year, int month) {
        int y = (month > 1) ? year : year - 1;
        int m = (month > 1) ? month - 1 : 12;
        dataFinder.sortDoctorsByShiftAvailability(y, m, doctors.getAllDoctors());
        return doctors.getAllDoctors();
    }

    private boolean scheduleByShiftsWorked(int year, int month) {
        sortDoctorsByShiftsWorkedLastMonth(year, month);
        boolean allDoctorsFullShifts = true;
        //System.out.println("\n_____ remaining dienste _____");
        for (Doctor doctor : doctors.getAllDoctors()) {
            //System.out.println(doctor.getName() + " " + doctor.getCalc() + " month-" + month);
            if (scheduleDoctor(2, doctor, year, month)) allDoctorsFullShifts = false;
        }
        return allDoctorsFullShifts;
    }

    private void scheduleConstrainedDoctors(int year, int month) {
        sortDoctorsByConstraints();
        int notConstrained = 7;
        for (Doctor doctor : doctors.getAllDoctors()) {
            if (doctor.getVerfugbareTageDienst().size() < notConstrained && !doctor.getVerfugbareTageDienst().isEmpty()) {
                scheduleDoctor(5, doctor, year, month);
            }
        }
    }

    // schedules until full or maxToSchedule reached
    private boolean scheduleDoctor(int maxToSchedule, Doctor doctor, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        int scheduled = 0;
        boolean allShiftsScheduled = false;
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (shiftDecider.canWorkShift(doctor, date)) {
                if (jobs.getOp().getVerfugbareTage().contains(date.getDayOfWeek())) {
                    dienstplan.assignDoctors(date, jobs.getOp(), doctor);
                }
                dienstplan.assignDoctors(date, jobs.getDienst(), doctor);
                scheduled++;
            }
            if (scheduled == maxToSchedule || maxShiftsInMonth(doctor, year, month)) {
                //System.out.println("max shifts reached: "+doctor.getName()+" "+date);
                explanations.addExplanation(date, Jobs.JobName.DIENST, doctor.getDocEnum(), "Doc has worked too many shifts this month.");
                return maxShiftsInMonth(doctor, year, month);
            }
        }
        return maxShiftsInMonth(doctor, year, month);
    }

    private boolean maxShiftsInMonth(Doctor doctor, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        int maxShifts = Math.min(doctor.getMaxDiensteImMonat(),jobs.getDienst().getMaxPerMonthPerDoctor());
        return maxShifts <= dataFinder.getTimesDoctorScheduledThisMonth(startDate, doctor, jobs.getDienst());
    }


}
