/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package convertcsv;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JLabel;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
/**
 *
 * @author RK
 */
public class Common {
    
    String defaultPath = "File Name You Selected.";
    String zipFileName = null;
    String csvFileName = null;
    InputStream XMLFileStream = null;
    JList ls_files = null;
    JList ls_rows = null;
    JLabel lbl_count = null;
    DefaultListModel mFiles = null;
    DefaultListModel mRows = null;
    List<String> header;
    List<HashMap<String,String>> rows;
    
    public Common(){
        this.header = new ArrayList<String>();
        this.rows = new ArrayList<HashMap<String,String>>();
        mFiles = new DefaultListModel();
        mRows = new DefaultListModel();
    }    

    boolean findEDRMFile() {
        ls_files.setModel(mFiles);
        if(this.zipFileName == null)
            return false;
        else{
            try{
                ZipFile zip = new ZipFile(this.zipFileName);
                for (Enumeration e = zip.entries(); e.hasMoreElements(); ) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    mFiles.addElement(entry.getName());
                    if (!entry.isDirectory()) {
                        if("EDRM.XML".equals(entry.getName().substring(entry.getName().length() - 8))){
                            this.XMLFileStream = zip.getInputStream(entry);
                            mFiles.addElement("XML File is found");
                            return true;
                        }
                    }
                }
            }catch(IOException e){
                return false;
            }
        }
        return false;
    }

    boolean processXML() throws SAXException {
        // Here I can use recursive function. But time ....
        try {
            ls_rows.setModel(mRows);
            int count = 0;
            // convert XML tree to DOM object
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();  
            Document doc = db.parse(this.XMLFileStream);
            doc.getDocumentElement().normalize(); 
            // find <Batch> node. there is not need to from <root> node.
            NodeList nodeList = doc.getElementsByTagName("Batch");
            //get first Batch. In xml, there is one <Batch> tag
            Node batchNode = nodeList.item(0);
            if(batchNode.hasChildNodes()){
                NodeList documentsAnOtherNode = batchNode.getChildNodes();
                //<Batch> tag contains one <Documents> tag
                Node documentsNode = null;
                for(int i =0; i <documentsAnOtherNode.getLength(); i++){
                    documentsNode = documentsAnOtherNode.item(i);
                    if (documentsNode.getNodeType() == Node.ELEMENT_NODE && "Documents".equals(documentsNode.getNodeName())) {
                        break;
                    }
                }
                if(documentsNode == null)
                    return false;
                else{
                    // <Document> tag list
                    NodeList documentList = documentsNode.getChildNodes();
                    for(int pos = 0; pos < documentList.getLength(); pos ++){
                        Node document = documentList.item(pos);
                        if(document.getNodeType() == Node.ELEMENT_NODE){
                            // get <Document> tag attributes.
                            NamedNodeMap attr = document.getAttributes();
                            HashMap<String, String> oneRow = new HashMap<String, String>();
                            for(int i = 0; i < attr.getLength(); i++){
                                if(!this.header.contains(attr.item(i).getNodeName()))
                                   this.header.add(attr.item(i).getNodeName());
                                oneRow.put(attr.item(i).getNodeName(), attr.item(i).getTextContent());
                            }
                            this.rows.add(oneRow);
                            oneRow = new HashMap<String, String>();
                            this.mRows.addElement(oneRow.values().toString());
                            count++;
                            this.lbl_count.setText(Integer.toString(count));
                            // get <FieldValues> tag
                            NodeList fieldValuesList = document.getChildNodes();
                            Node fieldValuesNode = null;
                            for(int i =0; i <fieldValuesList.getLength(); i++){
                                fieldValuesNode = fieldValuesList.item(i);
                                if (fieldValuesNode.getNodeType() == Node.ELEMENT_NODE && "FieldValues".equals(fieldValuesNode.getNodeName())) {
                                    break;
                                }
                            }
                            // get sub tags of <FieldValues> tag
                            NodeList fieldList = fieldValuesNode.getChildNodes();
                            for(int p = 0; p < fieldList.getLength(); p ++){
                                Node field = fieldList.item(p);
                                if(field.getNodeType() == Node.ELEMENT_NODE){
                                    //get all data
                                    Element temp = (Element) field;
                                    if(!this.header.contains(temp.getTagName()))
                                        this.header.add(temp.getTagName());
                                    oneRow.put(temp.getTagName(), temp.getTextContent());
                                }
                            }
                            this.rows.add(oneRow);
                            this.mRows.addElement(oneRow.values().toString());
                            count++;
                            this.lbl_count.setText(Integer.toString(count));
                        }
                    }
                }
            }
            System.out.println("ok");
            return true;
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Common.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (SAXException ex) {
            Logger.getLogger(Common.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(Common.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } 
    }

    boolean saveToCSV() {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(csvFileName));
            String[] header = new String[this.header.size()];
            header = this.header.toArray(header);
            writer.writeNext(header);
            List<String[]> content = new ArrayList<String[]>();
            for(int i = 0; i < this.rows.size(); i++){
                String[] temp = new String[this.header.size()];
                for(int p = 0; p < this.header.size(); p++){
                    temp[p] = this.rows.get(i).get(this.header.get(p));
                }
                content.add(temp);
            }
            writer.writeAll(content);
            // quote problem is solved.
            writer.close();
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Common.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
