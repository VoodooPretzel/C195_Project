package Model;

public class Contact extends Record implements Reportable {
    final String name;
    final String email;

    public Contact(long id, String name, String email) {
        super(id);
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    /**
     * overrides built-in toString() for display in a ComboBox
     *
     * @return the name of the contact
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * @see Reportable#toReportString()
     */
    @Override
    public String toReportString() {
        return String.format("%d\t%s\t%s\n", id, name, email);
    }
}
