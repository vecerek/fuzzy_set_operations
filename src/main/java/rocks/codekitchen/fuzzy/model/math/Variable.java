package rocks.codekitchen.fuzzy.model.math;

import java.util.Map;

/**
 * @author attila
 */
public final class Variable<String,Double> implements Map.Entry<String,Double> {
    private final String key;
    private Double value;

    public Variable(String key, Double value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Double getValue() {
        return value;
    }

    public Double setValue(Double value) {
        Double old = this.value;
        this.value = value;
        return old;
    }
}
