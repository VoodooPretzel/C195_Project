package Model;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class Record {
    public static ResourceBundle bundle;
    public static Locale locale;
    protected long id;

    public Record(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * gets error message content telling user that the field cannot be empty
     *
     * @param field the field that is empty
     * @return the message for the error window
     */
    private String getEmptyErrorMessage(String field) {
        final String key = String.format("%s.%s", getClass().getSimpleName().toLowerCase(), field);
        final String title = bundle.getString(key);
        final String issue = bundle.getString("issue.empty");
        final String message = bundle.getString("error.empty").replace("%{field}", title).replace("%{issue}", issue);
        return String.format(message, field, issue);
    }

    /**
     * iterates over all declared fields for a record performing validation on longs and strings
     *
     * @throws ValidationError the invalid field error
     */
    public void validate() throws ValidationError {
        for (final Field declaredField : getClass().getDeclaredFields()) {
            try {
                declaredField.setAccessible(true);
                final Object value = declaredField.get(this);
                if (value instanceof String) {
                    if (((String) value).length() == 0) {
                        throw new ValidationError(getEmptyErrorMessage(declaredField.getName()));
                    }
                } else if (value instanceof Long) {
                    if ((Long) value == 0) {
                        throw new ValidationError(getEmptyErrorMessage(declaredField.getName()));
                    }
                } else if (value instanceof LocalDateTime) {
                } else {
                    throw new ValidationError("unreachable");
                }
            } catch (IllegalAccessException ex) {
                System.out.println(ex);
            }
        }

        customValidate();
    }

    /**
     * to be overridden by subclasses that have non-String fields to validate
     *
     * @throws ValidationError the invalid field error
     */
    protected void customValidate() throws ValidationError {
    }

    public class ValidationError extends Exception {
        public ValidationError(String message) {
            super(message);
        }
    }
}
