package java.spr;

import javafx.scene.chart.XYChart;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

class SPR {


        ArrayList<ArrayList<Double>> BigList= new ArrayList<>() ;
        Double[] timeMas;
        boolean isTemperature=false;

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

    Double[][] getborders(int s, int cb, int canal) {
        ArrayList<Double> masX= new ArrayList<>();ArrayList<Double> masY= new ArrayList<>();
        for (int i = s; (i < s + cb)&&(i<this.BigList.size()); i++) {
            masX.add(this.BigList.get(i).get(0));
            masY.add(this.BigList.get(i).get(canal));
        }
        Collections.sort(masX);Collections.sort(masY);
        double diap = masY.get(masY.size() - 1) - masY.get(0);
        double ymax = new BigDecimal(masY.get(masY.size() - 1)).setScale(5, RoundingMode.DOWN).doubleValue();
        int odr = 5;
        while (masY.get(masY.size() - 1) - ymax < diap / 400) {
            ymax = new BigDecimal(masY.get(masY.size() - 1)).setScale(odr, RoundingMode.DOWN).doubleValue();
            odr--;
        }odr++;//Визначити вдалий порядок округлення
        double minX = new BigDecimal(masX.get(0)).setScale(0, RoundingMode.DOWN).doubleValue();
        double maxX = new BigDecimal(masX.get(masX.size() - 1)).setScale(0, RoundingMode.UP).doubleValue();
        double minY = new BigDecimal(masY.get(0)-0.1*(masY.get(masY.size() - 1)-masY.get(0))).setScale(odr, RoundingMode.DOWN).doubleValue();
        double maxY = new BigDecimal(masY.get(masY.size() - 1)+0.1*(masY.get(masY.size() - 1)-masY.get(0))).setScale(odr, RoundingMode.UP).doubleValue();
        double dY = new BigDecimal(diap / 9).setScale(odr, RoundingMode.UP).doubleValue();

        return new Double[][]{{minX, maxX}, {minY, maxY}, {dY}};
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

