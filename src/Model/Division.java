package Model;

public class Division extends Record implements Reportable {
    private final String division;
    private final long countryId;

    public Division(long id, String division, long countryId) {
        super(id);
        this.division = division;
        this.countryId = countryId;
    }

    public String getDivision() {
        return division;
    }

    public long getCountryId() {
        return countryId;
    }

    /**
     * overrides built-in toString() for display in a ComboBox
     *
     * @return the name of the division
     */
    @Override
    public String toString() {
        return division;
    }

    /**
     * @see Reportable#toReportString()
     */
    @Override
    public String toReportString() {
        return String.format("%d\t%s:\n", id, division);
    }
}