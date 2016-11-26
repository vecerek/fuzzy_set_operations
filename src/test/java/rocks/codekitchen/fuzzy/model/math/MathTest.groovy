package rocks.codekitchen.fuzzy.model.math

/**
 * @author attila
 */
class MathTest extends GroovyTestCase {

    void testMath() {
        def strExpr = "sin(x)"

        Math expression = new Math(strExpr)

        assert strExpr == expression.@strExpression
        assert expression.@scope != null
    }

    void testSetVariable() {
        def var = 'x'
        def val = 0.0

        Math expr = new Math("sin("+var+')')
        expr.setVariable(var, val)

        assert expr.scope.context.size() == 1
        assertTrue expr.scope.context.containsKey('x')
        assert expr.scope.context.get('x').name == 'x'
        assert expr.scope.context.get('x').value == 0.0
    }

    void testSetVariables() {
        def variables = new ArrayList<Variable<String, Double>>()
        variables.add(new Variable<String, Double>('x', (Double) 0.0))
        variables.add(new Variable<String, Double>('y', (Double) 1.0))

        Math expr = new Math("sin(x) + cos(y)")
        expr.setVariables(variables)

        assert expr.scope.context.size() == 2

        assertTrue expr.scope.context.containsKey('x')
        assert expr.scope.context.get('x').name == 'x'
        assert expr.scope.context.get('x').value == 0.0

        assertTrue expr.scope.context.containsKey('y')
        assert expr.scope.context.get('y').name == 'y'
        assert expr.scope.context.get('y').value == 1.0
    }

    void testBuild() {
        def variables = new ArrayList<Variable<String, Double>>()
        variables.add(new Variable<String, Double>('x', (Double) 0.0))
        variables.add(new Variable<String, Double>('y', (Double) 1.0))

        Math expr = new Math("sin(x) + cos(y)")
        expr.build().setVariables(variables)
        // Should not fail to build a second time
        expr.build()

        assertNotNull expr.@expression
        shouldFail() {
            new Math("sin(x) , cos(y)").build()
        }

        expr = new Math("sin(x) + cos(y)").build()
        Set<String> expectedVariables = new HashSet<String>() {{
            addAll(Arrays.asList('x', 'y'))
        }}
        Set<String> vars = expr.getVariableNames()
        assertEquals(expectedVariables, vars)
    }

    void testEval() {
        def variables = new ArrayList<Variable<String, Double>>()
        variables.add(new Variable<String, Double>('x', 0.0d))

        Math expr = new Math("sin(x) + cos(pi/2)").setVariables(variables).build()
        double result = (double) java.lang.Math.round(expr.eval() * 10e14) / 10e14
        assert result == (double) 0.0


        expr = new Math("if(sin(x) = 0, sin(pi/2 + x), undefined)").setVariable('x', 0.5d).build()
        assert expr.eval() == Double.NaN
    }
}
