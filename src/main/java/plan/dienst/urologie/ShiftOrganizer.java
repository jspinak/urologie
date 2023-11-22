package plan.dienst.urologie;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;

@Component
public class ShiftOrganizer {

    private final ShiftDecider shiftDecider;
    private final Doctors doctors;
    private final Dienstplan dienstplan;
    private final Jobs jobs;
    private final DataFinder dataFinder;
    private final Explanations explanations;

    public ShiftOrganizer(ShiftDecider shiftDecider, Doctors doctors, Dienstplan dienstplan,
                          Jobs jobs, DataFinder dataFinder, Explanations explanations) {
        this.shiftDecider = shiftDecider;
        this.doctors = doctors;
        this.dienstplan = dienstplan;
        this.jobs = jobs;
        this.dataFinder = dataFinder;
        this.explanations = explanations;
    }

    /*
    1. Decide who has the most constraints for Dienst
    2. Schedule those doctors first
    3. Look at who has the most amount of remaining Dienste (max - scheduled)
    4. Schedule those doctors
    5. When all Dienste are scheduled, then do the day jobs
     */
    public void scheduleShifts(int year, int month) {
        scheduleConstrainedDoctors(year, month);
        boolean allDocsFull, allShiftsAssigned;
        for (int i=0; i<10; i++) {
            allDocsFull = scheduleByShiftsWorked(year, month);
            allShiftsAssigned = dienstplan.areAllAssignedInMonth(LocalDate.of(year, month, 1), jobs.getDienst());
            if (allDocsFull || allShiftsAssigned) return;
        }
    }

    private void sortDoctorsByConstraints() {
        for (Doctor doctor : doctors.getAllDoctors()) {
            doctor.setCalc(doctor.getVerfugbareTageDienst().size());
        }
        doctors.getAllDoctors().sort(Comparator.comparingInt(Doctor::getCalc));
    }

    private void sortDoctorsByShiftsWorkedLastMonth(int year, int month) {
        int y = (month > 1) ? year : year - 1;
        int m = (month > 1) ? month - 1 : 12;
        dataFinder.sortDoctorsByShiftAvailability(y, m, doctors.getAllDoctors());
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
