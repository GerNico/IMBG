package spr;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
public class Main extends Application {
    private SPR read;
    private Scene scOpenFile,scMain;
    private ChoiceBox<Integer> cb,cb2,cb3,selectCH;
    private final NumberAxis xAxis=new NumberAxis();
    private final NumberAxis yAxis=new NumberAxis();
    private scrollBar scroll1,scroll2,scroll3,scroll4,scroll5;
    private final int initS1=0,initCB=160;
    private LineChart onlyChart;
    private HBox setings,mainS,supS,delScrollBar,D1box,D2box,labelBox1,labelBox2,fitBox,addBox;
    private BorderPane chartBox;
    private Label labelCB,labelFit,labelCH,labelAnalitic1,labelAnalitic2,labelforFit,labelcb3;
    private Stage currentStage;
    private VBox selectRange;
    private Button deleteButton;
    private Button saveButton;
    private Button fitButton;
    private final int StageHight1=160,StageHight2=500;
    private TextField bookName;
    private ObservableList<String> xlsbooks;
    private ExcelUsage toExcel;
    private Path path;
    private Rectangle rect;
    private double initXLowerBound = 0, initXUpperBound = 0, initYLowerBound = 0, initYUpperBound = 0;
    private SimpleDoubleProperty rectinitX = new SimpleDoubleProperty();
    private SimpleDoubleProperty rectinitY = new SimpleDoubleProperty();
    private SimpleDoubleProperty rectX = new SimpleDoubleProperty();
    private SimpleDoubleProperty rectY = new SimpleDoubleProperty();


    @Override
    public void start(Stage stage) throws Exception {
        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Режим");

        RadioMenuItem fileS = new RadioMenuItem("Вибір файлу");
        RadioMenuItem rangeS = new RadioMenuItem("Вибір діапазону");
        RadioMenuItem delPoint = new RadioMenuItem("Виколоти точку");
        RadioMenuItem fitS = new RadioMenuItem("Область наближення");
        RadioMenuItem analS = new RadioMenuItem("Аналітика");

        ToggleGroup group = new ToggleGroup();
        fileS.setToggleGroup(group);                fileS.setOnAction(event -> {selectMode(4);});
        rangeS.setToggleGroup(group);               rangeS.setOnAction(event -> {selectMode(0);});
        delPoint.setToggleGroup(group);             delPoint.setOnAction(event -> {selectMode(3);});
        fitS.setToggleGroup(group);                 fitS.setOnAction(event -> {selectMode(1);});
        analS.setToggleGroup(group);                analS.setOnAction(event -> {selectMode(2);});
        rangeS.setSelected(true);

        menu.getItems().addAll(rangeS,fitS,analS,delPoint,fileS);
        menuBar.getMenus().addAll(menu);

        currentStage=stage;
        xAxis.setLabel("time, min");
        xAxis.setAutoRanging(false);
        yAxis.setLabel("angle,deg");
        yAxis.setAutoRanging(false);
        onlyChart = new LineChart<>(xAxis, yAxis);
        onlyChart.setAnimated(false);

        //-----------------------------------         Selection of the file
        currentStage.setTitle("Select the file");
        Label lb1 = new Label();            lb1.setText("sp1/sp2: ");
        Label selectedFile = new Label();   selectedFile.setText("");
        Button openButton = new Button("Open spr");         openButton.setMinWidth(100);

        final FileChooser fileOpener = new FileChooser();
        fileOpener.setTitle("Open the file");
        fileOpener.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("SPR file","*.sp2","*.sp1"));

        openButton.setOnAction(
                e -> {
                    //      selection of the file
                    File file = fileOpener.showOpenDialog(currentStage);
                    if (file != null) {
                        try {
                            read =new SPR(file.getPath());
                        } catch (IOException e1) {e1.printStackTrace();}
                        selectedFile.setText(file.getPath());
                        scroll1.s.setMin(0);
                        scroll1.s.setMax(read.timeMas.length-1-initCB);
                        onlyChart.getData().setAll(makeData("working"));
                        setAxis(read,initS1,initCB);
                        currentStage.setScene(scMain);
                        currentStage.setHeight(StageHight2);
                        rangeS.setSelected(true);selectMode(0);
                        toExcel=new ExcelUsage(read);
                    }
                });
        HBox openBox = new HBox(openButton, lb1, selectedFile);
        openBox.setSpacing(10); openBox.setAlignment(Pos.CENTER_LEFT);
        VBox openFile = new VBox(12,openBox);
        openFile.setPadding(new Insets(10, 10, 10, 10));
        scOpenFile=new Scene(openFile);
        currentStage.setScene(scOpenFile);
        currentStage.setHeight(StageHight1);
        currentStage.show();
        //---------------------------------------       Select main range
        //       Вибір діапазону
        labelCB = new Label();           labelCB.setText("діапазон, точки");
        ArrayList<Integer> xDiap = new ArrayList<>();//діапазон часу на діаграмі
        for (Integer i = 25; i <= 5000; i+=25) xDiap.add(i);
        cb = new ChoiceBox<>(FXCollections.observableArrayList(xDiap));cb.setValue(200);
        cb.getSelectionModel().selectedIndexProperty().addListener(
                ((observable, oldValue, newValue) -> {
                    int cbv=newValue.intValue();
                    scroll1.s.setMin(0);
                    scroll1.s.setMax(read.timeMas.length-1-xDiap.get(cbv));
                    setAxis(read,(int)scroll1.s.getValue(),xDiap.get(cbv));
                }));
        //       Вибір каналу
        labelCH = new Label();            labelCH.setText("канал ");
        ArrayList<Integer> channels = new ArrayList<>();
        channels.add(1);        channels.add(2);
        selectCH = new ChoiceBox<>(FXCollections.observableArrayList(channels));
        selectCH.setValue(1);
        selectCH.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    onlyChart.getData().setAll(makeData("working"));
                    setAxis(read,(int)scroll1.s.getValue(),cb.getValue());
                });

        //       створити основний слайдер
        scroll1=new scrollBar(0,"0","діапазон");
        scroll1.s.valueProperty().addListener((observable, oldvalue, newvalue) ->
        {
            int i = newvalue.intValue();
            scroll1.text.setText(Round(read.timeMas[i]));
            setAxis(read,i,cb.getValue());

        });
        //      Select fitrange
//      вибір діапазону наближення
        labelFit = new Label();           labelFit.setText("фіт, точки");
        ArrayList<Integer> diap = new ArrayList<>();//діапазон часу на діаграмі
        for (Integer i = 5; i <= 1000; i+=5) diap.add(i);
        cb2=new ChoiceBox<>(FXCollections.observableArrayList(diap));
        cb2.setValue(20);
        cb2.getSelectionModel().selectedIndexProperty().addListener(
                ((observable, oldValue, newValue) -> {
                    int it=newValue.intValue();
                    scroll2.s.setMin(scroll1.s.getValue());
                    scroll2.s.setMax(scroll1.s.getValue()+cb.getValue()-1);
                    setAxis(read,(int)scroll1.s.getValue(),cb.getValue());
                    selectSLogic((int)scroll2.s.getValue(),diap.get(it));
                }));
//       Вибрати діапазон аналізу
        labelcb3 = new Label();           labelcb3.setText("ділянка, точки");
        ArrayList<Integer> diapAnal = new ArrayList<>();
        for (Integer i = 5; i <= 100; i++) diapAnal.add(i);
        cb3=new ChoiceBox<>(FXCollections.observableArrayList(diapAnal));
        cb3.setValue(20);
        cb3.getSelectionModel().selectedIndexProperty().addListener(
                ((observable, oldValue, newValue) -> {
                    int it=newValue.intValue();
                    scroll2.s.setMin(scroll1.s.getValue());
                    scroll2.s.setMax(scroll1.s.getValue()+cb.getValue()-1);
                    setAxis(read,(int)scroll1.s.getValue(),cb.getValue());
                    analiticSLogic((int)scroll4.s.getValue(),(int)scroll5.s.getValue(),it);
                }));

//      слайдер уточнення
        scroll2=new scrollBar((int)scroll1.s.getValue(),scroll1.text.getText(),"наближення");
        scroll2.s.valueProperty().addListener((observable, oldvalue, newvalue) ->
        {
            int i2 = newvalue.intValue();
            selectSLogic(i2,cb2.getValue());
        });
//      слайдер видалення
        scroll3=new scrollBar((int)scroll1.s.getValue(),scroll1.text.getText(),"виколоти");
        scroll3.s.valueProperty().addListener((observable, oldvalue, newvalue) ->
        {
            int i3 = newvalue.intValue();
            delSLogic(i3);
        });
        delScrollBar=scroll3.asHbox();
        //      перший слайдер вибору
        scroll4=new scrollBar((int)scroll1.s.getValue(),scroll1.text.getText(),"початок");
        scroll4.s.valueProperty().addListener((observable, oldvalue, newvalue) ->
        {
            int i4 = newvalue.intValue();
            analiticSLogic(i4,(int)scroll5.s.getValue(),cb3.getValue()-1);
        });
        D1box=scroll4.asHbox();
        //      другий слайдер вибору
        scroll5=new scrollBar((int)scroll1.s.getValue(),scroll1.text.getText()," кінець ");
        scroll5.s.valueProperty().addListener((observable, oldvalue, newvalue) ->
        {
            int i5 = newvalue.intValue();
            analiticSLogic((int)scroll4.s.getValue(),i5,cb3.getValue()-1);
        });
        D2box=scroll5.asHbox();

        deleteButton = new Button("Видалити");         deleteButton.setPrefWidth(100);
        deleteButton.setOnAction(event -> {
            scroll1.s.setMax((int)scroll1.s.getMax()-1);
            scroll2.s.setMax(scroll1.s.getValue()+cb.getValue()-2);
            scroll3.s.setMax(scroll1.s.getValue()+cb.getValue()-2);
            read.BigList.remove((int)scroll3.s.getValue());
            onlyChart.getData().setAll(
                    read.takeData("working",(int)scroll1.s.getValue(),cb.getValue(),selectCH.getValue()),
                    read.takeData("delete",(int)scroll3.s.getValue(),1,selectCH.getValue()));
        });

        labelforFit = new Label();           labelforFit.setText("Наближення: ");
        fitBox=new HBox(labelforFit);
        fitButton = new Button("Наблизити");         fitButton.setPrefWidth(100);
        fitButton.setOnAction(event -> fitdata((int)scroll2.s.getValue(),cb2.getValue()));
//      елементи аналітики
        labelAnalitic1 = new Label();           labelAnalitic1.setText("");
        labelAnalitic2 = new Label();           labelAnalitic2.setText("");

        bookName=new TextField("");bookName.setMinWidth(300);

        Button addButton = new Button("Внести");
        addButton.setPrefWidth(100);
        addButton.setOnAction(event -> {
            xlsbooks.add(bookName.getText());
            try {
                toExcel.fragToXLS(bookName.getText(),(int)scroll1.s.getValue(),cb.getValue(),
                        (int)scroll4.s.getValue(),(int)scroll5.s.getValue(),cb3.getValue(),selectCH.getValue()  );
            } catch (IOException e) {
                show(e.getMessage(),"error");
            }
        });

        saveButton = new Button("Зберегти");         saveButton.setPrefWidth(100);
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save your file");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Excel","*.xlsx"));

        saveButton.setOnAction(event -> {
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {toExcel.allToExcell(file.getPath());
                } catch (IOException e1) {
                    show(e1.getMessage(),"error");}
            }
        });

        xlsbooks=FXCollections.observableArrayList("Всі");
        ChoiceBox<String> XLSbooks = new ChoiceBox<>(xlsbooks);
        XLSbooks.setValue("Всі");

        onlyChart.setLegendVisible(false);
        supS = scroll2.asHbox();
        mainS =scroll1.asHbox();
        setings = new HBox(labelCH,selectCH,labelCB,cb);
        addBox=new HBox(10,bookName, addButton, XLSbooks);
        setings.setSpacing(10);setings.setAlignment(Pos.CENTER_LEFT);
        labelBox1= new HBox(labelAnalitic1);   labelBox1.setSpacing(10);    labelBox1.setAlignment(Pos.CENTER_LEFT);
        labelBox2= new HBox(labelAnalitic2);   labelBox2.setSpacing(10);    labelBox2.setAlignment(Pos.CENTER_LEFT);

        chartBox = new BorderPane(onlyChart);                              // chartBox.setCenter(onlyChart);
        chartBox.setTop(menuBar);

        selectRange = new VBox(5,chartBox,setings,mainS);
        selectRange.setPadding(new Insets(10, 10, 10, 10));
        selectRange.getStylesheets().setAll("style.css");
        scMain=new Scene(selectRange);
        currentStage.setMinWidth(540);        currentStage.setMinHeight(120);
        currentStage.setResizable(false);
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~кволі намагання зробити зум

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
//=========================================   методи і класи     =======================================================
    private Series makeData(String curvename){
        Series series = new Series();
        series.setName(curvename);
        return read.makeData(curvename,selectCH.getValue());
    }
    private void setAxis(SPR plot,int s,int cb){
            Double[][] borders=plot.plotborders(s,cb,selectCH.getValue());
            xAxis.setLowerBound(borders[0][0]);
            xAxis.setUpperBound(borders[0][1]);
            xAxis.setTickUnit(borders[2][0]);
            yAxis.setLowerBound(borders[1][0]);
            yAxis.setUpperBound(borders[1][1]);
            yAxis.setTickUnit(borders[2][1]);
            initXLowerBound = ((NumberAxis) onlyChart.getXAxis()).getLowerBound();
            initXUpperBound = ((NumberAxis) onlyChart.getXAxis()).getUpperBound();
            initYLowerBound = ((NumberAxis) onlyChart.getYAxis()).getLowerBound();
            initYUpperBound = ((NumberAxis) onlyChart.getYAxis()).getUpperBound();
        }
    private void delSLogic(int i){
        scroll3.text.setText(Round(read.timeMas[i]));
        onlyChart.getData().setAll(
                read.takeData("working",(int)scroll1.s.getValue(),cb.getValue(),selectCH.getValue()),
                read.takeData("delete data",i,1,selectCH.getValue()));
    }
    private void selectSLogic(int i,int cb2){
        scroll2.text.setText(Round(read.timeMas[i]));
        onlyChart.getData().setAll(
                read.takeData("working",(int)scroll1.s.getValue(),cb.getValue(),selectCH.getValue()),
                read.takeData("fit data",i,cb2,selectCH.getValue()));
    }
    void fitdata(int s, int cb2){
        int length;if(s+cb2<read.BigList.size())length=cb2;        else length=read.BigList.size()-s;
        double[] t=new double[length];double[] fi=new double[length];
        for (int i=0; i<length;i++){
            t[i]= read.BigList.get(i+s).get(0);
            fi[i]=read.BigList.get(i+s).get(selectCH.getValue());}
        SmartExpFit fit =new SmartExpFit(t,fi);
        if(fit.incrExp)
        labelforFit.setText("A="+RoundN(fit.y0_A_k[1]*1000,2)+" mdeg; k="+RoundN(fit.y0_A_k[2]/60.d*1000,2)
                +"mdeg/s"+"   φ="+RoundN(fit.y0_A_k[0],5)+"+"+RoundN(fit.y0_A_k[1],5)
                +"*(1-Exp[-"+RoundN(fit.y0_A_k[1],3)+"(t-"+RoundN(t[0],1)+")])");
        else
            labelforFit.setText("A="+RoundN(fit.y0_A_k[1]*1000,2)+" mdeg; k="+RoundN(fit.y0_A_k[2]/60.d*1000,2)
                    +"mdeg/s"+"   φ="+RoundN(fit.y0_A_k[0],5)+"-"+RoundN(fit.y0_A_k[1],5)
                    +"*Exp[-"+RoundN(fit.y0_A_k[1],3)+"(t-"+RoundN(t[0],1)+")]");
        XYChart.Series series = new XYChart.Series();
        series.setName("fitted");
        if(fit.incrExp)
            for (int i = 0;i<t.length; i++)
            series.getData().add(new XYChart.Data(t[i]
                    ,fit.y0_A_k[0]+fit.y0_A_k[1]*(1-Math.exp(-fit.y0_A_k[2]*(t[i]-t[0])))));
        else
            for (int i = 0;i<t.length; i++)
                series.getData().add(new XYChart.Data(t[i]
                        ,fit.y0_A_k[0]+fit.y0_A_k[1]*Math.exp(-fit.y0_A_k[2]*(t[i]-t[0]))));

        onlyChart.getData().setAll(
                read.takeData("working",(int)scroll1.s.getValue(),cb.getValue(),selectCH.getValue()),
                read.takeData("fit data",(int)scroll2.s.getValue(),cb2,selectCH.getValue()),
                series);
    }
    private void analiticSLogic(int S4i,int S5i,int cb3){
        scroll4.text.setText(Round(read.timeMas[S4i]));
        scroll5.text.setText(Round(read.timeMas[S5i]));
        onlyChart.getData().setAll(
                read.takeData("working",(int)scroll1.s.getValue(),cb.getValue(),selectCH.getValue()),
                read.takeData("D1",S4i,cb3,selectCH.getValue()),
                read.takeData("D2",S5i,cb3,selectCH.getValue()));

        SimpleRegression regression1 = new SimpleRegression();
        SimpleRegression regression2 = new SimpleRegression();
        DescriptiveStatistics stats1 = new DescriptiveStatistics();
        DescriptiveStatistics stats2 = new DescriptiveStatistics();
        int canal =selectCH.getValue();
        double t0 = read.BigList.get(S4i + cb3).get(0);
        double tf = read.BigList.get(S5i).get(0);
        for(int i=S4i;i<S4i+cb3;i++)
        {regression1.addData(read.BigList.get(i).get(0),read.BigList.get(i).get(canal));
            stats1.addValue(read.BigList.get(i).get(canal));}
        for(int i=S5i;i<S5i+cb3;i++)
        {regression2.addData(read.BigList.get(i).get(0),read.BigList.get(i).get(canal));
        stats2.addValue(read.BigList.get(i).get(canal));}
        double mean1 = stats1.getMean();
        double mean2 = stats2.getMean();
        double std1 = stats1.getStandardDeviation() * 1000;
        double std2 = stats2.getStandardDeviation() * 1000;
        double drift1 = regression1.getSlope() * 1000;
        double drift2 = regression2.getSlope() * 1000;

        labelAnalitic1.setText("φ1="+RoundN(mean1,5)+" deg; dφ1="+RoundN(std1,2)+
                " mkdeg/min;  DF1="+RoundN(drift1,2)+" mdeg/min; t0="+RoundN(t0,1)
                +" Signal="+RoundN((mean2 - mean1)*1000,1)+"mdeg");
        labelAnalitic2.setText("φ2="+RoundN(mean2,5)+" deg; dφ2="+RoundN(std2,2)+
                " mdeg/min;  DF2="+RoundN(drift2,2)+" mdeg/min; tf="+RoundN(tf,1));
        }
    private void selectMode(int val){
        if(val==0) {
            setings.getChildren().setAll(labelCH,selectCH,labelCB,cb);
            selectRange.getChildren().setAll(chartBox,setings,mainS);
            onlyChart.getData().setAll(
                    read.makeData("working",selectCH.getValue()));
            currentStage.setScene(scMain);
            currentStage.setHeight(StageHight2);
        }
        if(val==1) {
            setings.getChildren().setAll(labelCH,selectCH,labelFit,cb2,fitButton);
            selectRange.getChildren().setAll(chartBox,setings,fitBox,supS);
            scroll2.s.setMin(scroll1.s.getValue());
            scroll2.s.setMax(scroll1.s.getValue()+cb.getValue()-1);
            setAxis(read,(int)scroll1.s.getValue(),cb.getValue());
            selectSLogic((int)scroll1.s.getValue(),cb2.getValue());
            currentStage.setHeight(StageHight2);
            currentStage.setScene(scMain);
        }
        if(val==2) {
            setings.getChildren().setAll(labelcb3,cb3,saveButton);
            selectRange.getChildren().setAll(chartBox,setings,labelBox1,labelBox2,D1box,D2box,addBox);
            scroll4.s.setMin(scroll1.s.getValue());
            scroll4.s.setMax(scroll1.s.getValue()+cb.getValue()-1-cb3.getValue());
            scroll5.s.setMin(scroll1.s.getValue());
            scroll5.s.setMax(scroll1.s.getValue()+cb.getValue()-1-cb3.getValue());
            setAxis(read,(int)scroll1.s.getValue(),cb.getValue());
            analiticSLogic((int)scroll4.s.getValue(),(int)scroll5.s.getValue(),cb3.getValue()-1);
            bookName.setText(selectCH.getValue()+"from"+
                    RoundN(read.timeMas[(int)scroll1.s.getValue()],1)
                    +"to"+RoundN(read.timeMas[(int)(scroll1.s.getValue()+cb.getValue())],1));
            currentStage.setHeight(StageHight2+40);
            currentStage.setScene(scMain);
        }
        if(val==3) {
            setings.getChildren().setAll(deleteButton);
            selectRange.getChildren().setAll(chartBox,setings,delScrollBar);
            scroll3.s.setMin(scroll1.s.getValue());
            scroll3.s.setMax(scroll1.s.getValue()+cb.getValue()-1);
            setAxis(read,(int)scroll1.s.getValue(),cb.getValue());
            delSLogic((int)scroll1.s.getValue());
            currentStage.setHeight(StageHight2);
            currentStage.setScene(scMain);
        }
        if(val==4)  {currentStage.setScene(scOpenFile);currentStage.setHeight(StageHight1);}
    }
    private String Round(double doUble){
       return Double.toString(new BigDecimal(doUble).setScale(1, RoundingMode.UP).doubleValue());
    }
    private double RoundN(double doUble,int n){
        return new BigDecimal(doUble).setScale(n, RoundingMode.HALF_DOWN).doubleValue();
    }
    private class scrollBar {
        Text text,text0;
        ScrollBar s;
    private scrollBar(int value,String s_text,String s_text0) {

        this.text = new Text(s_text);
        this.text0 = new Text(s_text0);
        this.text0.setFont(new Font("monospaced", 14));
        this.text0.setTextAlignment(TextAlignment.LEFT);
        this.text.setFont(new Font("monospaced", 14));
        this.text.setTextAlignment(TextAlignment.RIGHT);
        s = new ScrollBar();
        s.setOrientation(Orientation.HORIZONTAL);
        s.setPrefWidth(400);
        s.setValue(value);
    }
        HBox asHbox() {
            HBox line = new HBox(2,this.text0 ,this.s,this.text);
            line.setPadding(new Insets(2));
            line.setAlignment(Pos.CENTER_LEFT);
            return line;
        }
    }
    public static void show(String message, String title)
    {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
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

    private EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent mouseEvent) {
            if (mouseEvent.getButton() == MouseButton.PRIMARY)
            {
                if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
                    rect.setX(mouseEvent.getX());
                    rect.setY(mouseEvent.getY());
                    rectinitX.set(mouseEvent.getX());
                    rectinitY.set(mouseEvent.getY());
                } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                    rectX.set(mouseEvent.getX());
                    rectY.set(mouseEvent.getY());
                } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {

                    if ((rectinitX.get() >= rectX.get())&&(rectinitY.get() >= rectY.get()))
                    {
                        //Condizioni Iniziali
                        LineChart<Number, Number> lineChart = (LineChart<Number, Number>) chartBox.getCenter();

                        ((NumberAxis) lineChart.getXAxis()).setLowerBound(initXLowerBound);
                        ((NumberAxis) lineChart.getXAxis()).setUpperBound(initXUpperBound);

                        ((NumberAxis) lineChart.getYAxis()).setLowerBound(initYLowerBound);
                        ((NumberAxis) lineChart.getYAxis()).setUpperBound(initYUpperBound);

                        ZoomFreeHand(path, 1.0, 1.0, 0, 0);

                    }
                    else
                    {
                        double Tgap;
                        double newLowerBound, newUpperBound, axisShift;
                        double xScaleFactor, yScaleFactor;
                        double xNewLowerBound, xNewUpperBound;
                        double yNewLowerBound, yNewUpperBound;

                        LineChart<Number, Number> lineChart = (LineChart<Number, Number>) chartBox.getCenter();

                        NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
                        Tgap = yAxis.getHeight()/(yAxis.getUpperBound() - yAxis.getLowerBound());
                        axisShift = getSceneShiftY(yAxis);

                        newUpperBound = yAxis.getUpperBound() - ((rectinitY.get() - axisShift) / Tgap);
                        newLowerBound = yAxis.getUpperBound() - (( rectY.get() - axisShift) / Tgap);

                        if (newUpperBound > yAxis.getUpperBound())
                            newUpperBound = yAxis.getUpperBound();

                        yScaleFactor = (initYUpperBound - initYLowerBound)/(newUpperBound - newLowerBound);
                        yNewLowerBound = newLowerBound;
                        yNewUpperBound = newUpperBound;

                        NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();

                        Tgap = xAxis.getWidth()/(xAxis.getUpperBound() - xAxis.getLowerBound());

                        axisShift = getSceneShiftX(xAxis);


                        newLowerBound = ((rectinitX.get() - axisShift) / Tgap) + xAxis.getLowerBound();
                        newUpperBound = ((rectX.get() - axisShift) / Tgap) + xAxis.getLowerBound();

                        if (newUpperBound > xAxis.getUpperBound())
                            newUpperBound = xAxis.getUpperBound();

                        xScaleFactor = (initXUpperBound - initXLowerBound)/(newUpperBound - newLowerBound);
                        xNewLowerBound = newLowerBound;
                        xNewUpperBound = newUpperBound;

                        yAxis.setLowerBound(yNewLowerBound);
                        yAxis.setUpperBound(yNewUpperBound);

                        xAxis.setLowerBound( xNewLowerBound );
                        xAxis.setUpperBound( xNewUpperBound );
                        ZoomFreeHand(path, xScaleFactor, yScaleFactor, xNewLowerBound, yNewUpperBound);
                    }

                    // Hide the rectangle
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
    private void ZoomFreeHand(Path path, double xScaleFactor, double yScaleFactor, double xaxisShift, double yaxisShift) {

        path.setScaleY(yScaleFactor);
        path.setScaleX(xScaleFactor);
        path.setTranslateX(xaxisShift);
        path.setTranslateY(yaxisShift);
    }

//-------------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) {
        launch(args);
    }
}
