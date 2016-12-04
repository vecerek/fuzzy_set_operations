package rocks.codekitchen.fuzzy.helper;

import parsii.tokenizer.ParseException;
import rocks.codekitchen.fuzzy.model.FuzzyMember;
import rocks.codekitchen.fuzzy.model.MembershipFunction;
import rocks.codekitchen.fuzzy.model.math.Range;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author attila
 */
public class ApplicationHelper {

    public static final String NUMBER = "[-+]?\\d+(\\.\\d+)?([eE][-+]?\\d*)?";
    public static final String SEPARATOR = ",\\s*";
    public static final String RANGE_OPERATOR = "\\s*\\.{2,3}\\s*";
    public static final String CLOSED_RANGE_OPERATOR = "\\s*\\.{3}\\s*";
    public static final String RANGE = String.format("%s%s%s", NUMBER, RANGE_OPERATOR, NUMBER);

    private static double LINECHART_WIDTH;

    public static Set<FuzzyMember> createUniverse(String specification) {
        if (isEnumeration(specification)) {
            return parseEnumeration(specification);
        } else if (isRange(specification)) {
            return parseRange(specification);
        } else {
            throw new IllegalArgumentException("The specification of the universe could not be parsed.");
        }
    }

    public static void setPrecision(double precision) {
        LINECHART_WIDTH = precision;
    }

    public static double getPrecision(double sizeOfRanges) {
        return sizeOfRanges/(LINECHART_WIDTH - 1);
    }

    public static boolean isUniverseDefinitionParsable(String specification) {
        return isEnumeration(specification) || isRange(specification);
    }

    public static boolean isMembershipFunctionParsable(String function) {
        try {
            new MembershipFunction(function);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private static boolean isEnumeration(String specification) {
        return specification.matches(String.format("(%s%s)*%s", NUMBER, SEPARATOR, NUMBER));
    }

    private static boolean isRange(String specification) {
        return specification.matches(String.format("(%s%s)*%s", RANGE, SEPARATOR, RANGE));
    }

    private static Set<FuzzyMember> parseEnumeration(String specification) {
        Set<FuzzyMember> universe = new HashSet<>();
        Matcher m = Pattern.compile(NUMBER).matcher(specification);
        while(m.find())
            universe.add(new FuzzyMember(Double.valueOf(m.group())));

        return universe;
    }

    private static Set<FuzzyMember> parseRange(String specification) {
        Set<Range> sRanges = new HashSet<>();
        Matcher mRanges = Pattern.compile(RANGE).matcher(specification);

        while(mRanges.find()) {
            Range range = new Range(mRanges.group());
            boolean joined = false;
            for (Range tmpRange : sRanges) {
                if (range.hasIntersactionWith(tmpRange)) {
                    Range newRange = tmpRange.union(range);
                    sRanges.remove(tmpRange);
                    sRanges.add(newRange);
                    joined = true;
                }
            }
            if (!joined)
                sRanges.add(range);
        }

        double size = 0;
        for (Range range : sRanges)
            size += range.size();

        Set<FuzzyMember> universe = new HashSet<>();
        for (Range range : sRanges)
            universe.addAll(range.generate(size));

        return universe;
    }
}
