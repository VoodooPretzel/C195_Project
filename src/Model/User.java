package Model;

final public class User extends Record {
    final private String name;

    public User(long id, String name) {
        super(id);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * overrides built-in toString() for display in a ComboBox
     *
     * @return the name of the user
     */
    @Override
    public String toString() {
        return name;
    }
}