package rocks.codekitchen.fuzzy.model

import spock.lang.Specification

/**
 * @author attila
 */
class MembershipFunctionTest extends Specification {
    def "Constructor"() {
        expect:
            Factory.mf(expr).variables == variables

        where:
            expr                    | variables
            "1/sin(x)"              | Factory.al("x")
            "sin(x) * cos(y)"       | Factory.al("x", "y")
            "z/(sin(x) + cos(y))"   | Factory.al("x", "y", "z")
    }

    def "Not() static"() {
        expect:
            MembershipFunction.not(Factory.mf(func)) == Factory.mf(notFunc)

        where:
            func                    | notFunc
            "1/sin(x)"              | "1 - 1/sin(x)"
            "sin(x) * cos(y)"       | "1 - sin(x) * cos(y)"
            "z/(sin(x) + cos(y))"   | "1 - z/(sin(x) + cos(y))"
    }

    def "Eval with multiple variables"() {
        expect:
            round(Factory.mf(func).eval(values)) == result

        where:
            func                | values                            || result
            "sin(x) + cos(y)"   | Factory.al(0.0d, Math.PI / 2d)    || 0.0d
            "a + c"             | Factory.al(18d, 24d)              || 42d
            "b * cos(x)"        | Factory.al(5d, 0.0d)              || 5d

    }

    def "Eval with single variable"() {
        expect:
            round(Factory.mf(func).eval(value)) == result

        where:
            func                | value     || result
            "sin(x)"            | 0.0d      || 0.0d
            "10 * a"            | 4.2d      || 42d
            "y/(cos(pi/2) + 1)" | 10d       || 10d
    }

    def "Not()"() {
        expect:
            Factory.mf(func).not() == Factory.mf(notFunc)

        where:
            func                    | notFunc
            "1/sin(x)"              | "1 - 1/sin(x)"
            "sin(x) * cos(y)"       | "1 - sin(x) * cos(y)"
            "z/(sin(x) + cos(y))"   | "1 - z/(sin(x) + cos(y))"
    }

    def "Min()"() {
        expect:
            Factory.mf(funcA).min(Factory.mf(funcB)) == Factory.mf(funcC)

        where:
            funcA                   | funcB                         || funcC
            "1/sin(x)"              | "atan(y)"                     || "min(1/sin(x), atan(y))"
            "sin(x) * cos(y)"       | "1 - sin(x) * cos(y)"         || "min(sin(x) * cos(y), 1 - sin(x) * cos(y))"
            "z/(sin(x) + cos(y))"   | "1 - z/(sin(x) + cos(y))"     || "min(z/(sin(x) + cos(y)), 1 - z/(sin(x) + cos(y)))"
    }

    def "Max()"() {
        expect:
            Factory.mf(funcA).max(Factory.mf(funcB)) == Factory.mf(funcC)

        where:
            funcA                   | funcB                         || funcC
            "1/sin(x)"              | "atan(y)"                     || "max(1/sin(x), atan(y))"
            "sin(x) * cos(y)"       | "1 - sin(x) * cos(y)"         || "max(sin(x) * cos(y), 1 - sin(x) * cos(y))"
            "z/(sin(x) + cos(y))"   | "1 - z/(sin(x) + cos(y))"     || "max(z/(sin(x) + cos(y)), 1 - z/(sin(x) + cos(y)))"
    }

    def "ToString()"() {
        expect:
            new MembershipFunction(func).toString() == func

        where:
            func << ["1/sin(x)", "sin(x) * cos(y)", "z/(sin(x) + cos(y))"]
    }

    static class Factory<T> {
        static MembershipFunction mf(String func) {
            return new MembershipFunction(func)
        }

        static ArrayList<T> al(T... members) {
            return new ArrayList<T>() {{ addAll(Arrays.asList(members)) }}
        }
    }

    static final double PRECISION = 10e14d

    static double round(double value) {
        return Math.round(value * PRECISION) / PRECISION
    }
}
