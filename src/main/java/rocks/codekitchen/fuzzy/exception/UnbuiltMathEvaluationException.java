package rocks.codekitchen.fuzzy.exception;

/**
 * @author attila
 */
public class UnbuiltMathEvaluationException extends RuntimeException {

    public static final String MSG = "build() not called before eval().";

    public UnbuiltMathEvaluationException() {
        super(MSG);
    }

    public UnbuiltMathEvaluationException(String msg) {
        super(msg);
    }
}
