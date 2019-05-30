package shiftman.server;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the ShiftMan API. This server manages a single roster at a time,
 * which has the shifts for a fixed seven days (single week), as well as all of the employed
 * staff.
 */
public class ShiftManServer implements ShiftMan {

    private Roster _roster;

    private final String NULL_ROSTER_ERROR = "ERROR: no roster has been created";

    // No custom constructor is necessary for this class as its only field is not set during object creation.



    public String newRoster(String shopName) {
        if (shopName == null) {
            return "ERROR: shop name given is empty";
        }

        _roster = new Roster(shopName);
        return "";
    }

    public String setWorkingHours(String dayOfWeek, String startTime, String endTime) {
        if (_roster == null) {
            return NULL_ROSTER_ERROR;
        }

        return _roster.setWorkingHours(dayOfWeek, startTime, endTime);
    }

    public String addShift(String dayOfWeek, String startTime, String endTime, String minimumWorkers) {
        if (_roster == null) {
            return NULL_ROSTER_ERROR;
        }

        return _roster.addShift(dayOfWeek, startTime, endTime, minimumWorkers);
    }

    public String registerStaff(String givenname, String familyName) {
        if (_roster == null) {
            return NULL_ROSTER_ERROR;
        }

        return _roster.registerStaff(givenname, familyName);
    }

    public String assignStaff(String dayOfWeek, String startTime, String endTime, String givenName,
                              String familyName, boolean isManager) {
        if (_roster == null) {
            return NULL_ROSTER_ERROR;
        }

        return _roster.assignStaff(dayOfWeek, startTime, endTime, givenName, familyName, isManager);
    }

    public List<String> getRegisteredStaff() {
        if (_roster == null) {
            return errorMessageList();
        }

        return _roster.listRegisteredStaff();
    }

    public List<String> getUnassignedStaff() {
        if (_roster == null) {
            return errorMessageList();
        }

        return _roster.listUnassignedStaff();
    }

    public List<String> shiftsWithoutManagers() {
        if (_roster == null) {
            return errorMessageList();
        }

        return _roster.listUnmanagedShifts();
    }

    public List<String> understaffedShifts() {
        if (_roster == null) {
            return errorMessageList();
        }

        return _roster.listOverOrUnderstaffedShifts(-1);
    }

    public List<String> overstaffedShifts() {
        if (_roster == null) {
            return errorMessageList();
        }

        return _roster.listOverOrUnderstaffedShifts(1);
    }

    public List<String> getRosterForDay(String dayOfWeek) {
        if (_roster == null) {
            return errorMessageList();
        }

        return _roster.getRosterForDay(dayOfWeek);
    }

    public List<String> getRosterForWorker(String workerName) {
        if (_roster == null) {
            return errorMessageList();
        }

        String[] splitName = splitWholeName(workerName);
        return _roster.getRosterForWorker(splitName[0], splitName[1]);
    }

    public List<String> getShiftsManagedBy(String managerName) {
        if (_roster == null) {
            return errorMessageList();
        }

        String[] splitName = splitWholeName(managerName);
        return _roster.getRosterForManager(splitName[0], splitName[1]);
    }



    /**
     * Helper method for every method that returns a List<String>.
     * Generates a list with only one item - the error message.
     */
    private List<String> errorMessageList() {
        List<String> errorMessage = new ArrayList<>();
        errorMessage.add(NULL_ROSTER_ERROR);
        return errorMessage;
    }

    /**
     * Helper method for getRosterForWorker() and getShiftsManagedBy().
     * Splits a whole name ("firstName lastName") into two separate strings, in an array.
     */
    private String[] splitWholeName(String name) {
        return name.trim().split("\\s+");
    }





    /**
     * The following method is not marked, and thus its implementation has been removed.
     */
    public String reportRosterIssues() {
        return "";
    }

    /**
     * The following method is not marked, and thus its implementation has been removed.
     */
    public String displayRoster() {
        return "";
    }
}
