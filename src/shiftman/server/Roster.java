package shiftman.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Roster. A roster is associated with a shop and thus stores its name.
 * It also has a list of staff members, and a list of days (effectively a single week).
 */
public class Roster {

    private String _shopName;

    private List<StaffMember> _staffList;

    private List<Day> _weekdays;

    /**
     * An enum containing the days of the week.
     * This is looped through to create the Day objects on construction of a Roster object.
     */
    public enum DaysOfWeek {
        DAY_1("Monday"), DAY_2("Tuesday"), DAY_3("Wednesday"), DAY_4("Thursday"),
        DAY_5("Friday"), DAY_6("Saturday"), DAY_7("Sunday");

        private String _stringRep;

        DaysOfWeek(final String _stringRep) {
            this._stringRep = _stringRep;
        }

        @Override
        public String toString() {
            return _stringRep;
        }
    }

    public Roster (String name) {
        _shopName = name;
        _staffList = new ArrayList<>();
        _weekdays = new ArrayList<>();
        for (DaysOfWeek d : DaysOfWeek.values()) {
            String dayName = d.toString();
            Day day = new Day(dayName);
            _weekdays.add(day);
        }
        System.out.println("@Roster object created with name " + _shopName);
    }



    public String setWorkingHours(String dayOfWeek, String startTime, String endTime) {
        try {
            Day day = findDayInWeek(dayOfWeek);
            day.setWorkingHours(startTime, endTime);
            return "";
        } catch (ShiftManUserException e) {
            return e.getMessage();
        }

    }

    public String addShift(String dayOfWeek, String startTime, String endTime, String minimumWorkers) {
        try {
            Day day = findDayInWeek(dayOfWeek);
            day.addShift(startTime, endTime, minimumWorkers);
            return "";
        } catch (ShiftManUserException e) {
            return e.getMessage();
        }

    }

    public String registerStaff(String givenName, String familyName) {
        if (givenName == null || familyName == null) {
            return "ERROR: Employee name given is empty";
        }

        StaffMember newStaff = new StaffMember(givenName, familyName);

        if (_staffList.contains(newStaff)) {
            return "ERROR: Employee has already been registered";
        } else {
            _staffList.add(newStaff);
            // This is so that upon recalling the registered staff, they are already sorted by last name
            Collections.sort(_staffList);
            return "";
        }
    }

    public String assignStaff(String dayOfWeek, String startTime, String endTime, String givenName,
                              String familyName, boolean isManager) {
        try {
            Day day = findDayInWeek(dayOfWeek);
            StaffMember staff = findRegisteredStaffMember(givenName, familyName);
            Shift shift = day.findShift(startTime, endTime);
            day.assignStaff(shift, staff, isManager);
            return "";
        } catch (ShiftManUserException e) {
            return e.getMessage();
        }
    }

    public List<String> listRegisteredStaff() {
        List<String> registered = new ArrayList<>();
        for (StaffMember s : _staffList) {
            registered.add(s.toString());
        }
        return registered;
    }

    public List<String> listUnassignedStaff() {

        // Get a list of all the staff members that are working
        List<StaffMember> assigned = new ArrayList<>();
        for (Day d: _weekdays) {
            assigned.addAll(d.listAllStaffWorkingToday());
        }

        // Make a copy of our registered staff list
        // and remove everyone who is working from it
        // Then sort the list to be in order of family name
        List<StaffMember> unassigned = new ArrayList<>(_staffList);
        unassigned.removeAll(assigned);
        Collections.sort(unassigned);

        // Convert the list of StaffMember objects into a list of their string representations
        List<String> unassignedStrings = new ArrayList<>();
        for (StaffMember s : unassigned) {
            unassignedStrings.add(s.toString());
        }

        return unassignedStrings;
    }

    public List<String> listUnmanagedShifts() {
        List<String> unmanaged = new ArrayList<>();

        for (Day d : _weekdays) {
            unmanaged.addAll(d.listShiftsWithoutManagers());
        }

        return unmanaged;
    }

    public List<String> listOverOrUnderstaffedShifts(int status) {
        List<String> xStaffed = new ArrayList<>();

        // List all the shifts that are over/understaffed, depending on the given status
        // -1 = understaffed, 1 = overstaffed
        for (Day d : _weekdays) {
            xStaffed.addAll(d.listOverOrUnderstaffedShifts(status));
        }

        return xStaffed;
    }

    public List<String> getRosterForDay(String dayOfWeek) {
        List<String> dayRoster = new ArrayList<>();
        Day day;

        try {
            day = findDayInWeek(dayOfWeek);
        } catch (ShiftManUserException e) {
            dayRoster.add(e.getMessage());
            return dayRoster;
        }

        dayRoster.addAll(day.getDayRoster());

        // If there are no shifts registered in the day, return an empty list as specified
        // Otherwise, add the day and shop details at the top of the list
        if (dayRoster.size() == 0) {
            dayRoster.clear();
        } else {
            dayRoster.add(0, _shopName);
            dayRoster.add(1, day.toString() + " " + day.getWorkingHours());
        }

        return dayRoster;
    }

    public List<String> getRosterForWorker(String givenName, String familyName) {
        List<String> workerRoster = new ArrayList<>();
        StaffMember staff;

        try {
            staff = findRegisteredStaffMember(givenName, familyName);
        } catch (ShiftManUserException e) {
            workerRoster.add(e.getMessage());
            return workerRoster;
        }

        for (Day d : _weekdays) {
            workerRoster.addAll(d.listShiftsWithStaffMember(staff));
        }

        // If there are no shifts worked by the staff member, return an empty list as specified
        // Otherwise, add the staff member's name at the top of the list
        if (workerRoster.size() == 0) {
            workerRoster.clear();
        } else {
            workerRoster.add(0, staff.getReversedName());
        }

        return workerRoster;
    }

    public List<String> getRosterForManager(String givenName, String familyName) {
        List<String> managerRoster = new ArrayList<>();

        StaffMember manager;

        try {
            manager = findRegisteredStaffMember(givenName, familyName);
        } catch (ShiftManUserException e) {
            managerRoster.add(e.getMessage());
            return managerRoster;
        }

        for (Day d : _weekdays) {
            managerRoster.addAll(d.listShiftsWithManager(manager));
        }

        // If there are no shifts managed by the staff member, return an empty list as specified
        // Otherwise, add the staff member's name at the top of the list
        if (managerRoster.size() == 0) {
            managerRoster.clear();
        } else {
            managerRoster.add(0, manager.getReversedName());
        }

        return managerRoster;
    }



    /**
     * Helper method for setWorkingHours(), addShift(), assignStaff(), and getRosterForDay().
     * Finds a specified Day object by comparing their string representations.
     */
    private Day findDayInWeek(String dayName) throws ShiftManUserException {
        for (Day d : _weekdays) {
            String dayString = d.toString();
            if (dayString.equals(dayName)) {
                return d;
            }
        }
        throw new ShiftManUserException("ERROR: Day does not exist in week");
    }

    /**
     * Helper method for assignStaff(), getRosterForWorker(), and getRosterForManager().
     * Finds a specified StaffMember object by comparing their string representations.
     */
    private StaffMember findRegisteredStaffMember(String givenName, String familyName) throws ShiftManUserException {
        String staffName = givenName + " " + familyName;

        for (StaffMember s : _staffList) {
            String staffString = s.toString();
            if (staffString.equals(staffName)) {
                return s;
            }
        }
        throw new ShiftManUserException("ERROR: Staff member is not registered");
    }

    /**
     * The most useful string representation of this object is the name of the shop the roster is for.
     */
    @Override
    public String toString() {
        return _shopName;
    }

}
