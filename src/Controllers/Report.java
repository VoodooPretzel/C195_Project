package Controllers;

import Model.Appointment;
import Model.Contact;
import Model.Customer;
import Model.Division;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controls the contents of the reports tab. Queries and formats the data for consumption
 */
public class Report extends Base {
    @FXML
    private TextArea textArea;

    /**
     * called when any of the report buttons are pushed. calles the correct report function and sets its return value
     * to the TextArea
     *
     * @param event JavaFX action event
     */
    @FXML
    private void runReport(ActionEvent event) {
        textArea.clear();
        final String button = ((Button) event.getSource()).getId().replace("button", "");
        String report;
        switch (button) {
            case "1":
                report = report1();
                break;
            case "2":
                report = report2();
                break;
            case "3":
                report = report3();
                break;
            default:
                System.out.println("unreachable unhandled report button");
                report = "";
        }
        textArea.setText(report);
    }

    /**
     * runs the first report to get the total number of appointments by month and by type
     *
     * @return the string to display
     */
    private String report1() {
        return bundle.getString("report.byMonth")
                + ":\n"
                + executeQuery("SELECT MONTH(`Start`) as `Month`, COUNT(*) as `Count` " +
                "FROM appointments GROUP BY MONTH(`Start`) " +
                "ORDER BY MONTH(`Start`)", this::parseMonthsCount)
                + "\n"
                + bundle.getString("report.byType")
                + ":\n"
                + executeQuery("SELECT `Type`, COUNT(*) as `Count` " +
                "FROM appointments GROUP BY `Type` " +
                "ORDER BY `Type`", this::parseTypesCount);

    }

    /**
     * parses the number of appointments by month into a human-readable format
     *
     * @param ex a sql exception from the query
     * @param rs the result set containing report values
     * @return the string to display
     */
    private String parseMonthsCount(SQLException ex, ResultSet rs) {
        if (ex != null) return "";
        final StringBuilder output = new StringBuilder();
        try {
            while (rs.next()) {
                final String month;
                month = bundle.getString(String.format("month.%d", rs.getInt(1)));
                output.append(String.format("\t%s:\t%d\n", month, rs.getInt(2)));
            }
        } catch (SQLException exception) {
            printSQLException(exception);
        }
        return output.toString();
    }

    /**
     * parses the number of appointments by type into a human-readable format
     *
     * @param ex a sql exception from the query
     * @param rs the result set containing report values
     * @return the string to display
     */
    private String parseTypesCount(SQLException ex, ResultSet rs) {
        if (ex != null) return "";
        String output = "";
        try {
            while (rs.next()) {
                output += String.format("\t%s:\t%d\n", rs.getString(1), rs.getInt(2));
            }
        } catch (SQLException exception) {
            printSQLException(exception);
        }
        return output;
    }

    /**
     * runs the second report to get a schedule of appointments per contact
     *
     * @return the string to display
     */
    private String report2() {
        return executeQuery("SELECT Appointment_ID, Title, Description, `Location`, `Type`, `Start`, `End`, " +
                "Customer_ID, User_ID, c.Contact_ID, c.Contact_Name, c.Email " +
                "FROM appointments a " +
                "JOIN contacts c ON c.Contact_ID = a.Contact_ID " +
                "ORDER BY Contact_ID, `Start`", this::parseContactsAndAppointments);
    }

    /**
     * parses the appointments and customers into a human-readable string for display
     *
     * @param ex a sql exception from the query
     * @param rs the result set containing report values
     * @return the string to display
     */
    private String parseContactsAndAppointments(SQLException ex, ResultSet rs) {
        if (ex != null) return "";
        final StringBuilder output = new StringBuilder();
        try {
            while (rs.next()) {
                long customerId = 0L;
                final Appointment appointment = new Appointment(rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getTimestamp(6).toLocalDateTime(),
                        rs.getTimestamp(7).toLocalDateTime(),
                        rs.getLong(8),
                        rs.getLong(9),
                        rs.getLong(10));
                if (customerId != appointment.getCustomerId()) {
                    customerId = appointment.getCustomerId();
                    output.append("\n");
                    output.append(new Contact(rs.getLong(10), rs.getString(11), rs.getString(12)).toReportString());
                }
                output.append(appointment.toReportString());
            }
        } catch (SQLException exception) {
            printSQLException(exception);
        }

        return output.toString();
    }

    /**
     * runs the third report to get a rundown of the customers per division
     *
     * @return the string to display
     */
    private String report3() {
        return executeQuery("SELECT Customer_ID, Customer_Name, Address, Postal_Code, Phone, d.Division_ID, d.Country_ID, d.Division " +
                "FROM customers c " +
                "JOIN first_level_divisions d ON d.Division_ID = c.Division_ID " +
                "ORDER BY d.Division, c.Customer_ID", this::parseCustomersAndDivisions);
    }

    /**
     * parses the divisions and customers into a human-readable string for display
     *
     * @param ex a sql exception from the query
     * @param rs the result set containing report values
     * @return the string to display
     */
    private String parseCustomersAndDivisions(SQLException ex, ResultSet rs) {
        if (ex != null) return "";
        final StringBuilder output = new StringBuilder();
        try {
            long divisionId = 0L;
            while (rs.next()) {
                final Customer customer = new Customer(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getLong(6)
                );
                if (divisionId != customer.getDivisionId()) {
                    divisionId = customer.getDivisionId();
                    output.append("\n");
                    output.append(new Division(rs.getLong(6), rs.getString(8), rs.getLong(7)).toReportString());
                }
                output.append(customer.toReportString());
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return output.toString();
    }
}
