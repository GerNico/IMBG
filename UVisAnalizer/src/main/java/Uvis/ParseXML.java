package Uvis;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

/**
 * Created by Matsishin on 10.09.2016.
 *
 * @author Matsishin Nicolas
 *         This class is for parsing XML files of Nanodrop2000 spectrometr report
 */
class ParseXML {

    ArrayList<WorkSheet> workSheets = new ArrayList<>();

    class WorkSheet {
        String title;
        String time;
        ArrayList<Double> wavelength = new ArrayList<>();
        ArrayList<Double> absorbance = new ArrayList<>();
        boolean selected = false;

        WorkSheet(String title, String time, ArrayList<Double> w, ArrayList<Double> a) {
            this.title = title;
            this.time = time;
            this.wavelength = w;
            this.absorbance = a;
        }
    }

    //This function is called recursively
    ParseXML(Document document) {
        //Normalize the XML Structure; It's just too important !!
        document.getDocumentElement().normalize();

        NodeList nList = document.getElementsByTagName("Worksheet");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node node = nList.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                NodeList subList = node.getChildNodes();
                NodeList table = subList.item(1).getChildNodes();
                //Визначаємо імя
                Node nameNode = table.item(11);
                ArrayList<Double> tableW = new ArrayList<>();
                ArrayList<Double> tableA = new ArrayList<>();

                tableW.add(Double.parseDouble(nameNode.getChildNodes().item(1).getLastChild().getTextContent()));
                tableA.add(Double.parseDouble(nameNode.getChildNodes().item(3).getLastChild().getTextContent()));
                String titleCell = nameNode.getChildNodes().item(5).getLastChild().getTextContent();
                String timeCell = nameNode.getChildNodes().item(7).getLastChild().getTextContent();

                for (int i = 6; i < table.getLength() / 2; i++) {
                    NodeList row = table.item(2 * i + 1).getChildNodes();

                    tableW.add(Double.parseDouble(row.item(1).getLastChild().getTextContent()));
                    tableA.add(Double.parseDouble(row.item(3).getLastChild().getTextContent()));

                }
                WorkSheet workSheet = new WorkSheet(titleCell, timeCell, tableW, tableA);
                workSheets.add(workSheet);
            }
        }
    }
}
