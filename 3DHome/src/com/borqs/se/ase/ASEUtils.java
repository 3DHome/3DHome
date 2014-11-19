package com.borqs.se.ase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.borqs.se.engine.SEVector.SERotate;
import com.borqs.se.engine.SEVector.SEVector3f;

public class ASEUtils {

    public static void changeModelInfoXML(String xmlPath, SEVector3f translate, SERotate rotate, SEVector3f scale,
            boolean openLight) {
        try {
            InputStream is = new FileInputStream(xmlPath);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            is.close();
            NodeList config = doc.getElementsByTagName("config");
            if (config.getLength() > 0) {
                String strlocalTrans = translate.toString() + "," + rotate.toString() + "," + scale.toString();
                NamedNodeMap attributes = config.item(0).getAttributes();
                Node localTrans = attributes.getNamedItem("localTrans");
                if (localTrans == null) {
                    Attr attr = doc.createAttribute("localTrans");
                    attr.setValue(strlocalTrans);
                    attributes.setNamedItem(attr);
                } else {
                    localTrans.setNodeValue(strlocalTrans);
                }
            }

            TransformerFactory tfs = TransformerFactory.newInstance();
            // 创建Transformer对象
            Transformer tf = tfs.newTransformer();
            // 将document输出到输出流中。
            OutputStream out = new FileOutputStream(xmlPath);
            tf.transform(new DOMSource(doc), new StreamResult(out));
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static void changeModelInfoXML(String folePath , String xmlPath) {
        try {
            File check = new File (folePath + File.separator + "isUser3DModel");
            if (check.exists()) {
                return;
            }
            check.createNewFile();
            InputStream is = new FileInputStream(xmlPath);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            is.close();
            NodeList BorqsResource = doc.getElementsByTagName("BorqsResource");
            Node node = BorqsResource.item(0);
   
            NodeList cover = doc.getElementsByTagName("cover");
            if (cover!= null && cover.getLength() > 0) {
                String value = cover.item(0).getTextContent();
                File file = new File (folePath + File.separator + value);
                if (!file.exists()) {
                    file =  new File (folePath + File.separator + "cover.jpg");
                    if (file.exists())  {
                        cover.item(0).setTextContent("cover.jpg");
                    } else {
                        node.removeChild(cover.item(0));
                    }
                }
            }
            NodeList screenshot1 = doc.getElementsByTagName("screenshot1");
            if (screenshot1!= null && screenshot1.getLength() > 0) {
                String value = screenshot1.item(0).getTextContent();
                File file = new File (folePath + File.separator + value);
                if (!file.exists()) {
                    file =  new File (folePath + File.separator + "screenshot1.jpg");
                    if (file.exists())  {
                        screenshot1.item(0).setTextContent("screenshot1.jpg");
                    } else {
                        node.removeChild(screenshot1.item(0));
                    }
                }
            }
            NodeList screenshot2 = doc.getElementsByTagName("screenshot2");
            if (screenshot2!= null && screenshot2.getLength() > 0) {
                String value =  screenshot2.item(0).getTextContent();
                File file = new File (folePath + File.separator + value);
                if (!file.exists()) {
                    file =  new File (folePath + File.separator + "screenshot2.jpg");
                    if (file.exists())  {
                        screenshot2.item(0).setTextContent("screenshot2.jpg");
                    } else {
                        node.removeChild(screenshot2.item(0));
                    }
                }
            }
            NodeList screenshot3 = doc.getElementsByTagName("screenshot3");
            if (screenshot3!= null && screenshot3.getLength() > 0) {
                String value =  screenshot3.item(0).getTextContent();
                File file = new File (folePath + File.separator + value);
                if (!file.exists()) {
                    file =  new File (folePath + File.separator + "screenshot3.jpg");
                    if (file.exists())  {
                        screenshot3.item(0).setTextContent("screenshot3.jpg");
                    } else {
                        node.removeChild(screenshot3.item(0));
                    }
                }
            }
            TransformerFactory tfs = TransformerFactory.newInstance();
            // 创建Transformer对象
            Transformer tf = tfs.newTransformer();
            // 将document输出到输出流中。
            OutputStream out = new FileOutputStream(xmlPath);
            tf.transform(new DOMSource(doc), new StreamResult(out));
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

//    public void append2XML(InputStream inputStream, String instructions) throws ParserConfigurationException,
//            SAXException, IOException, TransformerException {
//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        // 通过实例构建DocumentBuilder
//        DocumentBuilder db = dbf.newDocumentBuilder();
//        // 创建Document 解析给定的文件
//        Document doc = db.parse(inputStream);
//        Element a = doc.createElement("");
//        // doc = db.newDocument();
//        // 添加一个元素
//        Element eModel = doc.createElement("instructionsModel");
//        Element eInstructions = doc.createElement("instructions");
//        Element eCreatetime = doc.createElement("createtime");
//        Element eLastUseTime = doc.createElement("lastusetime");
//        // 在元素中添加文本
//        Text textInstructions = doc.createTextNode(instructions);
//        Text textCreatetime = doc.createTextNode("");
//        Text textLastUseTime = doc.createTextNode("");
//        eInstructions.appendChild(textInstructions);
//        eCreatetime.appendChild(textCreatetime);
//        eLastUseTime.appendChild(textLastUseTime);
//        // 通过Name获取节点 第0个节点
//        eModel.appendChild(eInstructions);
//        eModel.appendChild(eCreatetime);
//        eModel.appendChild(eLastUseTime);
//        Node book = doc.getElementsByTagName("instructionsList").item(0);
//        book.appendChild(eModel);
//        // 添加一个属性
//        // Attr attr = doc.createAttribute("aaa");
//        // 创建工厂对象
//        TransformerFactory tfs = TransformerFactory.newInstance();
//        // 创建Transformer对象
//        Transformer tf = tfs.newTransformer();
//        // 将document输出到输出流中。
//        tf.transform(new DOMSource(doc), new StreamResult(new FileOutputStream("")));
//    }
}
