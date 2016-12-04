package rocks.codekitchen.fuzzy.model;

/**
 * @author attila
 */
public class FuzzyMember<T> implements Valued {

    private T obj;
    private double value;

    public FuzzyMember(T obj, double value) {
        this.obj = obj;
        this.value = value;
    }

    public FuzzyMember(double value) {
        this.obj = (T) String.valueOf(value);
        this.value = value;
    }

    public T obj() {
        return obj;
    }

    public double value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof FuzzyMember))
            return false;
        return obj.equals(((FuzzyMember)o).obj);
    }

    @Override
    public int hashCode() {
        return obj.hashCode();
    }
}
