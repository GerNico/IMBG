package spr;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.charts.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


class ExcelUsage {

    private boolean istemp;
    private String[] colomns;
    private ArrayList<ArrayList<Double>> table= new ArrayList<>() ;
    private Workbook wb;

    ExcelUsage(SPR spr){
        if(spr.isTemperature)this.colomns= new String[]{"Час, хв", "черв. канал",
                "синій канал","температура, °C"};
        else colomns= new String[]{"Час, хв", "черв. канал", "синій канал"};
        this.table=spr.BigList;
        this.istemp=spr.isTemperature;
        this.wb=new XSSFWorkbook();
        Sheet sh1 = this.wb.createSheet(WorkbookUtil.createSafeSheetName("Все"));
        Row firstRow = sh1.createRow(0);
        for (int i=0;i<colomns.length;i++) firstRow.createCell(i).setCellValue(colomns[i]);
        int j = 1;
        for (ArrayList<Double> mas : this.table) {
            Row thisRow = sh1.createRow(j);
            for (int i = 0; i < mas.size(); i++) thisRow.createCell(i).setCellValue(mas.get(i));
            j++;
        }
            for(int i=0;i<colomns.length;i++)sh1.autoSizeColumn(i);
                /*                                            Plot                                              */

        Drawing drawing = sh1.createDrawingPatriarch();
        ClientAnchor anchor = drawing.createAnchor(0,0,0,0,4,0,18,20);

        Chart chart1 = drawing.createChart(anchor);
        ChartLegend legend = chart1.getOrCreateLegend();
        legend.setPosition(LegendPosition.BOTTOM);

        ScatterChartData data = chart1.getChartDataFactory().createScatterChartData();

        ArrayList<Double> mas1 = new ArrayList<>();
        for(int i=0;i<table.size();i++) mas1.add(table.get(i).get(1));
        ArrayList<Double> mas2 = new ArrayList<>();
        for(int i=0;i<table.size();i++) mas2.add(table.get(i).get(2));

        ValueAxis bottomAxis = chart1.getChartAxisFactory().createValueAxis(AxisPosition.BOTTOM);
        AxisAutoScale xRes= new AxisAutoScale(table.get(0).get(0),table.get(table.size()-1).get(0));
        bottomAxis.setMinimum(xRes.min);
        bottomAxis.setMaximum(xRes.max);
        ValueAxis leftAxis = chart1.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);

        double min=Math.min(Collections.min(mas1),Collections.min(mas2));
        double max=Math.max(Collections.max(mas1),Collections.max(mas2));
        AxisAutoScale yRes= new AxisAutoScale(min,max);
        leftAxis.setMinimum(yRes.min);
        leftAxis.setMaximum(yRes.max);
        leftAxis.setCrosses(AxisCrosses.MIN);

        ChartDataSource<Number> xs = DataSources.fromNumericCellRange(sh1, new CellRangeAddress(1,table.size(), 0, 0));
        ChartDataSource<Number> ys = DataSources.fromNumericCellRange(sh1, new CellRangeAddress(1,table.size(),1,1));
        ChartDataSource<Number> ys2 = DataSources.fromNumericCellRange(sh1, new CellRangeAddress(1,table.size(),2,2));

        data.addSerie(xs, ys);data.addSerie(xs, ys2);
        chart1.plot(data, bottomAxis, leftAxis);
        chart1.deleteLegend();


        /*                                            End of plot                                         */
    }

    void allToExcell(String path) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(path);
        this.wb.write(outputStream);// write down the data
        outputStream.close();//закрий поток
    }

    void fragToXLS (String sheetName, int s,int cb,int s2,int s3,int cb2,int can,String stringFactor, boolean corection, double k) throws IOException {
        int factor;
        if(stringFactor.equals("deg"))factor=1;
        else if(stringFactor.equals("r.u."))factor=10000;
        else {stringFactor="mdeg"; factor=1000;}

        Sheet sh = this.wb.createSheet(WorkbookUtil.createSafeSheetName(sheetName));
        Row firstRow = sh.createRow(0);
        for (int i=0;i<colomns.length;i++) firstRow.createCell(i).setCellValue(colomns[i]);

        for (int j=1;j<cb+1;j++) {
            Row thisRow = sh.createRow(j);
            for (int i = 0; i < table.get(j-1).size(); i++)
                if(s+j-1<table.size())thisRow.createCell(i).setCellValue(table.get(s+j-1).get(i));
        }

        CellStyle style = this.wb.createCellStyle();
        style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);;
        style.setFillForegroundColor(IndexedColors.GREEN.getIndex());

        CellStyle style2 = this.wb.createCellStyle();
        Font important=this.wb.createFont();
        important.setBold(true);
        style2.setFont(important);
        style2.setBorderRight(BorderStyle.MEDIUM);
        style2.setBorderLeft(BorderStyle.MEDIUM);
        style2.setBorderBottom(BorderStyle.MEDIUM);
        style2.setBorderTop(BorderStyle.MEDIUM);

        int start1,start2,finish1,finish2;
        start1=s2-s+1;  finish1=s2-s+cb2-1;
        start2=s3-s+1;  finish2=s3-s+cb2-1;
        int st1=start1+1,st2=start2+1,fin1=finish1+1,fin2=finish2+1;

        for(int i=start1;i<=finish1;i++)sh.getRow(i).getCell(can).setCellStyle(style);
        for(int i=start2;i<=finish2;i++)sh.getRow(i).getCell(can).setCellStyle(style);

        if(corection==true){
            k=k/1000;// conversion from mdeg/min
            firstRow.createCell(this.colomns.length).setCellValue("корекція");
            double b=sh.getRow(finish1).getCell(can).getNumericCellValue()-k*sh.getRow(finish1).getCell(0).getNumericCellValue();
            for (int j=1;j<cb+1;j++) {
                sh.getRow(j).createCell(this.colomns.length).setCellValue(sh.getRow(j).getCell(can).getNumericCellValue()
                        +sh.getRow(finish1).getCell(can).getNumericCellValue()
                        -k*sh.getRow(j).getCell(0).getNumericCellValue()-b);
            }}

        cellSt(sh,style2,0,6,"Осн.  канал");
        cellSt(sh,style2,1,6,"M,      deg");
        cellSt(sh,style2,1,7,"SD,    "+stringFactor);

        cellSt(sh,style2,2,5,"Початок");
        cellSt(sh,style2,3,5,"Кінець");

        cellIfF(sh,style2,can,2,6,"AVERAGE(B"+st1+":B"+fin1+")","AVERAGE(C"+st1+":C"+fin1+")");
        cellIfF(sh,style2,can,3,6,"AVERAGE(B"+st2+":B"+fin2+")","AVERAGE(C"+st2+":C"+fin2+")");
        cellIfF(sh,style2,can,2,7,"STDEV(B"+st1+":B"+fin1+")*"+factor,"STDEV(C"+st1+":C"+fin1+")*"+factor);
        cellIfF(sh,style2,can,3,7,"STDEV(B"+st2+":B"+fin2+")*"+factor,"STDEV(C"+st2+":C"+fin2+")*"+factor);

        cellSt(sh,style2,4,6,"Доп. канал");
        cellSt(sh,style2,5,6,"M,      deg");
        cellSt(sh,style2,5,7,"SD,   "+stringFactor);

        cellSt(sh,style2,6,5,"Початок");
        cellSt(sh,style2,7,5,"Кінець ");

        cellNIfF(sh,style2,can,6,6,"AVERAGE(B"+st1+":B"+fin1+")","AVERAGE(C"+st1+":C"+fin1+")");
        cellNIfF(sh,style2,can,7,6,"AVERAGE(B"+st2+":B"+fin2+")","AVERAGE(C"+st2+":C"+fin2+")");
        cellNIfF(sh,style2,can,6,7,"STDEV(B"+st1+":B"+fin1+")*"+factor,"STDEV(C"+st1+":C"+fin1+")*"+factor);
        cellNIfF(sh,style2,can,7,7,"STDEV(B"+st2+":B"+fin2+")*"+factor,"STDEV(C"+st2+":C"+fin2+")*"+factor);

        cellSt(sh,style2,9,5,"SignalM, "+stringFactor);
        cellStF(sh,style2,9,6,"(G4-G3)*"+factor);
        cellSt(sh,style2,9,7,"SignalSD, "+stringFactor);
        cellStF(sh,style2,9,8,"SQRT(H4*H4+H3*H3)");
        sh.getRow(9).getCell(5).setCellStyle(style);
        sh.getRow(9).getCell(6).setCellStyle(style);

        cellSt(sh,style2,10,5,"ReferenceM, "+stringFactor);
        cellStF(sh,style2,10,6,"(G8-G7)*"+factor);
        cellSt(sh,style2,10,7,"ReferenceSD, "+stringFactor);
        cellStF(sh,style2,10,8,"SQRT(H8*H8+H7*H7)");

        cellSt(sh,style2,11,5,"timeOfAveraging, min");
        cellSt(sh,style2,11,7,"SignalDuration, min");
        cellStF(sh,style2,11,6,"A"+fin1+"-A"+st1);
        cellStF(sh,style2,11,8,"A"+st2+"-A"+fin1);
        if(istemp){ cellSt(sh,style2,12,5,"T start, °C");
                    cellSt(sh,style2,12,7,"T finish, °C");
                    cellStF(sh,style2,12,6, "AVERAGE(D"+st1+":D"+fin1+")");
                    cellStF(sh,style2,12,8,"AVERAGE(D"+st2+":D"+fin2+")");}

        for(int i=0;i<8;i++)sh.autoSizeColumn(i);

        sh.addMergedRegion(new CellRangeAddress(0,0,6,7));
        sh.addMergedRegion(new CellRangeAddress(4,4,6,7));
        /*                                            Plot                                              */

        Drawing drawing = sh.createDrawingPatriarch();
        ClientAnchor anchor = drawing.createAnchor(0,0,0,0,5,14,13,34);

        Chart chart = drawing.createChart(anchor);
        ChartLegend legend = chart.getOrCreateLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);

        ScatterChartData data = chart.getChartDataFactory().createScatterChartData();

        ArrayList<Double> mas = new ArrayList<>();
        for(int i=s;(i<s+cb)&&(i<table.size());i++) mas.add(table.get(i).get(can));
        double min,max;
        if(corection==true){
            ArrayList<Double> mas2 = new ArrayList<>();
                for (int j=1;j<cb+1;j++) {mas2.add(sh.getRow(j).getCell(this.colomns.length).getNumericCellValue());}
            min=Math.min(Collections.min(mas),Collections.min(mas2));
            max=Math.max(Collections.max(mas),Collections.max(mas2));
        }else{
            min=Collections.min(mas);
            max=Collections.max(mas);
        }


        ValueAxis bottomAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.BOTTOM);
        AxisAutoScale xRes= new AxisAutoScale(table.get(s).get(0),table.get(s+cb).get(0));
        bottomAxis.setMinimum(xRes.min);
        bottomAxis.setMaximum(xRes.max);
        ValueAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);

        AxisAutoScale yRes= new AxisAutoScale(min,max);
        leftAxis.setMinimum(yRes.min);
        leftAxis.setMaximum(yRes.max);
        leftAxis.setCrosses(AxisCrosses.MIN);

        ChartDataSource<Number> xs = DataSources.fromNumericCellRange(sh, new CellRangeAddress(1,cb, 0, 0));
        ChartDataSource<Number> ys1 = DataSources.fromNumericCellRange(sh, new CellRangeAddress(1,cb,can,can));
        if(corection==false){
        ChartDataSource<Number> x2 = DataSources.fromNumericCellRange(sh, new CellRangeAddress(start1,finish1, 0, 0));
        ChartDataSource<Number> ys2 = DataSources.fromNumericCellRange(sh, new CellRangeAddress(start1,finish1,can,can));
        ChartDataSource<Number> x3 = DataSources.fromNumericCellRange(sh, new CellRangeAddress(start2,finish2, 0, 0));
        ChartDataSource<Number> ys3 = DataSources.fromNumericCellRange(sh, new CellRangeAddress(start2,finish2,can,can));

        data.addSerie(xs, ys1);       data.addSerie(x2, ys2);       data.addSerie(x3, ys3);        }
        else{
            ChartDataSource<Number> x2 = DataSources.fromNumericCellRange(sh, new CellRangeAddress(start1,finish1, 0, 0));
            ChartDataSource<Number> ys2 = DataSources.fromNumericCellRange(sh, new CellRangeAddress(start1,finish1,this.colomns.length,this.colomns.length));
            ChartDataSource<Number> x3 = DataSources.fromNumericCellRange(sh, new CellRangeAddress(start2,finish2, 0, 0));
            ChartDataSource<Number> ys3 = DataSources.fromNumericCellRange(sh, new CellRangeAddress(start2,finish2,this.colomns.length,this.colomns.length));
            ChartDataSource<Number> yCor = DataSources.fromNumericCellRange(sh, new CellRangeAddress(1,cb,this.colomns.length,this.colomns.length));
            data.addSerie(xs, ys1);    data.addSerie(xs, yCor);    data.addSerie(x2, ys2);       data.addSerie(x3, ys3);
        }
        chart.deleteLegend();

        chart.plot(data, bottomAxis, leftAxis);

        /*                                            End of plot                                         */
    }

    private void cellSt(Sheet sh,CellStyle st,int row,int cell,String s){
        sh.getRow(row).createCell(cell).setCellValue(s);
        sh.getRow(row).getCell(cell).setCellStyle(st);
    }
    private void cellStF(Sheet sh,CellStyle st,int row,int cell,String s){
        sh.getRow(row).createCell(cell).setCellFormula(s);
        sh.getRow(row).getCell(cell).setCellStyle(st);
    }
    private void cellIfF(Sheet sh,CellStyle st,int can,int row,int cell,String s1,String s2){
        if(can==1) sh.getRow(row).createCell(cell).setCellFormula(s1);
        if(can==2) sh.getRow(row).createCell(cell).setCellFormula(s2);
        sh.getRow(row).getCell(cell).setCellStyle(st);
    }
    private void cellNIfF(Sheet sh,CellStyle st,int can,int row,int cell,String s1,String s2){
        if(can==2) sh.getRow(row).createCell(cell).setCellFormula(s1);
        if(can==1) sh.getRow(row).createCell(cell).setCellFormula(s2);
        sh.getRow(row).getCell(cell).setCellStyle(st);
    }

}
