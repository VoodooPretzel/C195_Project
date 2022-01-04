package Controllers;

import Model.Appointment;
import Model.Contact;
import Model.Customer;
import Model.Record;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public final class AppointmentTable extends Table<Appointment> implements Initializable {
    private final Filter filterController = new Filter();
    private final HashMap<Long, Contact> contactMap = new HashMap<>();
    private final ObservableList<Customer> customers;
    private final String selectQuery = "SELECT Appointment_ID, Title, Description, `Location`, `Type`, `Start`, " +
            "`End`, Customer_ID, User_ID, Contact_ID " +
            "FROM appointments";
    private Filter.FilterFields currentFilter = null;

    public AppointmentTable(ObservableList<Customer> customers, Main.EventEmitter eventEmitter) {
        super(new AppointmentFormFactory(Appointment.class), eventEmitter);
        ((AppointmentFormFactory) formFactory).setContactMap(Collections.unmodifiableMap(contactMap));
        ((AppointmentFormFactory) formFactory).setCustomers(Collections.unmodifiableList(customers));
        this.customers = customers;
        eventEmitter.addListener(Main.Event.CustomerDeleted, this::populateTable);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize(url, resourceBundle);
        filterButton.setDisable(false);
        filterButton.setVisible(true);
    }

    /**
     * lambda1: correctly translate a contact id into a contact name
     * lambda2: correctly translate a start time into the local time zone
     * lambda3: correctly translate an end time into the local time zone
     * lambda4: correctly display a customer id, if valid
     *
     * @see Table#addColumns()
     */
    @Override
    protected void addColumns() {
        final TableColumn<Appointment, String> contactCol = new TableColumn<>(bundle.getString("appointment.contact"));
        contactCol
                // lambda to correctly translate a contact id into a contact name
                .setCellValueFactory(param -> {
                    final Optional<Contact> contact = Optional.ofNullable(contactMap.get(param.getValue().getContactId()));
                    return new SimpleStringProperty(contact.map(Contact::getName).orElse(""));
                });
        final TableColumn<Appointment, String> startCol = new TableColumn<>(bundle.getString("appointment.start"));
        // lambda to correctly translate a start time into the local time zone
        startCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFormattedStart()));
        final TableColumn<Appointment, String> endCol = new TableColumn<>(bundle.getString("appointment.end"));
        // lambda to correctly translate an end time into the local time zone
        endCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFormattedEnd()));
        final TableColumn<Appointment, String> customerIdCol = new TableColumn<>(bundle.getString("appointment.customerId"));
        // lambda to correctly display a customer id, if valid
        customerIdCol.setCellValueFactory(param -> new SimpleStringProperty(nonZero(param.getValue().getCustomerId())));
        tableView.getColumns().addAll(getStringColumn(Appointment.class, "title"),
                getStringColumn(Appointment.class, "description"),
                getStringColumn(Appointment.class, "location"),
                contactCol,
                getStringColumn(Appointment.class, "type"),
                startCol,
                endCol,
                customerIdCol);
    }

    /**
     * @see Table#populateData()
     */
    @Override
    protected final void populateData() {
        populateTable();
        executeQuery("SELECT * FROM contacts", this::buildContactMap);
    }

    /**
     * populates the table with all of the appointment information. called by populateData() and the event emitter
     * listener whenever a customer is deleted. it applies the current filter if it exists
     */
    private void populateTable() {
        List<Object> arguments = null;
        String query = selectQuery;
        tableView.getItems().clear();
        if (currentFilter != null) {
            query += String.format(" WHERE YEAR(`Start`) = ? AND %s(`Start`) = ?", currentFilter.field);
            arguments = toArray(currentFilter.year, currentFilter.fieldValue);
        }
        executeQuery(query, arguments, this::parseAppointments);
    }

    /**
     * parses the results of an appointment query into instances of the appointment model for display in the table
     *
     * @param ex a sql exception from the query
     * @param rs the result set containing the appointment rows
     */
    private void parseAppointments(SQLException ex, ResultSet rs) {
        if (ex != null) return;
        final ObservableList<Appointment> appointments = tableView.getItems();
        appointments.clear();
        try {
            while (rs.next()) {
                appointments.add(new Appointment(rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getTimestamp(6).toLocalDateTime(),
                        rs.getTimestamp(7).toLocalDateTime(),
                        rs.getLong(8),
                        rs.getLong(9),
                        rs.getLong(10)));
            }
        } catch (SQLException exception) {
            printSQLException(exception);
        }
    }

    /**
     * builds a map of contactId to contact model instances. used to look up the contact name from an appointment record
     * so the contact name is displayed in the table
     *
     * @param ex a sql exception from the query
     * @param rs the result set containing the contact rows
     */
    private void buildContactMap(SQLException ex, ResultSet rs) {
        if (ex != null) return;
        try {
            while (rs.next()) {
                final Contact contact = new Contact(rs.getLong(1), rs.getString(2), rs.getString((3)));
                contactMap.put(contact.getId(), contact);
            }
        } catch (SQLException exception) {
            printSQLException(exception);
        }
    }

    /**
     * @see Table#getInsertStatement()
     */
    @Override
    protected String getInsertStatement() {
        return "INSERT INTO appointments (Title, Description, `Location`, `Type`, `Start`, `End`, Customer_ID, User_ID, Contact_ID, Created_By, Last_Updated_By) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    /**
     * @see Table#getNewRecord()
     */
    @Override
    protected Appointment getNewRecord() {
        return new Appointment(0, null, null, null, null, null, null, 0, 0, 0);
    }

    /**
     * @see Table#getUpdateStatement()
     */
    @Override
    protected String getUpdateStatement() {
        return "UPDATE appointments " +
                "SET Title = ?, Description = ?, `Location` = ?, `Type` = ?, `Start` = ?, `End` = ?, Customer_ID = ?, User_ID = ?, Contact_ID = ?, Last_Updated_By = ?, Last_Update = NOW() " +
                "WHERE Appointment_ID = ?";
    }

    /**
     * @see Table#deleteDependencies(Record)
     */
    @Override
    protected boolean deleteDependencies(Appointment record) {
        return true;
    }

    /**
     * @see Table#getDeleteStatement()
     */
    @Override
    protected String getDeleteStatement() {
        return "DELETE FROM appointments WHERE Appointment_ID = ?";
    }

    /**
     * @see Table#getDeletedMessage(Record)
     */
    @Override
    protected String getDeletedMessage(Appointment appointment) {
        final String replacement = String.format("%s (%s: %d, %s: %s)",
                bundle.getString("appointment.appointment"),
                bundle.getString("record.id"),
                appointment.getId(),
                bundle.getString("appointment.type"),
                appointment.getType());
        return bundle.getString("record.deleted.message").replace("%{record}", replacement);

    }

    /**
     * if the passed in value is 0, it return an empty string for display in the table, otherwise it stringifies the
     * long
     *
     * @param val the long to stringify
     * @return the string value for the table
     */
    protected String nonZero(long val) {
        return val == 0 ? "" : Long.toString(val);
    }

    /**
     * lambda1: registers a callback with the filter controller so we know when the filter can be applied
     *
     * @see Table#addFilter()
     */
    @Override
    protected void addFilter() {
        // lambda registers a callback with the filter controller so we know when the filter can be applied
        filterController.openFilterWindow((fields) -> {
            currentFilter = fields;
            populateData();
        });
    }

    /**
     * lambda1: consume an exception and result set and allow for DRY resource cleanup
     *
     * @see Table#canUpdate(Record)
     */
    @Override
    protected boolean canUpdate(Appointment record) {
        String query = "SELECT COUNT(*) FROM appointments " +
                "WHERE (UNIX_TIMESTAMP(`START`) BETWEEN UNIX_TIMESTAMP(?) AND UNIX_TIMESTAMP(?)" +
                "OR UNIX_TIMESTAMP(`END`) BETWEEN UNIX_TIMESTAMP(?) AND UNIX_TIMESTAMP(?)) " +
                "AND Customer_ID = ?";
        final List<Object> arguments = toArray(record.getSQLStart(),
                record.getSQLEnd(),
                record.getSQLStart(),
                record.getSQLEnd(),
                record.getCustomerId());
        if (record.getId() != 0L) {
            query += " AND Appointment_Id != ?";
            arguments.add(record.getId());
        }
        // lambda to consume an exception and result set and allow for DRY resource cleanup
        final boolean noOverlaps = executeQuery(query, arguments, (ex, rs) -> {
            if (ex != null) return false;
            try {
                rs.next();
                return rs.getInt("COUNT(*)") == 0;
            } catch (SQLException exception) {
                printSQLException(exception);
                return false;
            }
        });

        if (!noOverlaps) {
            displayError(bundle.getString("error.overlapping"));
        }

        return noOverlaps;
    }
}
