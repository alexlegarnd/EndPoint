package ovh.alexisdelhaie.endpoint.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Param {

    private final StringProperty _name = new SimpleStringProperty();
    private final StringProperty _value = new SimpleStringProperty();

    public Param() {
        _name.set("");
        _value.set("");
    }

    public Param(String n, String v) {
        _name.set(n);
        _value.set(v);
    }

    public String getName() {
        return _name.get();
    }

    public StringProperty name() {
        return _name;
    }

    public String getValue() {
        return _value.get();
    }

    public StringProperty value() {
        return _value;
    }

    public boolean isEmpty() {
        return (_name.get().isBlank() && _value.get().isBlank());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Param{");
        sb.append("_name=").append(_name.get());
        sb.append(", _value=").append(_value.get());
        sb.append('}');
        return sb.toString();
    }
}
