package shiftman.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Day. Within each day, we have a name (what day of the week it is), the starting and ending hours
 * of work, and a list of shifts that take place in between these hours.
 */
public class Day {

    private String _dayName;

    private String _startOfDay;

    private String _endOfDay;

    private List<Shift> _shifts;

    public Day (String dayOfWeek) {
        _dayName = dayOfWeek;
        _shifts = new ArrayList<>();
        System.out.println("@Day object created with name " + _dayName);
    }



    public void setWorkingHours(String startTime, String endTime) throws ShiftManUserException {
        if (validateTimeFormat(startTime, endTime)) {
            throw new ShiftManUserException("ERROR: Time does not match format hh:mm");
        } else if (checkValidTimes(startTime, endTime)) {
            throw new ShiftManUserException("ERROR: Start and/or end time invalid");
        } else {
            _startOfDay = startTime;
            _endOfDay = endTime;
        }
    }

    public void addShift(String startTime, String endTime, String minimumWorkers) throws ShiftManUserException {
        if (validateTimeFormat(startTime, endTime)) {
            throw new ShiftManUserException("ERROR: Time does not match format hh:mm");
        } else if (checkValidTimes(startTime, endTime)) {
            throw new ShiftManUserException("ERROR: Start and/or end time invalid");
        } else if (checkWithinWorkingHours(startTime, endTime)) {
            throw new ShiftManUserException("ERROR: Start and/or end time outside of working hours");
        } else if (checkAgainstOtherShifts(startTime, endTime)) {
            throw new ShiftManUserException("ERROR: Start and/or end time clashes with existing shifts");
        } else {
            Shift newShift = new Shift(startTime, endTime, minimumWorkers);
            _shifts.add(newShift);
            // This is so that upon recalling the shifts for the day, they are already sorted by start time
            Collections.sort(_shifts);
        }
    }

    public void assignStaff(Shift shift, StaffMember staff, boolean isManager) throws ShiftManUserException {
        if (isManager) {
            shift.assignManager(staff);
        } else {
            shift.assignStaff(staff);
        }
    }

    public List<StaffMember> listAllStaffWorkingToday() {
        List<StaffMember> allStaff = new ArrayList<>();
        for (Shift s : _shifts) {
            allStaff.addAll(s.getAssignedStaff());
        }
        return allStaff;
    }

    public List<String> listShiftsWithoutManagers() {
        List<String> unmanagedShifts = new ArrayList<>();
        for (Shift s: _shifts) {
            if (!s.isManagerAssigned()) {
                unmanagedShifts.add(assembleShiftString(s));
            }
        }

        return unmanagedShifts;
    }

    public List<String> listOverOrUnderstaffedShifts(int status) {
        // Recall that status: -1 = understaffed, 1 = overstaffed
        List<String> xStaffedShifts = new ArrayList<>();
        for (Shift s : _shifts) {
            // Check if the shift is over/understaffed (whichever one we are looking for) before adding it to the list
            if (s.overOrUnderstaffed() == status) {
                xStaffedShifts.add(assembleShiftString(s));
            }
        }

        return xStaffedShifts;
    }

    public List<String> getDayRoster() {
        List<String> dayRoster = new ArrayList<>();
        for (Shift s: _shifts) {
            dayRoster.add(_dayName + s.shiftInfoForRoster());
        }

        return dayRoster;
    }

    public List<String> listShiftsWithStaffMember(StaffMember staff) {
        List<String> workerRoster = new ArrayList<>();
        for (Shift s : _shifts) {
            if (s.isStaffMemberAssigned(staff)) {
                workerRoster.add(assembleShiftString(s));
            }
        }

        return workerRoster;
    }

    public List<String> listShiftsWithManager(StaffMember manager) {
        List<String> managerRoster = new ArrayList<>();
        for (Shift s : _shifts) {
            if (s.isSpecificManagerAssigned(manager)) {
                managerRoster.add(assembleShiftString(s));
            }
        }

        return managerRoster;
    }



    /**
     * Helper method for Roster.assignStaff().
     * Attempts to find a shift during this day, based on its start and end times.
     */
    public Shift findShift(String startTime, String endTime) throws ShiftManUserException {
        if (validateTimeFormat(startTime, endTime)) {
            throw new ShiftManUserException("ERROR: Time does not match format hh:mm");
        }

        for (Shift s : _shifts) {
            if (s.compareShiftTimes(startTime, endTime)) {
                return s;
            }
        }

        throw new ShiftManUserException("ERROR: Shift does not exist in day");
    }

    /**
     * Helper method for setWorkingHours(), addShift(), and findShift().
     * Checks that the input start and end times match a given regex pattern - dd:dd
     * This allows us to alphabetically compare times in other helper methods.
     */
    private boolean validateTimeFormat(String startTime, String endTime) {
        String pattern = "\\d\\d:\\d\\d";
        // Returns true if the format is NOT matched
        return !(startTime.matches(pattern) && endTime.matches(pattern));
    }

    /**
     * Helper method for setWorkingHours() and addShift().
     * Compares the start and end times to the limits of the day - as specified, they cannot include midnight.
     * Also checks that the end time is after the start time.
     */
    private boolean checkValidTimes(String startTime, String endTime) {
        final String MIN_START_TIME = "00:01";
        final String MAX_END_TIME = "23:59";

        int startAfter12 = startTime.compareTo(MIN_START_TIME); // >= 0
        int endAfter12 = endTime.compareTo(MIN_START_TIME);     // >= 0
        int endBefore12 = endTime.compareTo(MAX_END_TIME);      // <= 0
        int startBeforeEnd = startTime.compareTo(endTime);      // <  0

        // Returns true if the times are NOT valid
        return !(startAfter12 >= 0 && endAfter12 >= 0 && endBefore12 <= 0 && startBeforeEnd < 0);
    }

    /**
     * Helper method for addShift().
     * Compares the start and end times of the shift to the previously set working hours,
     * which they must fall within.
     */
    private boolean checkWithinWorkingHours(String startTime, String endTime) {
        int withinWorkingStart = startTime.compareTo(_startOfDay);
        int withinWorkingEnd = endTime.compareTo(_endOfDay);

        // Returns true if the times are NOT within working hours
        return !(withinWorkingStart >= 0 && withinWorkingEnd <= 0);
    }

    /**
     * Helper method for addShift().
     * Checks the start and end times against the times of every shift currently registered
     * this day to ensure there is no overlap.
     * The logic is a bit confusing, but can be summed down to:
     * |----new----|            or           |----new----|
     *        |----old----|           |----old----|
     */
    private boolean checkAgainstOtherShifts(String startTime, String endTime) {
        for (Shift s : _shifts) {
            String otherStart = s.toString().substring(1,6);            // it's returning [09:00-12:00]
            String otherEnd = s.toString().substring(7,12);             // and we want    >     =     <

            int startsTooSoon = startTime.compareTo(otherEnd);          // is newStart <= otherEnd?
            int validStartComp = startTime.compareTo(otherStart);       // is newStart >= otherStart?

            int endsTooLate = endTime.compareTo(otherStart);            // is newEnd >= otherStart?
            int validEndComp = endTime.compareTo(otherEnd);             // is newEnd <= otherEnd?

            // Returns true if the times CLASH with other shifts
            if ((startsTooSoon <= 0 && validStartComp >= 0) ||(endsTooLate >= 0 && validEndComp <= 0)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Helper method for listShiftsWithoutManagers(), listOverOrUnderstaffedShifts(),
     * listShiftsWithStaffMember(), and listShiftWithManager().
     * Concatenates the day name with the string representation of the given shift,
     * giving the format day[hh:mm-hh:mm], as specified.
     */
    private String assembleShiftString(Shift shift) {
        return _dayName + shift.toString();
    }

    /**
     * The getRosterForDay() method requires the start and end times of the day represented as such.
     */
    public String getWorkingHours() {
        return _startOfDay + "-" + _endOfDay;
    }

    /**
     * The most useful string representation of this object is the name of the day.
     */
    @Override
    public String toString() {
        return _dayName;
    }
}
