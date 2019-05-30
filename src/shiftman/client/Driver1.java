package shiftman.client;

import java.util.List;

import shiftman.server.ShiftMan;
import shiftman.server.ShiftManServer;

/**
 * A sample driver program. There are lots of ways this could be done. This is just one
 * of them. Having the data in arrays makes it easier to use it by 
 */
public class Driver1 {
	private static final String[][] STAFF = {
			{ "Bayta", "Darell" },
			{ "Hari", "Sheldon" },
			{ "Ebling", "Mis" },
			{ "Dors", "Venabili" },
			{ "Gaal", "Dornick" }
	};

	/**
	 * Format: Day, Opening time, Closing time, non-empy means late night shopping,
	 * name of main manager for all times except 12:00-13:00, name of lunch
	 * manager (12:00-13:00) who has lunch 13:00-14:00 and is worker other times
	 */
	private static final String[][] OPENING_HOURS = {
			{ "Monday", "09:00", "17:00", "", STAFF[0][0], STAFF[0][1], STAFF[1][0], STAFF[1][1] },
			{ "Tuesday", "09:00", "17:00", "", STAFF[0][0], STAFF[0][1], STAFF[1][0], STAFF[1][1] },
			{ "Wednesday", "09:00", "17:00", "", STAFF[0][0], STAFF[0][1], STAFF[1][0], STAFF[1][1] },
			{ "Thursday", "09:00", "21:00", "late", STAFF[0][0], STAFF[0][1], STAFF[1][0], STAFF[1][1] },
			{ "Friday", "09:00", "17:00", "", STAFF[0][0], STAFF[0][1], STAFF[1][0], STAFF[1][1] },
			{ "Sunday", "09:00", "17:00", "", STAFF[0][0], STAFF[0][1], STAFF[1][0], STAFF[1][1] },
	};
	private static final String[][] SHIFT_HOURS = {
			{ "09:00", "12:00", "" },
			{ "12:00", "13:00", "LUNCH" },
			{ "13:00", "14:00", "LATE" },
			{ "14:00", "17:00", "" },
			{ "17:00", "21:00", "" }
	};

	public static void main(String[] args) {
		ShiftMan scheduler = new ShiftManServer();
		scheduler.newRoster("eScooters R Us");
		
		// Set up the roster
		for (String[] staff: STAFF) {
			scheduler.registerStaff(staff[0], staff[1]);
		}
		for (String[] dayspec: OPENING_HOURS) {
			scheduler.setWorkingHours(dayspec[0], dayspec[1], dayspec[2]);
			int normal = (dayspec[3].equals(""))?1:0;
			for (int i = 0; i < SHIFT_HOURS.length-normal; i++) {
				scheduler.addShift(dayspec[0], SHIFT_HOURS[i][0], SHIFT_HOURS[i][1], "1");
				if (SHIFT_HOURS[i][2].equals("")) {
					checkStatus("Assign manager", scheduler.assignStaff(dayspec[0], SHIFT_HOURS[i][0], SHIFT_HOURS[i][1], dayspec[4], dayspec[5], true));
					checkStatus("Assign worker", scheduler.assignStaff(dayspec[0], SHIFT_HOURS[i][0], SHIFT_HOURS[i][1], dayspec[6], dayspec[7], false));					
				} else if (SHIFT_HOURS[i][2].equals("LATE")) {
					checkStatus("Assign manager for late lunch", scheduler.assignStaff(dayspec[0], SHIFT_HOURS[i][0], SHIFT_HOURS[i][1], dayspec[4], dayspec[5], true));
					checkStatus("Assign worker for late lunch", scheduler.assignStaff(dayspec[0], SHIFT_HOURS[i][0], SHIFT_HOURS[i][1], STAFF[2][0], STAFF[2][1], false));
				} else if (SHIFT_HOURS[i][2].equals("LUNCH")) {
					checkStatus("Assign manager for lunch", scheduler.assignStaff(dayspec[0], SHIFT_HOURS[i][0], SHIFT_HOURS[i][1], dayspec[6], dayspec[7], true));					
					checkStatus("Assign worker for lunch", scheduler.assignStaff(dayspec[0], SHIFT_HOURS[i][0], SHIFT_HOURS[i][1], STAFF[2][0], STAFF[2][1], false));
				}
			}
		}
		// Demonstrate one-off staff assignment
		checkStatus("Assign late night shopping worker", scheduler.assignStaff(OPENING_HOURS[3][0], SHIFT_HOURS[4][0], SHIFT_HOURS[4][1], STAFF[3][0], STAFF[3][1], false));
		
		// Provide various reports on current roster
		report("Registered Staff:", scheduler.getRegisteredStaff());
		report("Unassigned Staff:", scheduler.getUnassignedStaff());
		System.out.println("No managers: " + scheduler.shiftsWithoutManagers());
		System.out.println("Understaffed: " + scheduler.understaffedShifts());
		System.out.println("Overstaff: " + scheduler.overstaffedShifts());
		for (String[] staff: STAFF) {
			String staffName = staff[0] + " " + staff[1];
			report("Worker roster for " + staffName, scheduler.getRosterForWorker(staffName));
		}
		for (String[] staff: STAFF) {
			String staffName = staff[0] + " " + staff[1];
			report("Manager roster for " + staffName, scheduler.getShiftsManagedBy(staffName));
		}
		for (String[] dayspec: OPENING_HOURS) {
			String day = dayspec[0];
			report("Roster for " + day, scheduler.getRosterForDay(day));
		}
	}
	/**
	 * Helper method for when a String is returned
	 */
	private static void checkStatus(String header, String status) {
		if (!status.equals("")) {
			System.out.print("Report:" + header + " >>");
			System.out.println(status);
		}		
	}
	/**
	 * Helper method for when a list of String is returned.
	 */
	private static void report(String header, List<String> status) {
		System.out.print("Report:" + header + " >>");
		if (status.size() == 1 && status.get(0).startsWith("ERROR")) {
			System.out.println(status.get(0));
		} else {
			System.out.println(status);
		}
	}
}
