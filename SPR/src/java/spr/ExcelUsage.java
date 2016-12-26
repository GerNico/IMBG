package spr;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.charts.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;


class ExcelUsage {

    private boolean istemp;
    private String[] colomns;
    private ArrayList<ArrayList<Double>> table= new ArrayList<>() ;
    private Workbook wb;

    ExcelUsage(SPR spr){
        if(spr.isTemperature)this.colomns= new String[]{"Час, хв", "внутр.(черв.) канал",
                "зовн.(синій.) канал","температура, °C"};
        else colomns= new String[]{"Час, хв", "внутр.(черв.) канал", "зовн.(синій.) канал"};
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
    }

    void allToExcell(String path) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(path);
        this.wb.write(outputStream);// write down the data
        outputStream.close();//закрий поток
    }

    void fragToXLS (String sheetName, int s,int cb,int s2,int s3,int cb2,int can) throws IOException {

        Sheet sh = this.wb.createSheet(WorkbookUtil.createSafeSheetName(sheetName));
        Row firstRow = sh.createRow(0);
        for (int i=0;i<colomns.length;i++) firstRow.createCell(i).setCellValue(colomns[i]);

        for (int j=1;j<cb+1;j++) {
            Row thisRow = sh.createRow(j);
            for (int i = 0; i < table.get(j-1).size(); i++)
                if(s+j-1<table.size())thisRow.createCell(i).setCellValue(table.get(s+j-1).get(i));
        }

        CellStyle style = this.wb.createCellStyle();
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);//без цього не працює
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
        start1=s2-s+2;  finish1=s2-s+cb2+2;
        start2=s3-s+2;  finish2=s3-s+cb2+2;

        for(int i=start1-1;i<finish1;i++)sh.getRow(i).getCell(can).setCellStyle(style);
        for(int i=start2-1;i<finish2;i++)sh.getRow(i).getCell(can).setCellStyle(style);

        cellSt(sh,style2,0,6,"Осн.  канал");
        cellSt(sh,style2,1,6,"M,      deg");
        cellSt(sh,style2,1,7,"SD,    mdeg");

        cellSt(sh,style2,2,5,"Початок");
        cellSt(sh,style2,3,5,"Кінець");

        cellIfF(sh,style2,can,2,6,"AVERAGE(B"+start1+":B"+finish1+")","AVERAGE(C"+start1+":C"+finish1+")");
        cellIfF(sh,style2,can,3,6,"AVERAGE(B"+start2+":B"+finish2+")","AVERAGE(C"+start2+":C"+finish2+")");
        cellIfF(sh,style2,can,2,7,"STDEV(B"+start1+":B"+finish1+")*1000","STDEV(C"+start1+":C"+finish1+")*1000");
        cellIfF(sh,style2,can,3,7,"STDEV(B"+start2+":B"+finish2+")*1000","STDEV(C"+start2+":C"+finish2+")*1000");

        cellSt(sh,style2,4,6,"Доп. канал");
        cellSt(sh,style2,5,6,"M,      deg");
        cellSt(sh,style2,5,7,"SD,    mdeg");

        cellSt(sh,style2,6,5,"Початок");
        cellSt(sh,style2,7,5,"Кінець ");

        cellNIfF(sh,style2,can,6,6,"AVERAGE(B"+start1+":B"+finish1+")","AVERAGE(C"+start1+":C"+finish1+")");
        cellNIfF(sh,style2,can,7,6,"AVERAGE(B"+start2+":B"+finish2+")","AVERAGE(C"+start2+":C"+finish2+")");
        cellNIfF(sh,style2,can,6,7,"STDEV(B"+start1+":B"+finish1+")*1000","STDEV(C"+start1+":C"+finish1+")*1000");
        cellNIfF(sh,style2,can,7,7,"STDEV(B"+start2+":B"+finish2+")*1000","STDEV(C"+start2+":C"+finish2+")*1000");

        cellSt(sh,style2,9,5,"Sm, mdeg");                   cellSt(sh,style2,9,7,"Sr, mdeg");
        cellStF(sh,style2,9,6,"(G4-G3)*1000");              cellStF(sh,style2,9,8,"(G8-G7)*1000");
        cellSt(sh,style2,10,5,"NM, mdeg");                  cellSt(sh,style2,10,7,"NR, mdeg");
        cellStF(sh,style2,10,6,"SQRT(H4*H4+H3*H3)");        cellStF(sh,style2,10,8,"SQRT(H8*H8+H7*H7)");

        cellSt(sh,style2,11,5,"t0, min");                   cellSt(sh,style2,11,7,"dt, min");
        cellStF(sh,style2,11,6,"A"+finish1+"-A"+start1);    cellStF(sh,style2,11,8,"A"+start2+"-A"+finish1);
        if(istemp){cellSt(sh,style2,12,5,"TM, °C");         cellSt(sh,style2,12,7,"TR, °C");
            cellStF(sh,style2,12,6, "AVERAGE(D"+start1+":D"+finish1+")");
            cellStF(sh,style2,12,8,"AVERAGE(D"+start2+":D"+finish2+")");}

        for(int i=0;i<8;i++)sh.autoSizeColumn(i);

        sh.addMergedRegion(new CellRangeAddress(0,0,6,7));  sh.addMergedRegion(new CellRangeAddress(4,4,6,7));
        /*                                            Plot                                              */

        Drawing drawing = sh.createDrawingPatriarch();
        ClientAnchor anchor = drawing.createAnchor(0,0,0,0,5,14,13,34);//_ _ _ _ x0 y0 xf yf

        Chart chart = drawing.createChart(anchor);
        ChartLegend legend = chart.getOrCreateLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);

        ScatterChartData data = chart.getChartDataFactory().createScatterChartData();

        ArrayList<Double> mas = new ArrayList<>();
        for(int i=s;(i<s+cb)&&(i<table.size());i++)
            mas.add(table.get(i).get(can));

        ValueAxis bottomAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.BOTTOM);
        bottomAxis.setMinimum(RoundN(table.get(s).get(0),0));
        bottomAxis.setMaximum(table.get(s+cb).get(0));
        ValueAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);
        double min=RoundN(Collections.min(mas),4);
        double max=RoundN(Collections.max(mas),4);
        leftAxis.setMinimum(RoundN(min-(max-min)/10d,4));
        leftAxis.setMaximum(RoundN(max+(max-min)/10d,4));
        leftAxis.setCrosses(AxisCrosses.MIN);

        ChartDataSource<Number> xs = DataSources.fromNumericCellRange(sh, new CellRangeAddress(1,cb, 0, 0));
        ChartDataSource<Number> ys1 = DataSources.fromNumericCellRange(sh, new CellRangeAddress(1,cb,can,can));
        ChartDataSource<Number> x2 = DataSources.fromNumericCellRange(sh, new CellRangeAddress(s2-s+1,s2-s+cb2+1, 0, 0));
        ChartDataSource<Number> ys2 = DataSources.fromNumericCellRange(sh, new CellRangeAddress(s2-s+1,s2-s+cb2+1,can,can));
        ChartDataSource<Number> x3 = DataSources.fromNumericCellRange(sh, new CellRangeAddress(s3-s+1,s3-s+cb2+1, 0, 0));
        ChartDataSource<Number> ys3 = DataSources.fromNumericCellRange(sh, new CellRangeAddress(s3-s+1,s3-s+cb2+1,can,can));


        data.addSerie(xs, ys1);       data.addSerie(x2, ys2);       data.addSerie(x3, ys3);
        chart.deleteLegend();

        chart.plot(data, bottomAxis, leftAxis);

        /*                                            End of plot                                         */
    }

    private void cellSt(Sheet sh,CellStyle st,int row,int cell,String s){
        sh.getRow(row).createCell(cell).setCellValue(s);sh.getRow(row).getCell(cell).setCellStyle(st);
    }
    private void cellStF(Sheet sh,CellStyle st,int row,int cell,String s){
        sh.getRow(row).createCell(cell).setCellFormula(s);sh.getRow(row).getCell(cell).setCellStyle(st);
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
    private double RoundN(double doUble,int n){
        return new BigDecimal(doUble).setScale(n, RoundingMode.HALF_DOWN).doubleValue();
    }
}