package rocks.codekitchen.fuzzy.gui;

import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import org.jetbrains.annotations.Contract;
import rocks.codekitchen.fuzzy.model.FuzzyMember;
import rocks.codekitchen.fuzzy.model.FuzzySet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author attila
 */
public class FuzzyLineChart {

    private LineChart<Number, Number> lineChart;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private static final int TICK_COUNT = 5;

    private double domainMax;
    private double domainMin;

    public FuzzyLineChart(LineChart<Number, Number> lineChart, NumberAxis xAxis, NumberAxis yAxis) {
        this.lineChart = lineChart;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.lineChart.setCreateSymbols(false);
        this.lineChart.setAnimated(true);
        setDomains();
        initAxes();
    }

    public void setTitle(String title) { this.lineChart.setTitle(title); }

    public void update(FuzzySet a, FuzzySet b, FuzzySet c) {
        resetDomains();
        lineChart.getData().removeAll(lineChart.getData());
        XYChart.Series<Number, Number> seriesA = setToSeries(a);
        XYChart.Series<Number, Number> seriesB = setToSeries(b);
        XYChart.Series<Number, Number> seriesC = setToSeries(c);
        updateAxis();
        lineChart.getData().add(seriesA);
        lineChart.getData().add(seriesB);
        if (seriesC != null)
            lineChart.getData().add(seriesC);
    }

    public void update(FuzzySet a, FuzzySet b) {
        update(a, b, null);
    }

    private void initAxes() {
        xAxis.setLabel("x");
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(100);
        xAxis.setTickUnit(getTickUnit(0, 100, true));

        yAxis.setLabel("memb(x)");
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(1.0);
        yAxis.setTickUnit(getTickUnit(0, 1.0));
    }

    private void updateAxis() {
        xAxis.setLowerBound(domainMin);
        xAxis.setUpperBound(domainMax);
        xAxis.setTickUnit(getTickUnit(domainMin, domainMax));
    }

    private void resetDomains() {
        domainMax = Double.MIN_VALUE;
        domainMin = Double.MAX_VALUE;
    }

    private void setDomains() { resetDomains(); }

    private double getTickUnit(double min, double max, boolean ceil) {
        double tickUnit = (max - min)/TICK_COUNT;
        if (ceil)
            tickUnit = Math.ceil(tickUnit);
        return tickUnit;
    }

    private double getTickUnit(double min, double max) {
        return getTickUnit(min, max, false);
    }

    private Set<XYChart.Series<Number, Number>> setsToSeries(FuzzySet... sets) {
        Set<XYChart.Series<Number, Number>> allSeries = new HashSet<>();
        for (FuzzySet set : sets)
            allSeries.add(setToSeries(set));
        return allSeries;
    }

    @Contract("null -> null")
    private XYChart.Series<Number, Number> setToSeries(FuzzySet set) {
        if (set == null)
            return null;
        double x;

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(set.name());
        ObservableList<XYChart.Data<Number, Number>> data = series.getData();

        for (Map.Entry<FuzzyMember, Double> entry : set.members().entrySet()) {
            x = entry.getKey().value();
            if (x > domainMax)
                domainMax = x;
            if (x < domainMin)
                domainMin = x;
            data.add(new XYChart.Data<>(x, entry.getValue()));
        }
        return series;
    }
}
