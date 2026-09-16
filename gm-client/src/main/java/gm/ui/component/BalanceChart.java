package gm.ui.component;

import gm.engine.dto.BalancePointDto;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.List;

/** Bonus: a user's account balance over time. */
public class BalanceChart extends LineChart<Number, Number> {

    public BalanceChart() {
        super(new NumberAxis(), new NumberAxis());
        setTitle("Account balance history");
        setCreateSymbols(true);
        setAnimated(false);
        setPrefHeight(200);
        ((NumberAxis) getXAxis()).setLabel("Transaction #");
        ((NumberAxis) getYAxis()).setLabel("Balance");
    }

    public void render(List<BalancePointDto> points) {
        getData().clear();
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Balance");
        int i = 1;
        for (BalancePointDto point : points) {
            XYChart.Data<Number, Number> data = new XYChart.Data<>(i++, point.getBalance());
            series.getData().add(data);
        }
        getData().add(series);
    }
}
