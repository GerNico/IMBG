package Uvis;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

@SuppressWarnings("unchecked")
public class Main extends Application {
    private final NumberAxis xAxis = new NumberAxis();
    private final NumberAxis yAxis = new NumberAxis();
    private LineChartWithMarkers<Number, Number> mainChart;
    private ArrayList<XYChart.Series> allCurves = new ArrayList<>();
    private ObservableList<Double> wl;
    private ChoiceBox<Double> st, fin;
    private ChoiceBox<Integer> order;
    private Stage MyStage;
    private ParseXML wb;
    private Menu spectra;
    private Path path;
    private BorderPane chartBox;
    private Rectangle rect;
    private double initXLowerBound = 0, initXUpperBound = 0, initYLowerBound = 0, initYUpperBound = 0;
    private SimpleDoubleProperty rectinitX = new SimpleDoubleProperty();
    private SimpleDoubleProperty rectinitY = new SimpleDoubleProperty();
    private SimpleDoubleProperty rectX = new SimpleDoubleProperty();
    private SimpleDoubleProperty rectY = new SimpleDoubleProperty();
    private final ObservableList<CurveMaxRecord> data = FXCollections.observableArrayList();
    private TableView<CurveMaxRecord> table;
    private static DataFormat dataFormat = new DataFormat("mydata");
    private ObservableList<Integer> selectedIndexes = FXCollections.observableArrayList();
    private static final String[] colors = {"Coral", "Darkred", "Deeppink", "Lightgreen", "Mediumblue", "Olivedrab",
            "Rosybrown", "Blue","Orange", "Peru", "Aquamarine", "Gold", "orchid", "cyan", "indigo", "lightseagreen",
            "plum","firebrick", "magent", "seagreen", "purple", "green", "black"};

    @Override
    public void start(Stage primaryStage) throws Exception {
        MyStage = primaryStage;

        MyStage.setTitle("Select the file");
        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Menu");
        spectra = new Menu("Spectrum");

        MenuItem fileOpen = new MenuItem("Select the file");
        fileOpen.setOnAction(event -> open());
        MenuItem fileSave = new MenuItem("Save file");

        menu.getItems().addAll(fileOpen, fileSave);
        menuBar.getMenus().addAll(menu, spectra);

        xAxis.setLabel("Wavelength (nm)");
        yAxis.setLabel("Absorbance/10");
        xAxis.setAutoRanging(false);
        yAxis.setAutoRanging(false);
        mainChart = new LineChartWithMarkers<>(xAxis, yAxis);
        mainChart.setAnimated(false);
        mainChart.setLegendVisible(false);
        chartBox = new BorderPane(mainChart);
        chartBox.setTop(menuBar);
        chartBox.getStylesheets().setAll("series.css");


        wl = FXCollections.observableArrayList();
        st = new ChoiceBox<>(wl);
        fin = new ChoiceBox<>(wl);
        ObservableList<Integer> ord = FXCollections.observableArrayList();
        ord.addAll(2, 3);
        order = new ChoiceBox<>(ord);
        order.setValue(2);

        Label lbSt = new Label("Start");
        Label lbFin = new Label("the end");
        Label poly = new Label("polynomial ord.");

        Button fit = new Button("fit the curve");
        fit.setOnAction(event -> justFit(order.getValue()));

        HBox selectRange = new HBox(10, lbSt, st, lbFin, fin, poly, order, fit);
        selectRange.setPadding(new Insets(10, 10, 10, 10));

        VBox myForm = new VBox(10, chartBox, selectRange);
        myForm.setPadding(new Insets(10, 10, 10, 10));

        MyStage.setScene(new Scene(myForm));
        MyStage.setMinWidth(140);
        MyStage.setMinHeight(120);
        MyStage.setResizable(false);

        MyStage.show();
//------------------- zoom
        path = new Path();
        path.setStrokeWidth(1);
        path.setStroke(Color.BLACK);

        chartBox.setOnMouseClicked(mouseHandler);
        chartBox.setOnMouseDragged(mouseHandler);
        chartBox.setOnMouseEntered(mouseHandler);
        chartBox.setOnMouseExited(mouseHandler);
        chartBox.setOnMouseMoved(mouseHandler);
        chartBox.setOnMousePressed(mouseHandler);
        chartBox.setOnMouseReleased(mouseHandler);
        chartBox.getChildren().add(path);

        rect = new Rectangle();
        rect.setFill(Color.web("blue", 0.1));
        rect.setStroke(Color.BLUE);
        rect.setStrokeDashOffset(50);

        rect.widthProperty().bind(rectX.subtract(rectinitX));
        rect.heightProperty().bind(rectY.subtract(rectinitY));
        chartBox.getChildren().add(rect);
    }

    private void open() {
        final FileChooser fileOpener = new FileChooser();
        fileOpener.setTitle("Open the file");
        fileOpener.getExtensionFilters().
                addAll(new FileChooser.ExtensionFilter("Spectrum", "*.xml"));

        File file = fileOpener.showOpenDialog(MyStage);
        if (file != null) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);
                wb = new ParseXML(document);
                makeItems();
                makeData();
                setAxsisDiap(wb, 0, wb.workSheets.get(0).wavelength.size() - 1);
            } catch (ParserConfigurationException | SAXException | IOException e) {
                showMessage(e.getMessage());
            }
        }
    }

    private static void showMessage(String message) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("error linked with file processing");
        stage.setMinWidth(250);
        stage.setMinHeight(150);
        Label lbl = new Label();
        lbl.setText(message);
        Button btnOK = new Button();
        btnOK.setText("OK");
        btnOK.setOnAction(e -> stage.close());
        VBox pane = new VBox(20);
        pane.getChildren().addAll(lbl, btnOK);
        pane.setAlignment(Pos.CENTER);
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void showTable() {
        Stage stage2 = new Stage();
        stage2.initModality(Modality.NONE);
        stage2.setTitle("Support window");
        stage2.setMinWidth(300);
        stage2.setMinHeight(180);

        final Label label = new Label("fit results");
        label.setFont(new Font("Arial", 20));

        table = new TableView<>();

        TableColumn<CurveMaxRecord, String> curveNameCol = new TableColumn<>("User Name");
        TableColumn<CurveMaxRecord, String> wavelengthCol = new TableColumn<>("λmax, nm");
        TableColumn<CurveMaxRecord, String> absorptionCol = new TableColumn<>("A/10");
        //-------------------обовязково привязуй по назві поля в обєкті--------------
        curveNameCol.setCellValueFactory(new PropertyValueFactory<>("curveName"));
        wavelengthCol.setCellValueFactory(new PropertyValueFactory<>("wavelength"));
        absorptionCol.setCellValueFactory(new PropertyValueFactory<>("absorption"));
        //---------------------------------------------------------------------------

        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getSelectionModel().clearSelection();
        table.getSelectionModel().setCellSelectionEnabled(false);

        table.getSelectionModel().getSelectedIndices().addListener((ListChangeListener<Integer>)
                change -> selectedIndexes.setAll(change.getList()));

        setRowFactory();


        MenuItem item = new MenuItem("Copy");
        item.setOnAction(event -> {
            ObservableList<TablePosition> posList = table.getSelectionModel().getSelectedCells();
            int old_r = -1;
            StringBuilder clipboardString = new StringBuilder();

            for (TablePosition p : posList) {
                int r = p.getRow();
                int size = table.getColumns().size();
                for (int i = 0; i < size; i++) {
                    Object cell = table.getColumns().get(i).getCellData(r);

                    if (cell == null)
                        cell = "";
                    if (old_r == r)
                        clipboardString.append('\t');
                    else if (old_r != -1)
                        clipboardString.append('\n');
                    clipboardString.append(cell);
                    old_r = r;
                }
            }

            final ClipboardContent content = new ClipboardContent();
            content.putString(clipboardString.toString());
            Clipboard.getSystemClipboard().setContent(content);
        });
        ContextMenu menu = new ContextMenu();
        menu.getItems().add(item);
        table.setContextMenu(menu);


        table.setEditable(true);
        table.setItems(data);

        table.getColumns().addAll(curveNameCol, wavelengthCol, absorptionCol);

        StackPane root = new StackPane();
        root.setPadding(new Insets(5));
        root.getChildren().add(table);
        Scene scene = new Scene(root, 180, 300);
        stage2.setScene(scene);
        stage2.show();

    }

    private void makeItems() {
        ArrayList<CheckMenuItem> items = new ArrayList<>();
        for (int i = 0; i < wb.workSheets.size(); i++) {
            CheckMenuItem item = new CheckMenuItem(wb.workSheets.get(i).title);
            item.setSelected(false);
            int finalI = i;
            item.setOnAction(event -> {
                wb.workSheets.get(finalI).selected = item.isSelected();
                if (item.isSelected()) addCurve(finalI);
                else removeCurve(finalI);
                setAxsisDiap(wb, 0, wb.workSheets.get(0).wavelength.size() - 1);
                styleSeries();
            });
            items.add(item);
        }
        items.get(0).setSelected(true);
        spectra.getItems().addAll(items);
        for (int i = 0; i < wb.workSheets.get(0).wavelength.size(); i += 10)
            wl.add(wb.workSheets.get(0).wavelength.get(i));

        for (int i = 0; i < wb.workSheets.size(); i++)
            wb.workSheets.get(i).selected = false;
        wb.workSheets.get(0).selected = true;

        st.setValue(460.0);
        fin.setValue(580.0);
        st.getSelectionModel().selectedIndexProperty().addListener(
                ((observable, oldValue, newValue) -> {
                    st.setValue(wl.get(newValue.intValue()));
                    replase(wl.get(newValue.intValue()), fin.getValue());
                }));

        fin.getSelectionModel().selectedIndexProperty().addListener(
                ((observable, oldValue, newValue) -> {
                    fin.setValue(wl.get(newValue.intValue()));
                    replase(st.getValue(), wl.get(newValue.intValue()));
                }));

    }

    private void replase(Double start, Double finish) {
        if (start > finish) {
            fin.setValue(start);
            st.setValue(finish);
        }
        mainChart.removeAllVerticalValueMarkers();
        XYChart.Data<Number, Number> verticalMarker1 = new XYChart.Data<>(st.getValue(), 0);
        mainChart.addVerticalValueMarker(verticalMarker1);
        XYChart.Data<Number, Number> verticalMarker2 = new XYChart.Data<>(fin.getValue(), 0);
        mainChart.addVerticalValueMarker(verticalMarker2);
    }

    private void makeData() {
        Double A;
        allCurves.clear();
        for (int i = 0; i < wb.workSheets.size(); i++) {
            XYChart.Series seria = new XYChart.Series();
            if (!wb.workSheets.get(i).selected) continue;
            for (int j = 0; j < wb.workSheets.get(i).wavelength.size(); j++) {
                A = wb.workSheets.get(i).absorbance.get(j);
                seria.getData().add(new XYChart.Data(wb.workSheets.get(i).wavelength.get(j), A));
            }
            seria.setName("Curve0");
            allCurves.add(seria);
        }
        mainChart.getData().setAll(allCurves);
    }

    private void addCurve(int i) {
        XYChart.Series seria = new XYChart.Series();
        Double A;
        for (int j = 0; j < wb.workSheets.get(i).wavelength.size(); j++) {
            A = wb.workSheets.get(i).absorbance.get(j);
            seria.getData().add(new XYChart.Data(wb.workSheets.get(i).wavelength.get(j), A));
        }
        seria.setName("Curve"+i);
        mainChart.getData().add(seria);
    }

    private void removeCurve(int i) {

        for (Object seria : mainChart.getData()) {
            XYChart.Series sI=(XYChart.Series)seria;
            if(sI.getName().equals("Curve"+i)) mainChart.getData().remove(seria);
        }


    }

    private void setAxsisDiap(ParseXML WB, int start, int finish) {

        Double maxA = 0d, minA = 0d;

        for (int i = 0; i < WB.workSheets.size(); i++) {
            if (WB.workSheets.get(i).selected) {
                Double maxAi = Collections.max(WB.workSheets.get(i).absorbance.subList(start, finish));
                Double minAi = Collections.min(WB.workSheets.get(i).absorbance.subList(start, finish));
                if (minA > minAi) minA = minAi;
                if (maxA < maxAi) maxA = maxAi;
            }
        }
        Double x1 = WB.workSheets.get(0).wavelength.get(start), x2 = WB.workSheets.get(0).wavelength.get(finish);
        if((x1!=x2)&&(minA - (maxA - minA) / 10!=maxA + 2 * (maxA - minA) / 10))
        scaleXY(x1,x2,minA - (maxA - minA) / 10, maxA + 2 * (maxA - minA) / 10);

        initXLowerBound = ((NumberAxis) mainChart.getXAxis()).getLowerBound();
        initXUpperBound = ((NumberAxis) mainChart.getXAxis()).getUpperBound();
        initYLowerBound = ((NumberAxis) mainChart.getYAxis()).getLowerBound();
        initYUpperBound = ((NumberAxis) mainChart.getYAxis()).getUpperBound();

    }

    private void justFit(int order) {
        long recordIterator = 1;
        mainChart.getData().clear();
        data.clear();
        for (int k = 0; k < wb.workSheets.size(); k++) {
            if (!wb.workSheets.get(k).selected) continue;
            int i0 = wb.workSheets.get(k).wavelength.indexOf(st.getValue());
            int il = wb.workSheets.get(k).wavelength.indexOf(fin.getValue());
            int len = il - i0;
            Double[] x = new Double[len];
            for (int j = 0; j < len; j++) x[j] = wb.workSheets.get(k).wavelength.get(i0 + j);
            Double[] y = new Double[len];
            for (int j = 0; j < len; j++) y[j] = wb.workSheets.get(k).absorbance.get(i0 + j);

            final WeightedObservedPoints obs = new WeightedObservedPoints();
            for (int i = 0; i < len; i++) obs.add(x[i], y[i]);
            final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(order);
            final double[] coefPoly = fitter.fit(obs.toList());
            PolynomialFunction polyFunc = new PolynomialFunction(coefPoly);
            XYChart.Series seria = new XYChart.Series();
            XYChart.Series seria2 = new XYChart.Series();

            for (int i = 0; i < x.length; i++) {
                seria.getData().add(new XYChart.Data(x[i], y[i]));
                seria2.getData().add(new XYChart.Data(x[i], polyFunc.value(x[i])));
            }
            mainChart.getData().addAll(seria, seria2);

            double[] coefs = polyFunc.getCoefficients();
            CurveMaxRecord newLine = new CurveMaxRecord(recordIterator++, wb.workSheets.get(k).title,
                    new BigDecimal(solverPoly(coefs)).setScale(2, RoundingMode.HALF_DOWN).doubleValue(),
                    new BigDecimal(polyFunc.value(solverPoly(coefs))).setScale(4, RoundingMode.HALF_DOWN).doubleValue()
            );
            data.add(newLine);

        }
        setAxsisDiap(wb, wb.workSheets.get(0).wavelength.indexOf(st.getValue()), wb.workSheets.get(0).wavelength.indexOf(fin.getValue()));
        showTable();
        chartBox.getStylesheets().setAll("series-line.css");
        styleLineSeries();

    }

    private double solverPoly(double[] coefs) {
        Double maxX = 0d;
        if (coefs.length == 4) {
            Double c = coefs[1], b = coefs[2], a = coefs[3];
            Double x1 = (-b - Math.sqrt(b * b - 3 * a * c)) / (3 * a);
            Double x2 = (-b + Math.sqrt(b * b - 3 * a * c)) / (3 * a);
            if (2 * b + 6 * a * x1 < 0) maxX = x1;
            else maxX = x2;
        }
        if (coefs.length == 3) {
            Double b = coefs[1], a = coefs[2];
            maxX = -(b / (2 * a));
        }
        return maxX;
    }

    private EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent mouseEvent) {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    rect.setX(mouseEvent.getX());
                    rect.setY(mouseEvent.getY());
                    rectinitX.set(mouseEvent.getX());
                    rectinitY.set(mouseEvent.getY());
                } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                    rectX.set(mouseEvent.getX());
                    rectY.set(mouseEvent.getY());
                } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {

                    if ((rectinitX.get() >= rectX.get()) && (rectinitY.get() >= rectY.get())) {
                        scaleXY(initXLowerBound,initXUpperBound,initYLowerBound,initYUpperBound);

                    } else {
                        double Tgap;
                        double newLowerBound, newUpperBound, xAxisShift,yAxisShift;
                        double yNewLowerBound, yNewUpperBound;

                        LineChart<Number, Number> lineChart = (LineChart<Number, Number>) chartBox.getCenter();

                        NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
                        Tgap = yAxis.getHeight() / (yAxis.getUpperBound() - yAxis.getLowerBound());
                        yAxisShift = getSceneShiftY(yAxis);
                        yNewUpperBound = yAxis.getUpperBound() - ((rectinitY.get() - yAxisShift) / Tgap);
                        yNewLowerBound = yAxis.getUpperBound() - ((rectY.get() - yAxisShift) / Tgap);

                        if (yNewUpperBound > yAxis.getUpperBound())yNewUpperBound = yAxis.getUpperBound();

                        NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
                        Tgap = xAxis.getWidth() / (xAxis.getUpperBound() - xAxis.getLowerBound());
                        xAxisShift = getSceneShiftX(xAxis);
                        newLowerBound = ((rectinitX.get() - xAxisShift) / Tgap) + xAxis.getLowerBound();
                        newUpperBound = ((rectX.get() - xAxisShift) / Tgap) + xAxis.getLowerBound();

                        if (newUpperBound > xAxis.getUpperBound())newUpperBound = xAxis.getUpperBound();

                        scaleXY(newLowerBound, newUpperBound,yNewLowerBound, yNewUpperBound);
                    }
                    rectX.set(0);
                    rectY.set(0);
                }
            }

        }
    };
    private static double getSceneShiftX(Node node) {
        double shift = 0;
        do {
            shift += node.getLayoutX();
            node = node.getParent();
        } while (node != null);
        return shift;
    }
    private static double getSceneShiftY(Node node) {
        double shift = 0;
        do {
            shift += node.getLayoutY();
            node = node.getParent();
        } while (node != null);
        return shift;
    }
    private void scaleXY(double xMin,double xMax,double yMin,double yMax){
        AxisAutoScale newYscale = new AxisAutoScale(yMin, yMax);
        yAxis.setLowerBound(newYscale.min);
        yAxis.setUpperBound(newYscale.max);
        yAxis.setTickUnit(newYscale.tick);
        AxisAutoScale newXscale = new AxisAutoScale(xMin, xMax);
        xAxis.setLowerBound(newXscale.min);
        xAxis.setUpperBound(newXscale.max);
        xAxis.setTickUnit(newXscale.tick);
    }

    @SuppressWarnings({"StringBufferReplaceableByString", "StringConcatenationInsideStringBufferAppend"})
    private void styleSeries() {
        mainChart.applyCss();
        int nSeries = 0;
        for (int k = 0; k < allCurves.size(); k++) {

            Set<Node> nodes = mainChart.lookupAll(".series" + nSeries);
            for (Node n : nodes) {
                StringBuffer style = new StringBuffer();
                style.append("-fx-stroke: transparent; -fx-background-color: " + colors[k] + ", white; ");
                n.setStyle(style.toString());
            }
            nSeries++;
        }
    }
    private void styleLineSeries() {
        mainChart.applyCss();
        int nSeries = 0;
        for (int k = 0; k < allCurves.size(); k+=2) {

            Set<Node> nodes = mainChart.lookupAll(".chart-series-line" + nSeries);
            for (Node n : nodes) {
                StringBuffer style = new StringBuffer();
                style.append("-fx-stroke: + colors[k]");
                n.setStyle(style.toString());
            }
            nSeries++;
        }
        for (int k = 1; k < allCurves.size(); k+=2) {

            Set<Node> nodes = mainChart.lookupAll(".chart-line-symbol" + nSeries);
            for (Node n : nodes) {
                StringBuffer style = new StringBuffer();
                style.append("-fx-stroke: transparent; -fx-background-color: " + colors[k] + ", white; ");
                n.setStyle(style.toString());
            }
            nSeries++;
        }
    }


    private void setRowFactory() {
        table.setRowFactory(p -> {
            final TableRow<CurveMaxRecord> row = new TableRow<>();
            row.setOnDragEntered(t -> table.getSelectionModel().select(row.getIndex()));

            row.setOnDragDetected(t -> {
                Dragboard db = row.getTableView().startDragAndDrop(TransferMode.COPY);
                ClipboardContent content = new ClipboardContent();
                content.put(dataFormat, "hXData");
                db.setContent(content);
                t.consume();
            });
            return row;
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
