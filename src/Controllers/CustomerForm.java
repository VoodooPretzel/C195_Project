package Controllers;

import Model.Country;
import Model.Customer;
import Model.Division;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.Comparator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;

public final class CustomerForm extends Form<Customer> implements Initializable {
    private final Map<Long, Country> countryMap;
    private final Map<Long, Division> divisionMap;
    @FXML
    private TextField nameField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField postalCodeField;
    @FXML
    private TextField phoneField;
    @FXML
    private ComboBox<Division> divisionComboBox;
    @FXML
    private ComboBox<Country> countryComboBox;

    public CustomerForm(String windowTitle,
                        Map<Long, Division> divisionMap,
                        Map<Long, Country> countryMap,
                        FormFactory.Mode mode,
                        Customer record,
                        Function<Customer, Boolean> callback) {
        super(windowTitle, mode, record, callback);
        this.divisionMap = divisionMap;
        this.countryMap = countryMap;
    }

    /**
     * lambda1: iterate over all the key value pairs. cleaner and more readable than an anonymous class
     *
     * @see Initializable#initialize(URL, ResourceBundle)
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<Country> countries = countryComboBox.getItems();
        // lambda to iterate over all the key value pairs. cleaner and more readable than an anonymous class
        divisionMap.forEach((id, division) -> {
            Country country = countryMap.get(division.getCountryId());
            if (!countries.contains(country)) {
                countries.add(country);
            }
        });
        countries.sort(Comparator.comparing(Country::getCountry));
        divisionComboBox.setDisable(true);
        super.initialize(url, resourceBundle);
    }

    /**
     * @see Form#setFields()
     */
    @Override
    protected void setFields() {
        final Division division = divisionMap.get(record.getDivisionId());
        countryComboBox.getSelectionModel().select(countryMap.get(division.getCountryId()));
        countryComboBox.setDisable(readOnly);
        populateDivisions(null);
        divisionComboBox.getSelectionModel().select(division);
    }

    /**
     * @see Form#getResourceURL()
     */
    @Override
    protected String getResourceURL() {
        return "/Views/CustomerForm.fxml";
    }

    /**
     * lambda1: iterate over all the key value pairs. cleaner and more readable than an anonymous class
     * <p>
     * sets the division ComboBox with all the divisions for the selected country
     *
     * @param event JavaFX action event
     */
    @FXML
    private void populateDivisions(ActionEvent event) {
        final Country country = countryComboBox.getValue();
        final ObservableList<Division> divisions = divisionComboBox.getItems();
        divisions.clear();
        // lambda to iterate over all the key value pairs. cleaner and more readable than an anonymous class
        divisionMap.forEach((key, division) -> {
            if (division.getCountryId() == country.getId()) divisions.add(division);
        });
        divisionComboBox.setDisable(readOnly || divisionComboBox.getItems().isEmpty());
        divisions.sort(Comparator.comparing(Division::getDivision));
    }

    /**
     * @see Form#applyOtherFieldsToRecord()
     */
    @Override
    protected void applyOtherFieldsToRecord() {
        record.setDivisionId(getRecordId(divisionComboBox.getValue()));
    }

    /**
     * @see Form#getHeight()
     */
    @Override
    protected double getHeight() {
        return 400;
    }

    /**
     * @see Form#getWidth()
     */
    @Override
    protected double getWidth() {
        return 400;
    }
}
