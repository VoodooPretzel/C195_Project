package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;

/**
 * negotiates the login flow
 */
public final class Login extends Base implements Initializable {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private Label zoneLabel;

    /**
     * validates the password matches for the return row (if any). also hard-coded to allow for a test account
     *
     * @param ex a SQL exception generated by the query
     * @param rs a result set containing 1 or 0 rows
     * @return whether the password from the form matches the password in the database
     */
    private long validateUsernameAndPassword(SQLException ex, ResultSet rs) {
        long result = -1L;
        if (ex == null) {
            final String username = usernameField.getText();
            final String password = passwordField.getText();
            try {
                if (rs.next() && (rs.getString("Password").trim().equals(hashPassword().trim()) || (username == "test" && password == "test"))) {
                    result = rs.getLong("User_ID");
                }
            } catch (SQLException exc) {
                printSQLException(exc);
            }
        }

        return result;
    }

    /**
     * validates that the required fields aren't empty and then checks
     *
     * @param event JavaFX button press event
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        final String username = usernameField.getText();
        final String password = passwordField.getText();
        if (username.length() != 0 && password.length() != 0) {
            final List<Object> arguments = new ArrayList<>();
            arguments.add(username);
            final long userId = executeQuery("SELECT User_ID, Password " +
                    "FROM users " +
                    "WHERE User_Name = ? " +
                    "LIMIT 1", arguments, this::validateUsernameAndPassword);
            logLoginAttempt(userId != -1);
            if (userId != -1) {
                Base.userId = userId;
                viewController.showMainView();
            } else {
                displayError(bundle.getString("error.invalidCredentials"));
            }
        }
    }

    /**
     * Logs user in when the "Enter" key is pressed
     *
     * @param event JavaFX key event
     */
    @FXML
    private void handleEnter(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin(null);
        }
    }

    /**
     * Obviously horribly unsafe but better than storing passwords in the clear.
     *
     * @return the hashed password
     */
    private String hashPassword() {
        try {
            final byte[] messageDigest = MessageDigest.getInstance("SHA-512").digest(passwordField.getText().getBytes());
            return Base64.getEncoder().encodeToString(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * logs every log in attempt with a ISO timestamp, the attempted username, and whether the attempt was successful
     *
     * @param success whether the login attempt was successful
     */
    private void logLoginAttempt(boolean success) {
        final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        final String time = formatter.format(OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        final String username = usernameField.getText();
        try {
            final FileWriter fw = new FileWriter("login_activity.txt", true);
            final BufferedWriter bw = new BufferedWriter(fw);
            bw.write("time: " + time + "\t");
            bw.write("username: " + username + "\t");
            bw.write("success: " + success + "\t");
            bw.newLine();
            bw.close();
        } catch (IOException ex) {
            System.out.println("Failed to log invalid login attempt:");
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        zoneLabel.setText(getLocale().toString());
    }
}