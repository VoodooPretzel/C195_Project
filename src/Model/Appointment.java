package Model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

public class Appointment extends Record implements Model<Appointment>, Reportable {
    private final String description;
    private final String location;
    private String title;
    private String type;
    private LocalDateTime start;
    private LocalDateTime end;
    private long customerId;
    private long userId;
    private long contactId;

    public Appointment(long id,
                       String title,
                       String description,
                       String location,
                       String type,
                       LocalDateTime start,
                       LocalDateTime end,
                       long customerId,
                       long userId,
                       long contactId) {
        super(id);
        this.title = title;
        this.description = description;
        this.location = location;
        this.type = type;
        this.start = start;
        this.end = end;
        this.customerId = customerId;
        this.userId = userId;
        this.contactId = contactId;
    }

    /**
     * @see Model#copy()
     */
    @Override
    public Appointment copy() {
        return new Appointment(id, title, description, location, type, start, end, customerId, userId, contactId);
    }

    /**
     * @see Model#toValues()
     */
    @Override
    public List<Object> toValues() {
        return new ArrayList(List.of(title,
                description,
                location,
                type,
                getSQLStart(),
                getSQLEnd(),
                customerId,
                userId,
                contactId));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type.trim();
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    /**
     * @return the start date formatted for a sql query
     */
    public String getSQLStart() {
        return formatSQLDate(start);
    }

    /**
     * @return the end date formatted for a sql query
     */
    public String getSQLEnd() {
        return formatSQLDate(end);
    }

    /**
     * formats a date for sql queries
     *
     * @param date the date to format
     * @return the string for the sql query
     */
    private static String formatSQLDate(LocalDateTime date) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")));
    }

    /**
     * @return the start date formatted for display in the table
     */
    public String getFormattedStart() {
        return formatLocalDate(start);
    }

    /**
     * @return the end date formatted for display in the table
     */
    public String getFormattedEnd() {
        return formatLocalDate(end);
    }

    /**
     * formats a date for display in the table
     *
     * @param date the date to format
     * @return the string to display
     */
    public static String formatLocalDate(LocalDateTime date) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale);
        return date.format(formatter);
    }

    /**
     * @return the start time in the local user's time zone
     */
    public ZonedDateTime getLocalStart() {
        return start.atZone(ZoneId.systemDefault());
    }

    /**
     * @return the end time in the local user's time zone
     */
    public ZonedDateTime getLocalEnd() {
        return end.atZone(ZoneId.systemDefault());
    }

    /**
     * @see Record#customValidate()
     */
    @Override
    protected void customValidate() throws ValidationError {
        final ZonedDateTime startEST = start.atZone(ZoneId.of("US/Eastern"));
        final ZonedDateTime endEST = end.atZone(ZoneId.of("US/Eastern"));
        checkDateRange(startEST, bundle.getString("appointment.start"));
        checkDateRange(endEST, bundle.getString("appointment.end"));
        if (start.compareTo(end) > 0) {
            throw new ValidationError(bundle.getString("error.startAfterEnd"));
        }
        if (!startEST.toLocalDate().equals(endEST.toLocalDate())) {
            throw new ValidationError(bundle.getString("error.notSameDay"));
        }
    }

    /**
     * validates that the record's hours fall within business hours
     *
     * @param date the start or end date and time
     * @param name the name of the field
     * @throws ValidationError an error saying the value is out of range
     */
    private void checkDateRange(ZonedDateTime date, String name) throws ValidationError {
        if (date.getHour() < 8 || date.getHour() > 22 || (date.getHour() == 22 && date.getMinute() != 0)) {
            throw new ValidationError(bundle.getString("error.invalidDateRange").replace("%{field}", name));
        }
    }

    /**
     * @see Reportable#toReportString()
     */
    @Override
    public String toReportString() {
        //  appointment ID, title, type and description, start date and time, end date and time, and customer ID
        String output = "";
        output += String.format("\t%s: %d\n", "ID", id);
        output += String.format("\t%s: %s\n", bundle.getString("appointment.title"), title);
        output += String.format("\t%s: %s\n", bundle.getString("appointment.type"), type);
        output += String.format("\t%s: %s\n", bundle.getString("appointment.description"), description);
        output += String.format("\t%s: %s\n", bundle.getString("appointment.start"), getFormattedStart());
        output += String.format("\t%s: %s\n", bundle.getString("appointment.end"), getFormattedEnd());
        output += String.format("\t%s: %s\n", bundle.getString("appointment.customerId"), customerId);
        return output + "\n";
    }
}
