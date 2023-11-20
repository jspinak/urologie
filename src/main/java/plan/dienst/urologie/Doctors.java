package plan.dienst.urologie;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.DayOfWeek.*;
import static plan.dienst.urologie.Doctors.DocName.*;

@Component
@Getter
public class Doctors {

    public enum DocName {
        PAKTIS, MICHAL, DIMI, TIMO, PATRICK, JETTE, CHRISTOPH, NIGEL, MARK, SEBASTIAN, SABINE
    }

    private Doctor paktis = new Doctor.Builder()
            .setEnum(PAKTIS)
            .setName("Paktis")
            .setVollzeit(true)
            .setMaxDiensteImMonat(14)
            .setVerfugbareTage(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
            .setVerfugbareTageDienst(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
            .build();
    private Doctor michal = new Doctor.Builder()
            .setEnum(MICHAL)
            .setName("Michal")
            .setVollzeit(true)
            .setMaxDiensteImMonat(14)
            .setVerfugbareTage(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
            .setVerfugbareTageDienst(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
            .build();
    private Doctor dimi = new Doctor.Builder()
            .setEnum(DIMI)
            .setName("Dimi")
            .setVollzeit(true)
            .setMaxDiensteImMonat(14)
            .setVerfugbareTage(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
            .setVerfugbareTageDienst(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
            .build();
    private Doctor timo = new Doctor.Builder()
            .setEnum(TIMO)
            .setName("Timo")
            .setVollzeit(true)
            .setMaxDiensteImMonat(14)
            .setVerfugbareTage(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
            .setVerfugbareTageDienst(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
            .build();
    private Doctor patrick = new Doctor.Builder()
            .setEnum(PATRICK)
            .setName("Patrick")
            .setVollzeit(true)
            .setMaxDiensteImMonat(14)
            .setVerfugbareTage(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
            .setVerfugbareTageDienst(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
            .build();
    private Doctor jette = new Doctor.Builder()
            .setEnum(JETTE)
            .setName("Jette")
            .setVollzeit(false)
            .setMaxDiensteImMonat(3)
            .setVerfugbareTage(MONDAY, TUESDAY, THURSDAY, SATURDAY, SUNDAY)
            .setVerfugbareTageDienst(MONDAY, SATURDAY, SUNDAY)
            .build();
    private Doctor christoph = new Doctor.Builder()
            .setEnum(CHRISTOPH)
            .setName("Christoph")
            .setVollzeit(false)
            .setMaxDiensteImMonat(3)
            .setVerfugbareTage(MONDAY, TUESDAY, WEDNESDAY, FRIDAY, SATURDAY, SUNDAY)
            .setVerfugbareTageDienst(MONDAY, TUESDAY, FRIDAY, SATURDAY, SUNDAY)
            .build();
    private Doctor nigel = new Doctor.Builder()
            .setEnum(NIGEL)
            .setName("Nigel")
            .setVollzeit(true)
            .setMaxDiensteImMonat(14)
            .setVerfugbareTage(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
            .setVerfugbareTageDienst(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
            .build();
    private Doctor mark = new Doctor.Builder()
            .setEnum(MARK)
            .setName("Mark")
            .setVollzeit(true)
            .setMaxDiensteImMonat(14)
            .setVerfugbareTage(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
            .setVerfugbareTageDienst(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
            .build();
    private Doctor sebastian = new Doctor.Builder()
            .setEnum(SEBASTIAN)
            .setName("Sebastian")
            .setVollzeit(false)
            .setMaxDiensteImMonat(0)
            .setVerfugbareTage()
            .setVerfugbareTageDienst()
            .build();
    private Doctor sabine = new Doctor.Builder()
            .setEnum(SABINE)
            .setName("Sabine")
            .setVollzeit(false)
            .setMaxDiensteImMonat(0)
            .setVerfugbareTage()
            .setVerfugbareTageDienst()
            .build();
    
    private final List<Doctor> allDoctors = new ArrayList<>();
    {
        allDoctors.add(paktis);
        allDoctors.add(michal);
        allDoctors.add(dimi);
        allDoctors.add(timo);
        allDoctors.add(patrick);
        allDoctors.add(jette);
        allDoctors.add(christoph);
        allDoctors.add(nigel);
        allDoctors.add(mark);
        allDoctors.add(sebastian);
        allDoctors.add(sabine);
    }

    private Map<DocName, Doctor> doctorMap = new HashMap<>();
    {
        doctorMap.put(PAKTIS, paktis);
        doctorMap.put(MICHAL, michal);
        doctorMap.put(DIMI, dimi);
        doctorMap.put(TIMO, timo);
        doctorMap.put(PATRICK, patrick);
        doctorMap.put(JETTE, jette);
        doctorMap.put(CHRISTOPH, christoph);
        doctorMap.put(NIGEL, nigel);
        doctorMap.put(MARK, mark);
        doctorMap.put(SEBASTIAN, sebastian);
        doctorMap.put(SABINE, sabine);
    }
}
