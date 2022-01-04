package Controllers;

import Model.Appointment;
import Model.Contact;
import Model.Customer;
import Model.Record;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class AppointmentFormFactory extends FormFactory<Appointment, AppointmentForm> {
    private Map<Long, Contact> contactMap;
    private List<Customer> customers;

    public AppointmentFormFactory(Class<Appointment> modelClass) {
        super(modelClass);
    }

    /**
     * @see FormFactory#getInstance(Mode, Record, Function)
     */
    @Override
    public AppointmentForm getInstance(Mode mode, Appointment record, Function<Appointment, Boolean> callback) {
        return new AppointmentForm(getTitle(mode), contactMap, customers, mode, record, callback);
    }

    /**
     * sets the contact map that is passed to every form controller instance. it prevents excessive sql requests
     *
     * @param contactMap a map of contactId to contact models
     */
    public void setContactMap(Map<Long, Contact> contactMap) {
        this.contactMap = contactMap;
    }

    /**
     * passes a list of all customers to every form controller instance. it prevents excessive sql requests
     *
     * @param customers a list of all customers
     */
    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }
}