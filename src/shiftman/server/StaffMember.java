package shiftman.server;

/**
 * Represents a staff member. A staff member has a name.
 */
public class StaffMember implements Comparable<StaffMember> {

    private String _givenName;

    private String _familyName;

    public StaffMember(String firstName, String lastName) {
        _givenName = firstName;
        _familyName = lastName;
        System.out.println("@StaffMember object created with name " + _givenName + " " + _familyName);
    }

    /**
     * Used by Comparable for sorting lists of StaffMembers, which are to be ordered by family name first,
     * then given name if necessary.
     */
    public int compareTo(StaffMember other) {
        String thisName = this._familyName + this._givenName;
        String thatName = other._familyName + other._givenName;
        return thisName.compareTo(thatName);
    }

    /**
     * Unlike our usual string representation, the getRosterForX methods require the family name first.
     */
    public String getReversedName() {
        return _familyName + ", " + _givenName;
    }

    /**
     * The most useful string representation of this object is the conventional ordering of names,
     * which are used when listing staff and finding them.
     */
    @Override
    public String toString() {
        return _givenName + " " + _familyName;
    }
}
