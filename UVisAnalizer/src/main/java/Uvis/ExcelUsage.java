package Uvis;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


class ExcelUsage {

    private ArrayList<ArrayList<Double>> table= new ArrayList<>() ;
    private Workbook wb;

    ExcelUsage(spectrumXML spectres,double fromWaveLength, double toWaveLength){
        ArrayList<String> colomns=new ArrayList<>();
        colomns.add("λ, нм");
        for (spectrumXML.Spectrum spect:spectres.spectres) {
            colomns.add(spect.title);
        }

        int[]indexSrartAndFinish=findFromToByX(spectres.spectres.get(0).wavelength,fromWaveLength,toWaveLength);
        for (int index=indexSrartAndFinish[0];index<=indexSrartAndFinish[1];index++)
            this.table.get(index-indexSrartAndFinish[0]).add(spectres.spectres.get(0).wavelength.get(index));

        for (int columns=0;columns<spectres.spectres.size();columns++)
        for (int index=indexSrartAndFinish[0];index<=indexSrartAndFinish[1];index++) {
           this.table.get(columns).add(spectres.spectres.get(columns).absorbency.get(index));
        }
        
        this.wb=new XSSFWorkbook();
        Sheet sh1 = this.wb.createSheet(WorkbookUtil.createSafeSheetName("curves and fitting"));
        insertTable(sh1,colomns,this.table,0,0);


    }

    void allToExcell(String path) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(path);
        this.wb.write(outputStream);// write down the data
        outputStream.close();//закрий поток
    }

    public static int[] findFromToByX (ArrayList<Double> mass,double from, double to){
        int[] result=new int[2];
        for (int i=1;i<mass.size();i++) {
            if (mass.get(i)>from)result[0]=i-1;
        }
        if (mass.get(mass.size()-1)<from) System.out.println("start wavelength too big");

        for (int i=mass.size()-2;i>0;i--) {
            if (mass.get(i)<to)result[1]=i+1;
        }
        if (mass.get(0)>to) System.out.println("finish wavelength is too small");

        return result;
    }

    public void insertTable (Sheet sheet1,ArrayList<String> names,ArrayList<ArrayList<Double>> data,int firstColumn, int firstRow){
        Row theFirstRow = sheet1.createRow(firstRow);
        for (int i=firstColumn;i<firstColumn+names.size();i++) theFirstRow.createCell(i).setCellValue(names.get(i));

        for (int j=1+firstRow;j<firstRow+data.size();j++) {
            Row thisRow = sheet1.createRow(j);
            for (int i = firstColumn; i < firstColumn+data.get(j).size(); i++) thisRow.createCell(i).setCellValue(data.get(j).get(i));
            j++;
        }
        for(int i=firstColumn;i<firstColumn+names.size();i++)sheet1.autoSizeColumn(i);
    }

    public void insertColumn(Sheet sheet1, ArrayList<?> column, int firstColumn, int firstRow){

        for (int j=0;j<column.size();j++) {
            Row thisRow;
            if (sheet1.getTopRow()<j+firstRow)thisRow= sheet1.createRow(j+firstRow);else thisRow=sheet1.getRow(j+firstRow);
            if (column.get(j) instanceof Double)thisRow.createCell(firstColumn).setCellValue((Double)column.get(j));
            if (column.get(j) instanceof String)thisRow.createCell(firstColumn).setCellValue((String) column.get(j));
        }

    }

    public static void main(String[] args) {

    }
}
