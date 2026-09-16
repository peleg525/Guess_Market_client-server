package gm.ui.component;

import gm.engine.dto.PricePointDto;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.List;

/** Bonus: price of one option over the sequence of trades that touched it. */
public class PriceChart extends LineChart<Number, Number> {

    public PriceChart(String title) {
        super(new NumberAxis(), new NumberAxis());
        setTitle(title);
        setCreateSymbols(true);
        setAnimated(false);
        setPrefHeight(200);
        ((NumberAxis) getXAxis()).setLabel("Trade #");
        ((NumberAxis) getYAxis()).setLabel("Price");
    }

    public void render(String seriesName, List<PricePointDto> points) {
        getData().clear();
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(seriesName);
        int i = 1;
        for (PricePointDto point : points) {
            series.getData().add(new XYChart.Data<>(i++, point.getPrice()));
        }
        getData().add(series);
    }
}
