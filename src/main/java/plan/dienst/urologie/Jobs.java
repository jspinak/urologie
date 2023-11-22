package plan.dienst.urologie;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.DayOfWeek.*;

@Component
@Getter
public class Jobs {

    public enum JobName {
        EAZ, ZNA, STATION, OP, DIENST, URLAUB
    }

    private Job eaz = new Job.Builder()
            .setEnum(JobName.EAZ)
            .setName("EAZ")
            .setVerfugbareTage(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)
            .setOneDoctorPerWeek(true)
            .setDifferentDoctorsOnConsecutiveDays(false)
            .setMaxPerMonthPerDoctor(10)
            .setMaxPerMonthPerDoctorOnWeekends(0)
            .setMaxDoctorsPerDay(1)
            .build();
    private Job zna = new Job.Builder()
            .setEnum(JobName.ZNA)
            .setName("ZNA")
            .setVerfugbareTage(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)
            .setOneDoctorPerWeek(true)
            .setDifferentDoctorsOnConsecutiveDays(false)
            .setMaxPerMonthPerDoctor(10)
            .setMaxPerMonthPerDoctorOnWeekends(0)
            .setMaxDoctorsPerDay(1)
            .build();
    private Job station = new Job.Builder()
            .setEnum(JobName.STATION)
            .setName("Station")
            .setVerfugbareTage(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)
            .setOneDoctorPerWeek(true)
            .setDifferentDoctorsOnConsecutiveDays(false)
            .setMaxPerMonthPerDoctor(10)
            .setMaxPerMonthPerDoctorOnWeekends(0)
            .setMaxDoctorsPerDay(1)
            .build();
    private Job op = new Job.Builder()
            .setEnum(JobName.OP)
            .setName("OP")
            .setVerfugbareTage(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)
            .setOneDoctorPerWeek(false)
            .setDifferentDoctorsOnConsecutiveDays(false)
            .setMaxPerMonthPerDoctor(31)
            .setMaxPerMonthPerDoctorOnWeekends(0)
            .setMaxDoctorsPerDay(1000)
            .build();
    private Job dienst = new Job.Builder()
            .setEnum(JobName.DIENST)
            .setName("Dienst")
            .setVerfugbareTage(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
            .setOneDoctorPerWeek(false)
            .setDifferentDoctorsOnConsecutiveDays(true)
            .setMaxPerMonthPerDoctor(5)
            .setMaxPerMonthPerDoctorOnWeekends(2)
            .setMaxDoctorsPerDay(1)
            .build();
    private Job urlaub = new Job.Builder()
            .setEnum(JobName.URLAUB)
            .setName("Urlaub")
            .setVerfugbareTage(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
            .setOneDoctorPerWeek(false)
            .setDifferentDoctorsOnConsecutiveDays(false)
            .setMaxPerMonthPerDoctor(31)
            .setMaxPerMonthPerDoctorOnWeekends(10)
            .setMaxDoctorsPerDay(10)
            .build();

    private List<Job> allJobs = new ArrayList<>();
    {
        allJobs.add(eaz);
        allJobs.add(zna);
        allJobs.add(op);
        allJobs.add(station);
        allJobs.add(dienst);
    }
    private List<Job> dayJobs = new ArrayList<>();
    {
        dayJobs.add(eaz);
        dayJobs.add(zna);
        dayJobs.add(op);
        dayJobs.add(station);
        dayJobs.add(urlaub);
    }

    private Map<JobName, Job> jobMap = new HashMap<>();
    {
        jobMap.put(JobName.EAZ, eaz);
        jobMap.put(JobName.ZNA, zna);
        jobMap.put(JobName.OP, op);
        jobMap.put(JobName.STATION, station);
        jobMap.put(JobName.DIENST, dienst);
        jobMap.put(JobName.URLAUB, urlaub);
    }

}
