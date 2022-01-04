package Controllers;

import Model.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;

public class AppointmentForm extends Form<Appointment> {
    final boolean use24HourTime = LocalTime.of(23, 00)
            .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
            .matches("^23.+00$");
    private final Map<Long, Contact> contactMap;
    private final HashMap<Long, Customer> customerMap = new HashMap<>();
    private final HashMap<Long, User> userMap = new HashMap<>();
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private ComboBox<String> startHourPicker;
    @FXML
    private ComboBox<String> startMinutePicker;
    @FXML
    private ChoiceBox<String> startMeridiemPicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private ComboBox<String> endHourPicker;
    @FXML
    private ComboBox<String> endMinutePicker;
    @FXML
    private ChoiceBox<String> endMeridiemPicker;
    @FXML
    private ComboBox<Contact> contactComboBox;
    @FXML
    private ComboBox<Customer> customerComboBox;
    @FXML
    private ComboBox<User> userComboBox;
    @FXML
    private TextField titleField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField locationField;
    @FXML
    private TextField typeField;

    public AppointmentForm(String windowTitle,
                           Map<Long, Contact> contactMap,
                           List<Customer> customers,
                           FormFactory.Mode mode,
                           Appointment record,
                           Function<Appointment, Boolean> callback) {
        super(windowTitle, mode, record, callback);
        this.contactMap = contactMap;
        for (Customer customer : customers) {
            customerMap.put(customer.getId(), customer);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        buildUserMap();
        initializeDateFields();
        contactComboBox.getItems().addAll(contactMap.values());
        customerComboBox.getItems().addAll(customerMap.values());
        super.initialize(url, resourceBundle);
    }

    /**
     * @see Form#applyOtherFieldsToRecord()
     */
    @Override
    protected void applyOtherFieldsToRecord() {
        final LocalDateTime start = parseDateTime(startDatePicker, startHourPicker, startMinutePicker, startMeridiemPicker);
        final LocalDateTime end = parseDateTime(endDatePicker, endHourPicker, endMinutePicker, endMeridiemPicker);
        record.setStart(start);
        record.setEnd(end);
        record.setCustomerId(getRecordId(customerComboBox.getValue()));
        record.setUserId(getRecordId(userComboBox.getValue()));
        record.setContactId(getRecordId(contactComboBox.getValue()));
    }

    /**
     * @see Form#setFields()
     */
    @Override
    protected void setFields() {
        setComboBoxFromMap(contactComboBox, contactMap, record.getContactId());
        setComboBoxFromMap(customerComboBox, customerMap, record.getCustomerId());
        setComboBoxFromMap(userComboBox, userMap, record.getUserId());
        setDateFields(record.getLocalStart(), startDatePicker, startHourPicker, startMinutePicker, startMeridiemPicker);
        setDateFields(record.getLocalEnd(), endDatePicker, endHourPicker, endMinutePicker, endMeridiemPicker);
    }

    private <T extends Record> void setComboBoxFromMap(ComboBox<T> comboBox, Map<Long, T> map, long id) {
        if (id != 0) comboBox.getSelectionModel().select(map.get(id));
        comboBox.setDisable(readOnly);
    }

    /**
     * sets the date and time fields from the ZonedDateTime object
     *
     * @param date           the object that holds the date and time
     * @param datePicker     a date picker for the date
     * @param hourPicker     a ComboBox for the hour
     * @param minutePicker   a ComboBox for the minute
     * @param meridiemPicker a ChoiceBox for am/pm
     */
    private void setDateFields(ZonedDateTime date,
                               DatePicker datePicker,
                               ComboBox<String> hourPicker,
                               ComboBox<String> minutePicker,
                               ChoiceBox<String> meridiemPicker) {
        if (use24HourTime) {
            hourPicker.setValue(String.format("%02d", date.getHour()));
        } else {
            final int modHour = date.getHour() % 12;
            hourPicker.setValue(Integer.toString(modHour == 0 ? 12 : modHour));
            meridiemPicker.setValue(date.getHour() >= 12 ? "pm" : "am");
            meridiemPicker.setDisable(readOnly);
        }
        hourPicker.setDisable(readOnly);
        minutePicker.setValue(String.format("%02d", date.getMinute()));
        minutePicker.setDisable(readOnly);
        datePicker.setValue(date.toLocalDate());
        datePicker.setDisable(readOnly);
    }

    /**
     * @see Form#getResourceURL()
     */
    @Override
    protected String getResourceURL() {
        return "/Views/AppointmentForm.fxml";
    }

    /**
     * @see Form#getWidth()
     */
    @Override
    protected double getWidth() {
        return 600;
    }

    /**
     * @see Form#getHeight()
     */
    @Override
    protected double getHeight() {
        return 540;
    }

    /**
     * fills the date fields with all necessary options to choose a start/end date and time
     */
    private void initializeDateFields() {
        ChoiceBox[] meridiemPickers = {endMeridiemPicker, startMeridiemPicker};
        for (ChoiceBox<String> meridiemPicker : meridiemPickers) {
            if (use24HourTime) {
                meridiemPicker.setDisable(true);
                meridiemPicker.setVisible(false);
            } else {
                meridiemPicker.getItems().addAll("am", "pm");
                meridiemPicker.getSelectionModel().select("am");
                meridiemPicker.setDisable(readOnly);
            }
        }

        ComboBox[] hourPickers = {startHourPicker, endHourPicker};
        for (ComboBox hourPicker : hourPickers) {
            final ObservableList<String> options = hourPicker.getItems();
            if (use24HourTime) {
                for (int h = 0; h < 24; h++) {
                    options.add(String.format("%02d", h));
                }
            } else {
                for (int h = 0; h < 12; h++) {
                    options.add(Integer.toString(h == 0 ? 12 : h));
                }
            }
            hourPicker.getSelectionModel().select("12");
        }

        ComboBox[] minutePickers = {startMinutePicker, endMinutePicker};
        for (ComboBox minutePicker : minutePickers) {
            final ObservableList<String> options = minutePicker.getItems();
            for (int m = 0; m < 60; m++) {
                options.add(String.format("%02d", m));
            }
            minutePicker.getSelectionModel().select("00");
        }

        DatePicker[] datePickers = {startDatePicker, endDatePicker};
        for (DatePicker datePicker : datePickers) {
            datePicker.setValue(LocalDate.now());
        }
    }

    /**
     * lambda1: consume an exception and result set and allow for DRY resource cleanup
     * <p>
     * creates a map of User Id to User object for easy lookup
     */
    private void buildUserMap() {
        // lambda to consume an exception and result set and allow for DRY resource cleanup
        executeQuery("SELECT User_ID, User_Name FROM users", (ex, rs) -> {
            if (ex != null) return;
            try {
                while (rs.next()) {
                    final User user = new User(rs.getLong(1), rs.getString(2));
                    userMap.put(user.getId(), user);
                }
            } catch (SQLException exception) {
                printSQLException(exception);
            }
        });
        userComboBox.getItems().addAll(userMap.values());
    }

    /**
     * parses a LocalDateTime out of the fields that make up the date/time info
     *
     * @param datePicker     a date picker for the date
     * @param hourPicker     a ComboBox for the hour
     * @param minutePicker   a ComboBox for the minute
     * @param meridiemPicker a ChoiceBox for am/pm
     * @return the LocalDateTime contained by the various form elements
     */
    private LocalDateTime parseDateTime(DatePicker datePicker,
                                        ComboBox<String> hourPicker,
                                        ComboBox<String> minutePicker,
                                        ChoiceBox<String> meridiemPicker) {
        int hour = hourPicker.getSelectionModel().getSelectedIndex();
        if (!use24HourTime) {
            hour += (meridiemPicker.getSelectionModel().getSelectedItem() == "am" ? 0 : 12);
        }
        final int minute = minutePicker.getSelectionModel().getSelectedIndex();
        return datePicker.getValue().atTime(hour, minute).atZone(ZoneOffset.systemDefault()).toLocalDateTime();
    }
}
