package rocks.codekitchen.fuzzy.model

import spock.lang.Specification

/**
 * @author attila
 */
class FuzzyMemberTest extends Specification {
    def "Obj is null, when not specified"() {
        expect:
            new FuzzyMember(value).obj() == String.valueOf(value)

        where:
            value   << [0.0d, 5.0d, -17e-1d]
    }

    def "Obj is obj"() {
        expect:
            new FuzzyMember(obj, value).obj() == obj

        where:
            obj                                         | value
            "1"                                         | 1d
            new ArrayList<String>() {{
                addAll(Arrays.asList("1", "2", "3"))
            }}                                          | 3d
            new Object()                                | 0d
    }

    def "Value"() {
        expect:
            new FuzzyMember(obj, value).value() == value

        where:
            obj                                         | value
            "1"                                         | 1d
            new ArrayList<String>() {{
                addAll(Arrays.asList("1", "2", "3"))
            }}                                          | 3d
            new Object()                                | 0d
    }

    def "Equals"() {
        expect:
            new FuzzyMember(obj1, value1) == new FuzzyMember(obj2, value2)

        where:
            obj1    | value1  | obj2    | value2
            "a"     | 1.001d  | "a"     | 1.001d
            "b"     | 2.00d   | "b"     | 3.00d
    }

    def "HashCode"() {
        expect:
            new FuzzyMember(obj, value).hashCode() == obj.hashCode()

        where:
            obj     | value
            "a"     | 3.14d
            2.00d   | 2.00d
    }
}
