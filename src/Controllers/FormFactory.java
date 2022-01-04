package Controllers;

import Model.Record;

import java.util.function.Function;

/**
 * Instantiates a form controller of type T for model R
 *
 * @param <R> a Record subclass
 * @param <T> a Form subclass
 */
public abstract class FormFactory<R extends Record, T extends Form<R>> extends Base {
    private final Class<R> modelClass;

    public FormFactory(Class<R> modelClass) {
        this.modelClass = modelClass;
    }

    /**
     * gets the correct string from the bundle based off the class name and the form mode
     *
     * @param mode the mode the form opens in
     * @return the title for the form window
     */
    protected String getTitle(Mode mode) {
        return bundle.getString(String.format("form.%s.%s", mode.toString().toLowerCase(), modelClass.getSimpleName().toLowerCase()));
    }

    /**
     * Returns an instance of the Form for the Record
     *
     * @param mode     the mode to open the form in
     * @param record   the record to create/read/update
     * @param callback the callback that will act on the record after editing has finished
     * @return the form controller instance
     */
    abstract public T getInstance(Mode mode, R record, Function<R, Boolean> callback);

    enum Mode {
        Create,
        Read,
        Update
    }
}
