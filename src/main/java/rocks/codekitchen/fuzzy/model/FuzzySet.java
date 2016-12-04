package rocks.codekitchen.fuzzy.model;

import parsii.tokenizer.ParseException;
import rocks.codekitchen.fuzzy.exception.MembershipFunctionEvaluationException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author attila
 */
public class FuzzySet {

    private static final double PRECISION = 10e14;

    final String name;
    private Map<FuzzyMember, Double> members = new HashMap<>();
    private MembershipFunction membershipFunction;

    private double height;
    private double width;
    private Map<FuzzyMember, Double> support = new HashMap<>();
    private Map<FuzzyMember, Double> core = new HashMap<>();
    private Map<FuzzyMember, Double> crossover = new HashMap<>();

    public static FuzzySet create(String name, Set<FuzzyMember> universe, String expression)
            throws ParseException, MembershipFunctionEvaluationException {
        return new FuzzySet(name, universe, new MembershipFunction(expression));
    }

    public FuzzySet(String name, Set<FuzzyMember> universe, MembershipFunction function)
            throws MembershipFunctionEvaluationException {
        this.name = name;
        this.membershipFunction = function;
        init(universe);
        if (height > 1.0) normalize();
    }

    public FuzzySet(String name, Map<FuzzyMember, Double> members, MembershipFunction function) {
        this.name = name;
        this.members = members;
        this.membershipFunction = function;
    }

    public String name() { return name; }

    public Map<FuzzyMember, Double> members() { return members; }

    public double membership(double value) throws MembershipFunctionEvaluationException {
        return membershipFunction.eval(value);
    }

    public Map<FuzzyMember, Double> support() {
        support = members.entrySet().stream()
                .filter(map -> round(map.getValue()) > 0)
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        return support;
    }

    public boolean isSingleton() {
        if (support == null)
            support();
        return support.size() == 1;
    }

    public Map<FuzzyMember, Double> core() {
        core = members.entrySet().stream()
                .filter(map -> round(map.getValue()) == 1)
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        return core;
    }

    public boolean isNormal() {
        if (core == null)
            core();
        return !core.isEmpty();
    }

    public Map<FuzzyMember, Double> crossover() {
        crossover = members.entrySet().stream()
                .filter(map -> round(map.getValue()) == 0.5)
                .collect(Collectors.toMap(p -> p.getKey(), p-> p.getValue()));
        return crossover;
    }

    public double height() { return height; }

    public Map<FuzzyMember, Double> alphaCut(double alpha) { return alphaCut(alpha, false); }

    public Map<FuzzyMember, Double> strongAlphaCut(double alpha) { return alphaCut(alpha, true); }

    public FuzzySet add(FuzzyMember member) throws MembershipFunctionEvaluationException {
        Double membershipDegree = membership(member);
        double roundedMembership = round(membershipDegree);

        if (membershipDegree > height)
            height = membershipDegree;
        if (roundedMembership > 0)
            support.put(member, membershipDegree);
        if (roundedMembership == 1)
            core.put(member, membershipDegree);
        if (roundedMembership == 0.5)
            crossover.put(member, membershipDegree);
        if (crossover.size() == 2) {
            ArrayList<FuzzyMember> crossoverPoints = new ArrayList<>(crossover.keySet());
            width = Math.abs(crossoverPoints.get(1).value() - crossoverPoints.get(0).value());
        }
        members.put(member, membershipDegree);

        return this;
    }

    public FuzzySet complement() throws ParseException {
        Map<FuzzyMember, Double> newMembers = new HashMap<>();
        for (Map.Entry<FuzzyMember, Double> member : members.entrySet()) {
            newMembers.put(member.getKey(), 1 - member.getValue());
        }

        MembershipFunction newFunction = membershipFunction.not();
        return new FuzzySet(String.format("¬(%s)", name), newMembers, newFunction);
    }

    public FuzzySet union(FuzzySet b) throws ParseException, MembershipFunctionEvaluationException {
        Map<FuzzyMember, Double> newMembers = new HashMap<>();
        Set<FuzzyMember> universe = new HashSet<>();
        universe.addAll(members.keySet());
        universe.addAll(b.members().keySet());

        double membershipA, membershipB;
        for (FuzzyMember member : universe) {
            membershipA = members.containsKey(member) ? members.get(member) : membership(member.value());
            membershipB = b.members.containsKey(member) ? b.members.get(member) : b.membership(member.value());
            newMembers.put(member, java.lang.Math.max(membershipA, membershipB));
        }

        MembershipFunction newFunction = membershipFunction.max(b.membershipFunction);
        String newName = String.format("(%s ∪ %s)", name, b.name);
        return new FuzzySet(newName, newMembers, newFunction);
    }

    public FuzzySet intersection(FuzzySet b) throws ParseException, MembershipFunctionEvaluationException {
        Map<FuzzyMember, Double> newMembers = new HashMap<>();
        Set<FuzzyMember> universe = new HashSet<>();
        universe.addAll(members.keySet());
        universe.addAll(b.members().keySet());

        double membershipA, membershipB;
        for (FuzzyMember member : universe) {
            membershipA = members.containsKey(member) ? members.get(member) : membership(member.value());
            membershipB = b.members.containsKey(member) ? b.members.get(member) : b.membership(member.value());
            newMembers.put(member, java.lang.Math.min(membershipA, membershipB));
        }

        MembershipFunction newFunction = membershipFunction.min(b.membershipFunction);
        String newName = String.format("(%s ∩ %s)", name, b.name);
        return new FuzzySet(newName, newMembers, newFunction);
    }

    public FuzzySet minus(FuzzySet b) throws ParseException, MembershipFunctionEvaluationException {
        Map<FuzzyMember, Double> newMembers = new HashMap<>();
        Set<FuzzyMember> universe = members.keySet();
        universe.addAll(b.members().keySet());

        double membershipA, membershipB;
        for (FuzzyMember member : universe) {
            membershipA = members.containsKey(member) ? members.get(member) : membership(member.value());
            membershipB = b.members.containsKey(member) ? b.members.get(member) : b.membership(member.value());
            newMembers.put(member, membershipA == membershipB ? Double.NaN : membershipA);
        }

        MembershipFunction newFunction = membershipFunction.minus(b.membershipFunction);
        String newName = String.format("(%s\\%s)", name, b.name);
        return new FuzzySet(newName, newMembers, newFunction);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof FuzzySet))
            return false;

        FuzzySet b = (FuzzySet) o;
        Set<FuzzyMember> universe = members.keySet();
        universe.addAll(b.members().keySet());

        double membershipA, membershipB;
        for (FuzzyMember member : universe) {
            try {
                membershipA = members.containsKey(member) ? members.get(member) : membership(member.value());
                membershipB = b.members.containsKey(member) ? b.members.get(member) : b.membership(member.value());
            } catch (MembershipFunctionEvaluationException e) {
                return false;
            }
            if (membershipA != membershipB) return false;
        }

        return true;
    }

    public boolean subsetOf(FuzzySet b) throws MembershipFunctionEvaluationException {
        if (b == null)
            return false;

        Set<FuzzyMember> universe = members.keySet();
        universe.addAll(b.members().keySet());

        double membershipA, membershipB;
        for (FuzzyMember member : universe) {
            membershipA = members.containsKey(member) ? members.get(member) : membership(member.value());
            membershipB = b.members.containsKey(member) ? b.members.get(member) : b.membership(member.value());
            if (membershipA > membershipB) return false;
        }

        return true;
    }

    private void init(Set<FuzzyMember> universe) throws MembershipFunctionEvaluationException {
        for (FuzzyMember member : universe) {
            add(member);
        }
    }

    private Double membership(FuzzyMember member) throws MembershipFunctionEvaluationException {
        return membershipFunction.eval(member.value());
    }

    private Map<FuzzyMember, Double> alphaCut(double alpha, boolean strong) {
        if (strong) {
            return members.entrySet().stream()
                    .filter(map -> round(map.getValue()) > alpha)
                    .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        } else {
            return members.entrySet().stream()
                    .filter(map -> round(map.getValue()) >= alpha)
                    .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        }
    }

    private void normalize() {
        members.replaceAll((k, v) -> v/ height);
    }

    private double round(double value) {
        return Math.round(value * PRECISION) / PRECISION;
    }
}
