package rocks.codekitchen.fuzzy.model;

import parsii.tokenizer.ParseException;
import rocks.codekitchen.fuzzy.exception.MembershipFunctionEvaluationException;
import rocks.codekitchen.fuzzy.model.math.Math;
import rocks.codekitchen.fuzzy.model.math.Variable;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * @author attila
 */
public class MembershipFunction {

    final Math expression;
    final ArrayList<String> variables;

    public MembershipFunction(String expression) throws ParseException {
        this.expression = new Math(expression).build();
        this.variables = new ArrayList<>(this.expression.getVariableNames());
    }

    public static MembershipFunction not(MembershipFunction func) throws ParseException {
        return new MembershipFunction(String.format("1 - %s", func.toString()));
    }

    public double eval(ArrayList<Double> values) throws MembershipFunctionEvaluationException {
        if (values.size() != variables.size())
            throw new MembershipFunctionEvaluationException(
                    "Number of values given does not match the number of variables in expression");

        ArrayList<Variable<String, Double>> variables = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            variables.add(new Variable<>(this.variables.get(i), values.get(i)));
        }
        return expression.setVariables(variables).eval();
    }

    public double eval(final double value) throws MembershipFunctionEvaluationException {
        return eval(new ArrayList<Double>() {{
            add(value);
        }});
    }

    public MembershipFunction not() throws ParseException { return not(this); }

    public MembershipFunction min(MembershipFunction b) throws ParseException {
        return new MembershipFunction(Factory.Function.Min.build(this, b));
    }

    public MembershipFunction max(MembershipFunction b) throws ParseException {
        return new MembershipFunction(Factory.Function.Max.build(this, b));
    }

    public MembershipFunction minus(MembershipFunction b) throws ParseException {
        return new MembershipFunction(
                Factory.Function.If.build(Factory.Condition.Equals.build(this.toString(), b.toString()),
                        this.toString(), Factory.Condition.UNDEFINED));
    }

    public String toString() {
        return expression.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof MembershipFunction))
            return false;
        return this.toString().equals(o.toString());
    }

    private static class Factory {

        static class Function {
            static class Min {
                static String build(MembershipFunction... functions) {
                    return buildMinMax("min", functions);
                }
            }

            static class Max {
                static String build(MembershipFunction... functions) {
                    return buildMinMax("max", functions);
                }
            }

            static class If {
                static String build(String condition, String ifValue, String elseValue) {
                    return String.format("if(%s, %s, %s)", condition, ifValue, elseValue);
                }
            }

            private static String buildMinMax(String name, MembershipFunction... functions) {
                ArrayList<String> args = new ArrayList<>();
                for (MembershipFunction function : functions) {
                    args.add(function.expression.toString());
                }
                return String.format(name + "(%s)",
                        args.stream().map(Object::toString).collect(Collectors.joining(", ")));
            }
        }

        static class Condition {
            static final String UNDEFINED = "undefined";

            static class Equals {
                static String build(String lExpression, String rExpression) {
                    return String.format("%s = %s", lExpression, rExpression);
                }
            }
        }
    }
}
