package Model;

import java.util.ArrayList;
import java.util.List;

public final class Customer extends Record implements Model<Customer>, Reportable {
    private final String address;
    private final String postalCode;
    private final String phone;
    private String name;
    private long divisionId;

    public Customer(long id, String name, String address, String postalCode, String phone, long divisionId) {
        super(id);
        this.name = name;
        this.address = address;
        this.postalCode = postalCode;
        this.phone = phone;
        this.divisionId = divisionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public long getDivisionId() {
        return divisionId;
    }

    public void setDivisionId(long divisionId) {
        this.divisionId = divisionId;
    }

    /**
     * @see Model#toValues()
     */
    @Override
    public List<Object> toValues() {
        return new ArrayList<>(List.of(name, address, postalCode, phone, divisionId));
    }

    /**
     * @see Model#copy()
     */
    @Override
    public Customer copy() {
        return new Customer(id, name, address, postalCode, phone, divisionId);
    }

    /**
     * overrides built-in toString() for display in a ComboBox
     *
     * @return the name of the customer
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
        return String.format("\t%d\t%s\n", id, name);
    }
}