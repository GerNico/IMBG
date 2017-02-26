package spr;

import javafx.scene.chart.XYChart;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

@SuppressWarnings("unchecked")
class SPR {


        ArrayList<ArrayList<Double>> BigList= new ArrayList<>() ;
        Double[] timeMas;
        boolean isTemperature=false;
        boolean isCorrection=false;

        SPR(String paths) throws IOException {
            Path pa = Paths.get(paths);
            Scanner in =new Scanner(pa);
            in.nextLine();//перша строка не інформативнa
            in.nextLine();//строка коментар
            in.nextLine();//строка коментар
            String s;
            while (in.hasNext()){
                s=in.nextLine();s=s.trim();
                s=s.replaceAll("[\\s]{2,}", " ");//Видалити повтор пробілів
                String[] ars=s.split("\\s");//Поділити по пробілу
                ArrayList<Double> dMas= new ArrayList<>();
                dMas.add(Double.parseDouble(ars[0])/60d);//Поділити на 60 секунд
                dMas.add(Double.parseDouble(ars[1])/360d);//Поділити на 360 градусів основний канал
                dMas.add(Double.parseDouble(ars[2])/360d);//Поділити на 360 градусів другий канал
                if(ars.length>5)dMas.add(Double.parseDouble(ars[5]));// Дописуємо колонку температури
                this.BigList.add(dMas);
            }
            this.isTemperature = this.BigList.get(1).size() == 4;
            timeMas=new Double[this.BigList.size()];
            for(int i=0;i<this.BigList.size();i++)timeMas[i]=BigList.get(i).get(0);
        }

    Double[][] plotborders(int slider, int cb, int canal) {
        ArrayList<Double> masX= new ArrayList<>();
        ArrayList<Double> masY= new ArrayList<>();

        for (int i = slider; (i < slider + cb)&&(i<this.BigList.size()); i++) {
            masX.add(this.BigList.get(i).get(0));
            masY.add(this.BigList.get(i).get(canal));
        }
        double xMin=Collections.min(masX);
        double xMax=Collections.max(masX);
        AxisAutoScale xAxis=new AxisAutoScale(xMin-0.05*Math.abs(xMax-xMin),xMax+0.05*Math.abs(xMax-xMin));
        double yMin=Collections.min(masY);
        double yMax=Collections.max(masY);
        AxisAutoScale yAxis=new AxisAutoScale(yMin-0.05*Math.abs(yMax-yMin),yMax+0.05*Math.abs(yMax-yMin));

            return new Double[][]{{xAxis.min,xAxis.max},{yAxis.min,yAxis.max},{xAxis.tick,yAxis.tick}};
    }

    XYChart.Series makeData(String curvename, int canal){
        XYChart.Series series = new XYChart.Series();
        series.setName(curvename);
        for (ArrayList<Double> aFullList : this.BigList)
            series.getData().add(new XYChart.Data(aFullList.get(0), aFullList.get(canal)));
        return series;}
    XYChart.Series takeData(String curvename,int s,int cb,int canal){
        XYChart.Series series = new XYChart.Series();
        series.setName(curvename);
        for (int i = s;((i<s+cb)&&(i<this.BigList.size())); i++)
            series.getData().add(new XYChart.Data(this.BigList.get(i).get(0),this.BigList.get(i).get(canal)));
        return series;
    }


}

