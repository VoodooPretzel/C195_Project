package Model;

public class Country extends Record {
    private final String country;

    public Country(int id, String country) {
        super(id);
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    /**
     * overrides built-in toString() for display in a ComboBox
     *
     * @return the name of the country
     */
    @Override
    public String toString() {
        return country;
    }
}