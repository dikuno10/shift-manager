package shiftman.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Shift. A shift is a single period of work, with a start and end time, a single manager
 * overseeing this period, a minimum number of workers required (not including the manager),
 * and a list of staff members who have been assigned to work during it.
 */
public class Shift implements Comparable<Shift> {

    private List<StaffMember> _assignedStaff;

    private StaffMember _manager;

    private String _startTime;

    private String _endTime;

    private int _minWorkers;

    public Shift(String startTime, String endTime, String minimumWorkers) {
        _startTime = startTime;
        _endTime = endTime;
        _minWorkers = Integer.parseInt(minimumWorkers);
        _assignedStaff = new ArrayList<>();

        System.out.println("@Shift object created at time " + _startTime + " to " + _endTime);
    }



    public void assignManager(StaffMember staff) throws ShiftManUserException {
        if (_manager == null) {
            _manager = staff;
        } else {
            throw new ShiftManUserException("ERROR: Manager has already been assigned to this shift");
        }
    }

    public void assignStaff(StaffMember staff) throws ShiftManUserException {
        if (_assignedStaff.contains(staff)) {
            throw new ShiftManUserException("ERROR: Staff member has already been assigned to this shift");
        } else {
            _assignedStaff.add(staff);
            // This is so that upon recalling the registered staff, they are already sorted by last name
            Collections.sort(_assignedStaff);
        }
    }

    /**
     * Gets a list of all staff assigned to this shift, including the manager.
     */
    public List<StaffMember> getAssignedStaff() {
        // Create a duplicate of the existing list, to avoid destructive modification and maintain encapsulation.
        List<StaffMember> allStaff = new ArrayList<>(_assignedStaff);
        allStaff.add(_manager);
        return allStaff;
    }

    public int overOrUnderstaffed() {
        int count = _assignedStaff.size();

        // This will return -1 if count < _minWorkers, 0 if ==, and 1 if >.
        return Integer.compare(count, _minWorkers);
    }

    /**
     * Creates the single string representing the shift in the roster,
     * first by concatenating the worker names, then the manager name, then the shift times.
     */
    public String shiftInfoForRoster() {
        String workerNames = "";
        if (_assignedStaff.size() == 0) {
            workerNames = "No workers assigned";
        } else {
            for (StaffMember s : _assignedStaff) {
                workerNames = workerNames + s.toString() + ", ";
            }
            // Removes the final trailing ", " from the end of the string
            workerNames = workerNames.substring(0, workerNames.length() - 2);
        }

        String managerName;
        if (_manager == null) {
            managerName = "[No manager assigned]";
        } else {
            managerName = "Manager:" + _manager.getReversedName();
        }

        return this.toString() + " " + managerName + " [" + workerNames + "]";
    }

    public boolean isManagerAssigned() {
        return _manager != null;
    }

    /**
     * Unlike the above method, which simply tests for presence, this one specifically compares
     * a given manager to the manager of this shift.
     */
    public boolean isSpecificManagerAssigned(StaffMember manager) {
        return _manager.compareTo(manager) == 0;
    }

    public boolean isStaffMemberAssigned(StaffMember staff) {
        return _assignedStaff.contains(staff);
    }

    /**
     * Compares given start and end times with the start and end times of this shift.
     * This differs from the Comparable, which is only used for sorting.
     */
    public boolean compareShiftTimes(String startTime, String endTime) {
        return _startTime.equals(startTime) && _endTime.equals(endTime);
    }

    /**
     * Used by Comparable for sorting lists of Shifts, which are to be ordered by starting time, earliest to latest.
     */
    public int compareTo (Shift other) {
        return _startTime.compareTo(other._startTime);
    }

    /**
     * The most useful string representation of this object is simply its start and end times,
     * which are accessed far more often than the details about the manager and workers.
     */
    @Override
    public String toString() {
        return "[" + _startTime + "-" + _endTime + "]";
    }
}
