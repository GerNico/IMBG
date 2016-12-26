package Uvis;

/*
 *
 * @author Matsishin Nicolas
 * This class mekes chart with line marker for area selection
 */

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.shape.Line;

import java.util.Objects;


@SuppressWarnings("unchecked")
class LineChartWithMarkers<X, Y> extends LineChart {

    private ObservableList<Data<X, Y>> horizontalMarkers;
    private ObservableList<Data<X, Y>> verticalMarkers;

    LineChartWithMarkers(Axis<X> xAxis, Axis<Y> yAxis) {
        super(xAxis, yAxis);
        horizontalMarkers = FXCollections.observableArrayList(data -> new Observable[]{data.YValueProperty()});
        horizontalMarkers.addListener((InvalidationListener) observable -> layoutPlotChildren());
        verticalMarkers = FXCollections.observableArrayList(data -> new Observable[]{data.XValueProperty()});
        verticalMarkers.addListener((InvalidationListener) observable -> layoutPlotChildren());
    }


    void addVerticalValueMarker(Data<X, Y> marker) {
        Objects.requireNonNull(marker, "the marker must not be null");
        if (verticalMarkers.contains(marker)) return;
        Line line = new Line();
        marker.setNode(line);
        getPlotChildren().add(line);
        verticalMarkers.add(marker);
    }

    //        public void removeVerticalValueMarker(Data<X, Y> marker) {
//            Objects.requireNonNull(marker, "the marker must not be null");
//            if (marker.getNode() != null) {
//                getPlotChildren().remove(marker.getNode());
//                marker.setNode(null);
//            }
//            verticalMarkers.remove(marker);
//        }
    void removeAllVerticalValueMarkers() {
        for (Data<X, Y> marker : verticalMarkers)
            getPlotChildren().remove(marker.getNode());
        verticalMarkers.remove(0, verticalMarkers.size() - 1);
    }


    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        for (Data<X, Y> horizontalMarker : horizontalMarkers) {
            Line line = (Line) horizontalMarker.getNode();
            line.setStartX(0);
            line.setEndX(getBoundsInLocal().getWidth());
            line.setStartY(getYAxis().getDisplayPosition(horizontalMarker.getYValue()) + 0.5); // 0.5 for crispness
            line.setEndY(line.getStartY());
            line.toFront();
        }
        for (Data<X, Y> verticalMarker : verticalMarkers) {
            Line line = (Line) verticalMarker.getNode();
            line.setStartX(getXAxis().getDisplayPosition(verticalMarker.getXValue()) + 0.5);  // 0.5 for crispness
            line.setEndX(line.getStartX());
            line.setStartY(0d);
            line.setEndY(getBoundsInLocal().getHeight());
            line.toFront();
        }
    }
}

