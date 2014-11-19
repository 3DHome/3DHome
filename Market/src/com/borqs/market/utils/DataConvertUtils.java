package com.borqs.market.utils;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.borqs.market.json.Product;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;


/**
 * common xml conver utility
 *
 * @author viruscodecn@gmail.com
 * @version Framework 2010.10.26
 * @url http://blog.csdn.net/arjick/article/details/6251777
 * http://gejw0623.blog.51cto.com/4532274/1137225
 */
public  class DataConvertUtils {
    private static final String TAG = "DataConvertUtils";

    private static List<Product> getTestValues() {
        List<Product> persons = new ArrayList<Product>();
        Product person = new Product();
        person.product_id = "23";
        person.name = "李磊";
        person.logo_url = ("30");
        persons.add(person);

        person = new Product();
        person.product_id = "20";
        person.name = "韩梅梅";
        person.logo_url = ("25");
        persons.add(person);

        return persons;
    }
    public static String createXML(String filePath) {
        // TODO Auto-generated method stub

        StringWriter stringWriter = new StringWriter();
        // 创建测试数据
        List<Product> persons = getTestValues();

        try {
            // 创建工厂
            SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory
                    .newInstance();

            TransformerHandler handler = factory.newTransformerHandler();

            Transformer info = handler.getTransformer();
            // 是否自动添加额外的空白
            info.setOutputProperty(OutputKeys.INDENT, "yes");
            // 设置字符编码
            info.setOutputProperty(OutputKeys.ENCODING, "utf-8");

            info.setOutputProperty(OutputKeys.VERSION, "1.0");

            // 保存创建的xml
            StreamResult result = new StreamResult(stringWriter);
            handler.setResult(result);

            // 开始xml
            handler.startDocument();

            AttributesImpl impl = new AttributesImpl();
            impl.clear();
            handler.startElement("", "", "persons", impl);

            for (int i = 0; i < persons.size(); i++) {
                Product product = persons.get(i);

                impl.clear();
                impl.addAttribute("", "", ID_TAG, "", product.product_id);
                handler.startElement("", "", "product", impl);

                impl.clear();
                handler.startElement("", "", "name", impl);
                String txt = product.name;
                handler.characters(txt.toCharArray(), 0, txt.length());
                handler.endElement("", "", "name");

                impl.clear();
                handler.startElement("", "", "age", impl);
                txt = product.logo_url;
                handler.characters(txt.toCharArray(), 0, txt.length());
                handler.endElement("", "", "age");

                handler.endElement("", "", "product");

            }

            handler.endElement("", "", "persons");

            // 结束xml
            handler.endDocument();

//            return stringWriter.toString();

            FileWriter fw = new FileWriter(filePath);
            fw.write(stringWriter.toString());
            fw.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        return null;
    }
    public static boolean isExistingFolderPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File root = new File(path);
        return root.exists() && root.isDirectory();
    }

    public static boolean scanBuildingFromParentPath(String path) {
        File current = new File(path);
        if (null == current) {
            return false;
        }

        if (current.exists()) {
            if (current.isDirectory()) {
                File[] files = current.listFiles();
                if (null != files && files.length > 0) {
                    return false;
                }
            } else {
                current.delete();
            }
        }

        boolean added = false;
        current.mkdir();
        File parent = current.getParentFile();
        if (null != parent && parent.exists() && parent.isDirectory()) {
            File[] fileList = parent.listFiles();
            for (File file : fileList) {
                File configFile = new File(file.getPath() + File.separator + MarketUtils.MARKET_CONFIG_FILE_NAME);
                if (configFile.exists()) {
                    Product product = createFromConfigFile(configFile);
                    if (isWallpaperProduct(product)) {
                        File[] files = file.listFiles();

                        if (file.renameTo(new File(path + File.separator + file.getName()))) {
                            if (!TextUtils.isEmpty(product.logo_url)) {
                                product.logo_url = Uri.fromFile(new File(file.getAbsolutePath() + File.separator + product.logo_url)).toString();
                            }
                        } else {
                            BLog.w(TAG, "scanBuildingFromParentPath, fail to move file " + file.getAbsolutePath());
                        }
                        added = true;
                    }
                } else {
                    // no config file
                    Log.v(TAG, "scanBuildingFromParentPath, no market conf found in " + file.getAbsolutePath());
                }
            }
        }
        return added;
    }
    private static Product createFromConfigFile(File configFile) {
        try {
            InputStream is = new FileInputStream(configFile);
            Product product = new SAX_parserSingleXML().ReadXML(is);
            return product;
        } catch (FileNotFoundException fnf) {
            fnf.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private static void parseLocalWallpaper(List<Product> resultList, File file) {
        if (file != null && resultList != null) {
            if (file.exists() && file.isDirectory()) {
                File configFile = new File(file.getPath() + File.separator + MarketUtils.MARKET_CONFIG_FILE_NAME);
                if (configFile.exists()) {
                    Product product = createFromConfigFile(configFile);
                    if (isWallpaperProduct(product)) {
                        File[] files = file.listFiles();

                        if (!TextUtils.isEmpty(product.logo_url)) {
                            product.logo_url = Uri.fromFile(new File(file.getAbsolutePath() + File.separator + product.logo_url)).toString();
                        }
                        if (!TextUtils.isEmpty(product.cover_url)) {
                            product.cover_url = Uri.fromFile(new File(file.getAbsolutePath() + File.separator + product.cover_url)).toString();
                        }
                        if(product.screenshots != null && product.screenshots.size() > 0) {
                            ArrayList<String> screenshots = new ArrayList<String>();
                            for(int i = 0; i < product.screenshots.size(); i++) {
                                screenshots.add(i,Uri.fromFile(new File(file.getAbsolutePath() + File.separator + product.screenshots.get(i))).toString());
                            }
                            product.screenshots = screenshots;
                        }
                        resultList.add(product);
                    }
                } else {
                    // no config file
                    Log.v(TAG, "parseLocalWallpaper, no market conf found in " + file.getAbsolutePath());
                }
            }
        }
    }
    public static boolean isWallpaperProduct(Product product) {
        if (null != product) {
            return Product.ProductType.WALL_PAPER.equalsIgnoreCase(product.type);
        }
        return false;
    }
    public static List<Product> parseXML(String parentPath) {
        List<Product> result = new ArrayList<Product>();
//        if (scanBuildingFromParentPath(parentPath)) {
//            return result;
//        }

        if (isExistingFolderPath(parentPath)) {
            File folder = new File(parentPath);
            File[] files = folder.listFiles();
            for (File f : files) {
                parseLocalWallpaper(result, f);
            }
        }
        return result;
    }

    private static final String START_TAG = "BorqsResource";
    private static final String ID_TAG = "id";
    private static final String NAME_EN_TAG = "en_US";
    private static final String NAME_ZH_TAG = "zh_CN";
    private static final String LOGO_TAG = "logo";
    private static final String COVER_TAG = "cover";
    private static final String VERSION_TAG = "version";
    private static final String AUTHOR_TAG = "authorName";
    private static final String TYPE = "category";
    private static final String SCREENSHOT1_TAG = "screenshot1";
    private static final String SCREENSHOT2_TAG = "screenshot2";
    private static final String SCREENSHOT3_TAG = "screenshot3";
    private static final String SCREENSHOT4_TAG = "screenshot4";
    public static class SAX_parserSingleXML extends DefaultHandler {
        Product product;
        // 当前节点
        String curNode;

        public Product ReadXML(InputStream is) {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            try {
                SAXParser parser = factory.newSAXParser();

                // 第一种方法
                // parser.parse(is, this);

                // 第二种方法
                XMLReader reader = parser.getXMLReader();
                reader.setContentHandler(this);
                reader.parse(new InputSource(is));

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }

            return product;
        }

        /**
         * 当开始解析xml文件的声明的时候就会触发这个事件, 可以做一些初始化的工作
         * */
        @Override
        public void startDocument() throws SAXException {
            // TODO Auto-generated method stub
            super.startDocument();
        }

        /**
         * 当开始解析元素的开始标签的时候,就会触发这个事件
         * */
        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            // TODO Auto-generated method stub
            super.startElement(uri, localName, qName, attributes);

            // 如果读到是person标签 开始存储
            if (localName.equals(START_TAG)) {
                product = new Product();
                BLog.v("WallpaperLocalGridFragment", "product = new Product()");
//                product.product_id = attributes.getValue("id");
            }
            curNode = localName;
        }

        /**
         * 当读到文本元素的时候要触发这个事件.
         * */
        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            // TODO Auto-generated method stub
            super.characters(ch, start, length);

            if (product != null) {
                // 取出目前元素对应的值
                String txt = new String(ch, start, length);
                // 判断元素是否是name.set_name(
                if (curNode.equals(NAME_EN_TAG) || curNode.equals(NAME_ZH_TAG)) {
                    // 将取出的值添加到person对象
                    if (TextUtils.isEmpty(product.name)) {
                        product.name = txt;
                    }
                } else if (curNode.equals(LOGO_TAG)) {
                    product.logo_url = txt;
                } else if (curNode.equals(COVER_TAG)) {
                    product.cover_url = txt;
                } else if (curNode.equalsIgnoreCase(ID_TAG)) {
                    product.product_id = txt;
                } else if (curNode.equalsIgnoreCase(VERSION_TAG)) {
                    product.version_code = Integer.parseInt(txt);
                } else if (curNode.equalsIgnoreCase(AUTHOR_TAG)) {
                    product.author = txt;
                } else if (curNode.equalsIgnoreCase(SCREENSHOT1_TAG)) {
                    BLog.v("WallpaperLocalGridFragment", curNode +"=  "+txt);
                    product.screenshots.add(txt);
                } else if (curNode.equalsIgnoreCase(SCREENSHOT2_TAG)) {
                    BLog.v("WallpaperLocalGridFragment", curNode +"=  "+txt);
                    product.screenshots.add(txt);
                } else if (curNode.equalsIgnoreCase(SCREENSHOT3_TAG)) {
                    BLog.v("WallpaperLocalGridFragment", curNode +"=  "+txt);
                    product.screenshots.add(txt);
                } else if (curNode.equalsIgnoreCase(SCREENSHOT4_TAG)) {
                    BLog.v("WallpaperLocalGridFragment", curNode +"=  "+txt);
                    product.screenshots.add(txt);
                } else if (curNode.equalsIgnoreCase(TYPE)) {
                    product.type = txt;
                }
            }
        }

        /**
         * 当读到结束标签的时候 就会触发这个事件
         * */
        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            // TODO Auto-generated method stub
            super.endElement(uri, localName, qName);

            // 如果是person结尾 并且person不为空，添加到list
//            if (localName.equals("product") && product != null) {
//                persons.add(product);
//                product = null;
//            }

            curNode = "";
        }
    }

//    public static class SAX_parserXML extends DefaultHandler {
//
//        List<Product> persons;
//        Product product;
//        // 当前节点
//        String curNode;
//
//        public List<Product> ReadXML(InputStream is) {
//
//            SAXParserFactory factory = SAXParserFactory.newInstance();
//            try {
//                SAXParser parser = factory.newSAXParser();
//
//                // 第一种方法
//                // parser.parse(is, this);
//
//                // 第二种方法
//                XMLReader reader = parser.getXMLReader();
//                reader.setContentHandler(this);
//                reader.parse(new InputSource(is));
//
//            } catch (Exception e) {
//                // TODO: handle exception
//                e.printStackTrace();
//            }
//
//            return persons;
//        }
//
//        /**
//         * 当开始解析xml文件的声明的时候就会触发这个事件, 可以做一些初始化的工作
//         * */
//        @Override
//        public void startDocument() throws SAXException {
//            // TODO Auto-generated method stub
//            super.startDocument();
//            // 初始化list
//            persons = new ArrayList<Product>();
//        }
//
//        /**
//         * 当开始解析元素的开始标签的时候,就会触发这个事件
//         * */
//        @Override
//        public void startElement(String uri, String localName, String qName,
//                                 Attributes attributes) throws SAXException {
//            // TODO Auto-generated method stub
//            super.startElement(uri, localName, qName, attributes);
//
//            // 如果读到是person标签 开始存储
//            if (localName.equals("product")) {
//                product = new Product();
//                product.set_id(attributes.getValue("id"));
//            }
//            curNode = localName;
//        }
//
//        /**
//         * 当读到文本元素的时候要触发这个事件.
//         * */
//        @Override
//        public void characters(char[] ch, int start, int length)
//                throws SAXException {
//            // TODO Auto-generated method stub
//            super.characters(ch, start, length);
//
//            if (product != null) {
//                // 取出目前元素对应的值
//                String txt = new String(ch, start, length);
//                // 判断元素是否是name
//                if (curNode.equals("name")) {
//                    // 将取出的值添加到person对象
//                    product.set_name(txt);
//                } else if (curNode.equals("age")) {
//                    product.set_age(txt);
//                }
//            }
//        }
//
//        /**
//         * 当读到结束标签的时候 就会触发这个事件
//         * */
//        @Override
//        public void endElement(String uri, String localName, String qName)
//                throws SAXException {
//            // TODO Auto-generated method stub
//            super.endElement(uri, localName, qName);
//
//            // 如果是person结尾 并且person不为空，添加到list
//            if (localName.equals("product") && product != null) {
//                persons.add(product);
//                product = null;
//            }
//
//            curNode = "";
//        }
//    }
}