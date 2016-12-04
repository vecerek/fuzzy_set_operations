package rocks.codekitchen.fuzzy.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

import parsii.tokenizer.ParseException;
import rocks.codekitchen.fuzzy.exception.MembershipFunctionEvaluationException;
import rocks.codekitchen.fuzzy.gui.FuzzyLineChart;
import rocks.codekitchen.fuzzy.helper.ApplicationHelper;
import rocks.codekitchen.fuzzy.model.FuzzyMember;
import rocks.codekitchen.fuzzy.model.FuzzySet;

import java.util.Set;

/**
 * @author attila
 */
public class MainController {

    @FXML private TextField universeDefinitionTextField;
    @FXML private Hyperlink universeDefinitionErrorHyperlink;
    @FXML private Label setNameALabel;
    @FXML private Label setNameBLabel;
    @FXML private TextField setNameATextField;
    @FXML private TextField setNameBTextField;
    @FXML private Label setNameErrorLabel;
    @FXML private TextField membershipATextField;
    @FXML private TextField membershipBTextField;
    @FXML private Label membershipAErrorLabel;
    @FXML private Label membershipBErrorLabel;
    @FXML private Button notButton;
    @FXML private Button unionButton;
    @FXML private Button intersectButton;

    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private LineChart<Number, Number> fuzzyLineChart;

    private FuzzyLineChart chartWrapper;

    private static final String setADefaultName = "Set A";
    private static final String setBDefaultName = "Set B";

    private BooleanBinding universeDefinitionValid;
    private BooleanBinding membershipAFunctionValid;
    private BooleanBinding membershipBFunctionValid;
    private BooleanBinding setNameErrorLabelMissing;

    private boolean modified = false;
    private String lastAction;

    private static final String NOT_BUTTON = "Complement";
    private static final String UNION_BUTTON = "Union";
    private static final String INTERSECT_BUTTON = "Intersection";

    private static final String membershipParseErrorText = "Invalid function";
    private static final String membershipEvalErrorText = "Use only 1 variable";

    @FXML public void initialize() {
        initializeBindings();
        addListeners();
        initializeChart();
    }

    @FXML private void handleNotButtonAction() throws ParseException, MembershipFunctionEvaluationException {
        if (modified || !lastAction.equals(NOT_BUTTON)) {
            Double width = fuzzyLineChart.getWidth();
            ApplicationHelper.setPrecision(width);
            Set<FuzzyMember> universe = ApplicationHelper.createUniverse(universeDefinitionTextField.getText());

            FuzzySet a = FuzzySet.create(setNameALabel.getText(), universe, membershipATextField.getText());
            FuzzySet notA = a.complement();
            chartWrapper.setTitle("Complement");
            chartWrapper.update(a, notA);

            modified = false;
            lastAction = NOT_BUTTON;
        }
    }

    @FXML private void handleUnionButtonAction() throws ParseException, MembershipFunctionEvaluationException {
        if (modified || !lastAction.equals(UNION_BUTTON)) {
            Double width = fuzzyLineChart.getWidth();
            ApplicationHelper.setPrecision(width);
            Set<FuzzyMember> universe = ApplicationHelper.createUniverse(universeDefinitionTextField.getText());

            FuzzySet a = FuzzySet.create(setNameALabel.getText(), universe, membershipATextField.getText());
            FuzzySet b = FuzzySet.create(setNameBLabel.getText(), universe, membershipBTextField.getText());
            FuzzySet aORb = a.union(b);
            chartWrapper.setTitle("Union");
            chartWrapper.update(a, b, aORb);

            modified = false;
            lastAction = UNION_BUTTON;
        }
    }

    @FXML private void handleIntersectButtonAction() throws ParseException, MembershipFunctionEvaluationException {
        if (modified || !lastAction.equals(INTERSECT_BUTTON)) {
            Double width = fuzzyLineChart.getWidth();
            ApplicationHelper.setPrecision(width);
            Set<FuzzyMember> universe = ApplicationHelper.createUniverse(universeDefinitionTextField.getText());

            FuzzySet a = FuzzySet.create(setNameALabel.getText(), universe, membershipATextField.getText());
            FuzzySet b = FuzzySet.create(setNameBLabel.getText(), universe, membershipBTextField.getText());
            FuzzySet aANDb = a.intersection(b);
            chartWrapper.setTitle("Intersection");
            chartWrapper.update(a, b, aANDb);

            modified = false;
            lastAction = INTERSECT_BUTTON;
        }
    }

    private void addListeners() {
        universeDefinitionTextField.textProperty().addListener((obs, oldValue, newValue) ->
                handleUniverseDefinitionTextAreaChanged(newValue));
        setNameATextField.textProperty().addListener((obs, oldValue, newValue) ->
                changeNameOfSet(newValue, setNameATextField));
        setNameBTextField.textProperty().addListener((obs, oldValue, newValue) ->
                changeNameOfSet(newValue, setNameBTextField));
        membershipATextField.textProperty().addListener((obs, oldValue, newValue) ->
                checkMembershipFunction(newValue, membershipATextField));
        membershipBTextField.textProperty().addListener((obs, oldValue, newValue) ->
                checkMembershipFunction(newValue, membershipBTextField));
    }

    private void initializeBindings() {
        universeDefinitionValid =
                Bindings.createBooleanBinding(this::isUniverseDefinitionValid, universeDefinitionTextField.textProperty());
        membershipAFunctionValid = Bindings.createBooleanBinding(() ->
                isMembershipFunctionParsable(membershipATextField), membershipATextField.textProperty());
        membershipBFunctionValid = Bindings.createBooleanBinding(() ->
                isMembershipFunctionParsable(membershipBTextField), membershipBTextField.textProperty());
        setNameErrorLabelMissing = Bindings.createBooleanBinding(() ->
                !setNameErrorLabel.isVisible(), setNameErrorLabel.visibleProperty());

        notButton.disableProperty().bind(shouldUnaryOperationBeDisabled());
        unionButton.disableProperty().bind(shouldBinaryOperationBeDisabled());
        intersectButton.disableProperty().bind(shouldBinaryOperationBeDisabled());
    }

    private void initializeChart() {
        chartWrapper = new FuzzyLineChart(fuzzyLineChart, xAxis, yAxis);
    }

    private void handleUniverseDefinitionTextAreaChanged(String definition) {
        boolean isVisible = !definition.isEmpty() && !ApplicationHelper.isUniverseDefinitionParsable(definition);
        universeDefinitionErrorHyperlink.setVisible(isVisible);
        modified = true;
    }

    private void changeNameOfSet(String name, TextField set) {
        Label label;
        String defaultName;
        if (set == setNameATextField) {
            label = setNameALabel;
            defaultName = setADefaultName;
        } else {
            label = setNameBLabel;
            defaultName = setBDefaultName;
        }

        if (name.isEmpty()) {
            label.setText(defaultName);
        } else {
            label.setText(name);
        }

        setNameErrorLabel.setVisible(isSetNameConflict());
        modified = true;
    }

    private boolean isSetNameConflict() {
        return setNameALabel.getText().equals(setNameBLabel.getText());
    }

    private void checkMembershipFunction(String function, TextField membershipTextField) {
        Label errorLabel = membershipTextField == membershipATextField ? membershipAErrorLabel : membershipBErrorLabel;
        boolean parsable = isMembershipFunctionParsable(function);
        boolean evaluable = true;
        if (parsable) {
            evaluable = isMembershipFunctionEvaluable(function);
        } else {
            errorLabel.setText(membershipParseErrorText);
        }
        if (parsable && !evaluable)
            errorLabel.setText(membershipEvalErrorText);

        errorLabel.setVisible(!parsable || !evaluable);
        modified = true;
    }

    private boolean isUniverseDefinitionValid() {
        String definition = universeDefinitionTextField.getText();
        return !definition.isEmpty() && ApplicationHelper.isUniverseDefinitionParsable(definition);
    }

    private boolean isMembershipFunctionParsable(String function) {
        return !function.isEmpty() && ApplicationHelper.isMembershipFunctionParsable(function);
    }

    private boolean isMembershipFunctionParsable(TextField membershipTextField) {
        return isMembershipFunctionParsable(membershipTextField.getText());
    }

    private boolean isMembershipFunctionEvaluable(String function) {
        return !function.isEmpty() && ApplicationHelper.isMembershipFunctionEvaluable(function);
    }

    private BooleanBinding shouldUnaryOperationBeDisabled() {
        return universeDefinitionValid.not()
                .or(membershipAFunctionValid.not().and(membershipBFunctionValid.not()));
    }

    private BooleanBinding shouldBinaryOperationBeDisabled() {
        return universeDefinitionValid.not()
                .or(setNameErrorLabelMissing.not())
                .or(membershipAFunctionValid.not().or(membershipBFunctionValid.not()));
    }
}
