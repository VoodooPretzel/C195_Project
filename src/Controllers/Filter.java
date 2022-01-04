package Controllers;

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * controller to filter the appointment table by month or week and year
 */
public class Filter extends Base implements Initializable {
    @FXML
    private final ToggleGroup toggleGroup = new ToggleGroup();
    @FXML
    private ComboBox<Integer> yearComboBox;
    @FXML
    private ComboBox<ComboBoxValue> comboBox;
    @FXML
    private RadioButton monthButton;
    @FXML
    private RadioButton weekButton;
    @FXML
    private Label comboBoxLabel;
    private Stage stage;
    private Consumer<FilterFields> callback;
    private String fieldName;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setYears();
        toggleGroup.selectedToggleProperty().addListener(this::updateItems);
        yearComboBox.getSelectionModel().selectedItemProperty().addListener(this::handleYearChange);
        toggleGroup.getToggles().addAll(monthButton, weekButton);
        toggleGroup.selectToggle(monthButton);
    }

    private void handleYearChange(Observable observable) {
        updateItems(null, null, toggleGroup.getSelectedToggle());
    }

    /**
     * used to populate the month/week ComboBox with the appropriate values for the selected year
     *
     * @param observable not used
     * @param oldValue   not used
     * @param newValue   the radio button that has been selected
     */
    private void updateItems(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
        String bundleProp;
        if (newValue == monthButton) {
            bundleProp = "month";
            setMonths();
        } else {
            bundleProp = "week";
            setWeeks();
        }
        fieldName = bundleProp.toUpperCase();
        comboBoxLabel.setText(bundle.getString(String.format("form.%s", bundleProp)));
        comboBox.getSelectionModel().selectFirst();
    }

    /**
     * lambda1: consume an exception and result set and allow for DRY resource cleanup
     * <p>
     * when the filter window is opened, we query for the distinct list of years that the appointments in the database
     * have and set them in the year ComboBox
     */
    private void setYears() {
        // lambda to consume an exception and result set and allow for DRY resource cleanup
        executeQuery("SElECT DISTINCT YEAR(`Start`) FROM appointments ORDER BY YEAR(`Start`)", (ex, rs) -> {
            if (ex != null) return;
            final ObservableList<Integer> years = yearComboBox.getItems();
            try {
                while (rs.next()) {
                    years.add(rs.getInt(1));
                }
            } catch (SQLException exception) {
                printSQLException(exception);
            }
        });
        yearComboBox.getSelectionModel().selectFirst();
    }

    /**
     * lambda1: consume an exception and result set and allow for DRY resource cleanup
     * <p>
     * called when a year is selected in the ComboBox. we get a list of all weeks of that year that have an appointment.
     * these weeks are then set in the ComboBox
     */
    private void setWeeks() {
        final ObservableList<ComboBoxValue> items = comboBox.getItems();
        items.clear();
        final List<Object> arguments = List.of(yearComboBox.getValue());
        // lambda to consume an exception and result set and allow for DRY resource cleanup
        executeQuery("SELECT DISTINCT WEEK(`Start`) " +
                "FROM appointments " +
                "WHERE YEAR(`Start`) = ? " +
                "ORDER BY WEEK(`Start`)", arguments, (ex, rs) -> {
            if (ex != null) return;
            try {
                while (rs.next()) {
                    final int week = rs.getInt(1);
                    items.add(new ComboBoxValue(Integer.toString(week + 1), week));
                }
            } catch (SQLException exception) {
                printSQLException(exception);
            }
        });
    }

    /**
     * lambda1: consume an exception and result set and allow for DRY resource cleanup
     * <p>
     * called when a year is selected in the ComboBox. we get a list of all months of that year that have an
     * appointment. these months are then set in the ComboBox
     */
    private void setMonths() {
        final ObservableList<ComboBoxValue> items = comboBox.getItems();
        items.clear();
        final List<Object> arguments = List.of(yearComboBox.getValue());
        // lambda to consume an exception and result set and allow for DRY resource cleanup
        executeQuery("SELECT DISTINCT MONTH(`Start`) " +
                "FROM appointments " +
                "WHERE YEAR(`Start`) = ? " +
                "ORDER BY MONTH(`Start`)", arguments, (ex, rs) -> {
            if (ex != null) return;
            try {
                while (rs.next()) {
                    final int month = rs.getInt(1);
                    items.add(new ComboBoxValue(bundle.getString(String.format("month.%d", month)), month));
                }
            } catch (SQLException exception) {
                printSQLException(exception);
            }
        });
    }

    /**
     * calls the passed in callback that actually filters the appointment table, using the FilterFields instance as
     * the arguments to the statement
     *
     * @param values the values to filter by
     */
    private void callCallback(FilterFields values) {
        if (callback != null) {
            callback.accept(values);
            callback = null;
        }
    }

    /**
     * applies the filter using the callback from the AppointmentTable controller
     *
     * @param event JavaFX action event
     */
    @FXML
    private void handleSave(ActionEvent event) {
        final int year = yearComboBox.getValue();
        final int fieldValue = comboBox.getValue().value;
        final FilterFields fields = new FilterFields(year, fieldName, fieldValue);
        callCallback(fields);
        handleClose(null);
    }

    /**
     * clears any applied filter by passing null to the callback from the AppointmentTable controller
     *
     * @param event JavaFX action event
     */
    @FXML
    private void handleClear(ActionEvent event) {
        callCallback(null);
        handleClose(null);
        toggleGroup.getToggles().clear();
    }

    /**
     * closes the filter window without applying any filter. called by the cancel button or closing the filter window
     *
     * @param event JavaFX action event
     */
    @FXML
    private void handleClose(ActionEvent event) {
        if (stage != null) stage.close();
        stage = null;
    }

    /**
     * lambda1: ensures the callback is always called
     * <p>
     * opens the filter window
     *
     * @param callback a method that will execute a sql query with arguments from the FilterFields instance
     */
    public void openFilterWindow(Consumer<FilterFields> callback) {
        this.callback = callback;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Filter.fxml"), bundle);
            loader.setController(this);
            Scene scene = new Scene(loader.load(), 400, 400);
            stage = new Stage();
            // ensures the callback is always called
            stage.setOnHidden(ev -> handleClose(null));
            stage.setScene(scene);
            stage.setTitle(bundle.getString("filter.windowTitle"));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
            handleClose(null);
        }
    }

    /**
     * the object that is returned after the filter is applied. it contains the values to be used an arguments for the
     * query
     */
    public static class FilterFields {
        final public int year;
        final public String field;
        final public int fieldValue;

        public FilterFields(int year, String field, int fieldValue) {
            this.year = year;
            this.field = field;
            this.fieldValue = fieldValue;
        }
    }

    /**
     * an object to hold ComboBox values. there is an internal value for use in querying and a display value for the
     * user to interact with
     */
    private static class ComboBoxValue {
        final public int value;
        final private String display;

        public ComboBoxValue(String display, int value) {
            this.display = display;
            this.value = value;
        }

        @Override
        public String toString() {
            return display;
        }
    }
}
