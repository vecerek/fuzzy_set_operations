package rocks.codekitchen.fuzzy.model.math;

import rocks.codekitchen.fuzzy.helper.ApplicationHelper;
import rocks.codekitchen.fuzzy.model.FuzzyMember;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static rocks.codekitchen.fuzzy.helper.ApplicationHelper.NUMBER;
import static rocks.codekitchen.fuzzy.helper.ApplicationHelper.CLOSED_RANGE_OPERATOR;

/**
 * @author attila
 */
public class Range {

    public double start;
    public double end;
    private boolean closed;
    private static final Pattern numberPattern = Pattern.compile("(" + NUMBER + ")");

    public Range(double a, double b) {
        this.start = java.lang.Math.min(a, b);
        this.end = java.lang.Math.max(a, b);
        closed = true;
    }

    public Range(String r) {
        Matcher numberMatcher = numberPattern.matcher(r);
        ArrayList<Double> bounds = new ArrayList<>(2);
        while (numberMatcher.find()) {
            bounds.add(Double.valueOf(numberMatcher.group()));
        }
        Collections.sort(bounds);
        this.start = bounds.get(0);
        this.end = bounds.get(1);
        closed = r.matches(".*" + CLOSED_RANGE_OPERATOR + ".*");
    }

    public double size() { return end - start; }

    public boolean hasIntersactionWith(Range o) {
        return start >= o.start && start <= o.end || end >= o.start && end <= o.end;
    }

    public Range union(Range o) {
        if (this.hasIntersactionWith(o)) {
            double newStart = java.lang.Math.min(start, o.start);
            double newEnd = java.lang.Math.max(end, o.end);
            return new Range(newStart, newEnd);
        }
        return null;
    }

    public Set<FuzzyMember> generate(double sizeOfRanges) {
        double precision = ApplicationHelper.getPrecision(sizeOfRanges);
        long limit = calculateLimit(precision);
        return DoubleStream.iterate(start, n -> n + precision)
                .limit(limit)
                .boxed()
                .map((Function<Double, FuzzyMember>) FuzzyMember::new)
                .collect(Collectors.toSet());
    }

    private long calculateLimit(double precision) {
        long limit = (long) java.lang.Math.floor((end - start)/precision);
        if (closed) limit += 1;
        return limit;
    }
}
