package Model;

import java.lang.reflect.Field;
import java.util.List;

public interface Model<T> {
    T copy();

    List<Object> toValues();

    /**
     * copies fields from one instance of the record to this one
     *
     * @param other another instance of the class
     * @return this
     */
    default T applyChanges(T other) {
        for (Field declaredField : getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);
            try {
                declaredField.set(this, declaredField.get(other));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return (T) this;
    }
}
