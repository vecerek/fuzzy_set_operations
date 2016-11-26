package rocks.codekitchen.fuzzy.model.math;

import rocks.codekitchen.fuzzy.exception.UnbuiltMathEvaluationException;

import parsii.eval.Expression;
import parsii.eval.Parser;
import parsii.eval.Scope;
import parsii.tokenizer.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author attila
 */
public class Math {

    private static final Set<String> DEFAULT_VARIABLES = new HashSet<String>() {{
        addAll(Arrays.asList("euler", "pi", "undefined"));
    }};

    private String strExpression;
    private Expression expression;
    private Scope scope = Scope.create();

    public Math(String expression) {
        strExpression = expression;
    }

    public Math setVariable(String name, Double value) {
        scope.getVariable(name).setValue(value);
        return this;
    }

    public Math setVariables(ArrayList<Variable<String, Double>> variables) {
        if (expression != null)
            for (Variable<String, Double> var : variables) {
                setVariable(var.getKey(), var.getValue());
            }
        return this;
    }

    public Set<String> getVariableNames() {
        Set<String> varNames = scope.getNames();
        varNames.removeAll(DEFAULT_VARIABLES);
        return varNames;
    }

    public Set<parsii.eval.Variable> getVariables() {
        return (Set<parsii.eval.Variable>) scope.getVariables();
    }

    public Math build() throws ParseException {
        if(expression == null) {
            expression = Parser.parse(strExpression, scope);
        }
        return this;
    }

    public double eval() {
        setUndefinedValueIfPresent();

        if(expression != null) {
            return expression.evaluate();
        } else throw new UnbuiltMathEvaluationException();
    }

    public String toString() {
        return strExpression;
    }

    private void setUndefinedValueIfPresent() {
        if (scope.getNames().contains("undefined"))
            if (!scope.getVariable("undefined").isConstant())
                scope.getVariable("undefined").makeConstant(Double.NaN);
    }
}
