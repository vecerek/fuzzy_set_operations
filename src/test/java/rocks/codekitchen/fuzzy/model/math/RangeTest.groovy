package rocks.codekitchen.fuzzy.model.math

import spock.lang.Specification

/**
 * @author attila
 */
class RangeTest extends Specification {

    def "Initialize Range with a String"() {
        when:
            def range = new Range(sRange)

        then:
            range.start == start
            range.end == end
            range.@closed == closed

        where:
            sRange      | start     | end       | closed
            "1..10"     | 1d        | 10d       | false
            "1...10"    | 1d        | 10d       | true
            "5.2..0.75" | 0.75d     | 5.2d      | false
    }

}
