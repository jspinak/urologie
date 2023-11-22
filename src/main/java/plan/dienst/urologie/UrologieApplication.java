package plan.dienst.urologie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.time.LocalDate;
import static plan.dienst.urologie.Doctors.DocName.*;

@SpringBootApplication
public class UrologieApplication implements CommandLineRunner {

	private final PlanBuilder planBuilder;

	@Autowired
	public UrologieApplication(PlanBuilder planBuilder) {
		this.planBuilder = planBuilder;
	}

	public static void main(String[] args) {
		SpringApplication.run(UrologieApplication.class, args);
	}

	private void urlaub(Doctors.DocName doctor, int monat, int tag) {
		planBuilder.urlaub(doctor, date(monat, tag));
	}

	private void urlaub(Doctors.DocName doctor, int vonMonat, int vonTag, int bisMonat, int bisTag) {
		LocalDate startDate = date(vonMonat, vonTag);
		LocalDate endDate = date(bisMonat, bisTag);
		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1))
			planBuilder.urlaub(doctor, date);
	}

	private void dienst(Doctors.DocName doctor, int monat, int tag) {
		planBuilder.dienst(doctor, date(monat, tag));
	}

	private LocalDate date(int monat, int tag) {
		return LocalDate.of(2024, monat, tag);
	}

	@Override
	public void run(String... args) throws Exception {
		dienst(SABINE,1, 6);
		dienst(SABINE,1,20);
		dienst(SABINE, 2, 17);
		dienst(SABINE,3,23);
		dienst(SEBASTIAN, 1, 13);
		dienst(SEBASTIAN, 2, 3);
		dienst(SEBASTIAN, 3,2);
		dienst(NIGEL, 1, 1);
		urlaub(DIMI, 1, 2);

		urlaub(NIGEL, 1, 13, 1, 28);
		urlaub(CHRISTOPH, 1, 22, 1, 26);
		urlaub(CHRISTOPH, 2, 9);
		urlaub(PAKTIS, 2,10,2,18);
		urlaub(JETTE, 2,10,2,18);
		urlaub(CHRISTOPH,2,20);
		urlaub(DIMI, 2,24,3,12);
		urlaub(CHRISTOPH, 3,13,3,28);
		urlaub(NIGEL,2,3,2,4);
		urlaub(MICHAL, 1, 13, 1, 21);
		urlaub(TIMO, 3, 2, 3, 17);

		planBuilder.makePlan(2024, 1); // Call the makePlan() method when the application starts
	}

}
