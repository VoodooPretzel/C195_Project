package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class Main extends Base implements Initializable {
    private final EventEmitter eventEmitter = new EventEmitter();
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab customerTab;
    @FXML
    private Tab appointmentTab;
    private boolean customerTabInitialized = false;
    private boolean appointmentTabInitialized = false;
    private CustomerTable customerTableController;

    /**
     * lambda1: determine which tab has been selected and display the correct data
     *
     * @see Initializable#initialize(URL, ResourceBundle)
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // lambda to easily determine which tab has been selected and display the correct data
        tabPane.getSelectionModel().selectedItemProperty()
                .addListener(((observableValue, oldTab, newTab) -> populateData(newTab)));
        populateData(tabPane.getSelectionModel().getSelectedItem());
    }

    /**
     * is called whenever the tab on the main view changes, it makes sure that all the tabs are initialized
     *
     * @param newTab the currently selected tab
     */
    private void populateData(Tab newTab) {
        if (newTab == customerTab) {
            populateCustomerData();
        } else if (newTab == appointmentTab) {
            populateAppointmentData();
        }
    }

    /**
     * creates the customer table if it's not already initialized
     */
    private void populateCustomerData() {
        if (customerTabInitialized) return;
        customerTabInitialized = true;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Table.fxml"), bundle);
        customerTableController = new CustomerTable(eventEmitter);
        loader.setController(customerTableController);
        try {
            customerTab.setContent(loader.load());
        } catch (IOException ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
    }

    /**
     * creates the appointment table if it's not already initialized
     */
    private void populateAppointmentData() {
        if (appointmentTabInitialized) return;
        appointmentTabInitialized = true;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Table.fxml"), bundle);
        loader.setController(new AppointmentTable(customerTableController.getData(), eventEmitter));
        try {
            appointmentTab.setContent(loader.load());
        } catch (IOException ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
    }

    public enum Event {
        CustomerDeleted
    }

    /**
     * event emitter class. used by customer table to alert the appointment table of a customer deletion so the
     * deleted appointments can be removed from the table
     */
    final public class EventEmitter implements java.util.EventListener {
        final private HashMap<Event, List<Runnable>> eventMap = new HashMap<>();

        /**
         * registers an event listener
         *
         * @param e the event to listen to
         * @param r a callback for when the event happens
         */
        public void addListener(Event e, Runnable r) {
            List<Runnable> listeners = eventMap.get(e);
            if (listeners == null) {
                listeners = new ArrayList<>();
                eventMap.put(e, listeners);
            }
            listeners.add(r);
        }

        /**
         * calls all registered event listeners for the emitted event
         *
         * @param e the event that happened
         */
        public void emit(Event e) {
            final List<Runnable> listeners = eventMap.get(e);
            if (listeners != null) {
                for (Runnable runnable : listeners) {
                    runnable.run();
                }
            }
        }
    }
}
