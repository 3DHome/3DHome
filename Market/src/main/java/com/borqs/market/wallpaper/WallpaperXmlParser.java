package com.borqs.market.wallpaper;

import android.net.Uri;
import android.text.TextUtils;

import com.borqs.market.json.Product;
import com.borqs.market.json.Product.ProductType;
import com.borqs.market.json.Product.SupportedMod;
import com.borqs.market.utils.MarketUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class WallpaperXmlParser {
    private static final String AUTHOR_EMAIL_TAG = "authorEmail";
    private static final String SCREENSHOT1_TAG = "screenshot1";
    private static final String SCREENSHOT2_TAG = "screenshot2";
    private static final String SCREENSHOT3_TAG = "screenshot3";
    private static final String SCREENSHOT4_TAG = "screenshot4";
    private static final String SCREENSHOT5_TAG = "screenshot5";
    private static final String SUPPORTED_MOD_TAG = "supportedMod";


    private static final String TAG_ID = "id";
    private static final String TAG_PKG_NAME = "app";
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_NAME = "name";
    private static final String TAG_LANG = "langs";
    private static final String TAG_EN = "en_US";
    private static final String TAG_CN = "zh_CN";
    private static final String TAG_DESC = "description";
    private static final String TAG_AUTHOR = "authorName";
    private static final String TAG_LOGO = "logo";
    private static final String TAG_COVER = "cover";
    private static final String TAG_VERSION = "version";
    private static final String TAG_APP_VERSION = "maxAppVC";
    private static final String TAG_VER_NAME = "versionName";
    private static final String TAG_RECENT_CHANGE = "recentChange";

    private static final String TAG_PAPERS = "PAPERS";
    private static final String TAG_PAPER_ITEM = "PAPER_ITEM";
    private static final String TAG_PAPER_NAME = "PAPER_NAME";
//    private static final String TAG_PAPER_PATH_DEFAULT = "PATH_DEFAULT";
//    private static final String TAG_PAPER_PATH_OVERLAY = "PATH_OVERLAY";
    private static final String TAG_PAPER_PATH_DECODED = "PATH_DECODED";
    private static final String TAG_PAPER_TYPE = "PAPER_TYPE";

    public static Product parser(File file) {
        String nameTag = TAG_EN;
        File configFile = new File(file.getPath() + File.separator + MarketUtils.MARKET_CONFIG_FILE_NAME);
        Product product = null;
        XmlPullParserFactory factory;
        try {
            product = new Product();
            product.screenshots = new ArrayList<String>();
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            InputStream stream = new FileInputStream(configFile);

            parser.setInput(stream,  "UTF-8");
            int eventType = parser.getEventType();
            while(eventType != XmlPullParser.END_DOCUMENT) {
                switch(eventType) {
                case XmlPullParser.START_TAG: {
                    String tag = parser.getName();
                    if(TAG_ID.equalsIgnoreCase(tag)) {
                        product.product_id =  parser.nextText();
                    }else if(TAG_NAME.equalsIgnoreCase(tag)) {
                        while(!(TAG_NAME.equalsIgnoreCase(tag) && eventType == XmlPullParser.END_TAG)) {
                            String tag_name = parser.getName();
                            if(eventType == XmlPullParser.START_TAG && nameTag.equalsIgnoreCase(tag_name)) {
                                product.name =  parser.nextText();
                            }
                            eventType = parser.next();
                        }
                    }else if(TAG_DESC.equalsIgnoreCase(tag)) {
                        while(!(TAG_DESC.equalsIgnoreCase(tag) && eventType == XmlPullParser.END_TAG)) {
                            String tag_des = parser.getName();
                            if(eventType == XmlPullParser.START_TAG && nameTag.equalsIgnoreCase(tag_des)) {
                                product.description =  parser.nextText();
                            }
                            eventType = parser.next();
                        }
                    }else if(TAG_VER_NAME.equalsIgnoreCase(tag)) {
                        while(!(TAG_VER_NAME.equalsIgnoreCase(tag) && eventType == XmlPullParser.END_TAG)) {
                            String tag_ver = parser.getName();
                            if(eventType == XmlPullParser.START_TAG && nameTag.equalsIgnoreCase(tag_ver)) {
                                product.version_name =  parser.nextText();
                            }
                            eventType = parser.next();
                        }
                    }else if(TAG_PKG_NAME.equalsIgnoreCase(tag)) {
                        product.app_package =  parser.nextText();
                    }else if(TAG_CATEGORY.equalsIgnoreCase(tag)) {
                        product.type =  parser.nextText();
                    }else if(AUTHOR_EMAIL_TAG.equalsIgnoreCase(tag)) {
                        product.author_email =  parser.nextText();
                    }else if(TAG_AUTHOR.equalsIgnoreCase(tag)) {
                        product.author =  parser.nextText();
                    }else if(TAG_LOGO.equalsIgnoreCase(tag)) {
                        product.logo_url =  parser.nextText();
                        if (!TextUtils.isEmpty(product.logo_url)) {
                            product.logo_url = Uri.fromFile(new File(file.getAbsolutePath() + File.separator + product.logo_url)).toString();
                        }
                    }else if(TAG_COVER.equalsIgnoreCase(tag)) {
                        product.cover_url =  parser.nextText();
                        if (!TextUtils.isEmpty(product.cover_url)) {
                            product.cover_url = Uri.fromFile(new File(file.getAbsolutePath() + File.separator + product.cover_url)).toString();
                        }
                    }else if(TAG_VERSION.equalsIgnoreCase(tag)) {
                        product.version_code =  Integer.parseInt(parser.nextText());
                    }else if(SCREENSHOT1_TAG.equalsIgnoreCase(tag) ||
                            SCREENSHOT2_TAG.equalsIgnoreCase(tag) ||
                            SCREENSHOT3_TAG.equalsIgnoreCase(tag) ||
                            SCREENSHOT4_TAG.equalsIgnoreCase(tag) ||
                            SCREENSHOT5_TAG.equalsIgnoreCase(tag)) {
                        String url = parser.nextText();
                        if (!TextUtils.isEmpty(url)) {
                            product.screenshots.add(Uri.fromFile(new File(file.getAbsolutePath() + File.separator + url)).toString());
                        }
                    }else if(SUPPORTED_MOD_TAG.endsWith(tag)) {
                        product.supported_mod = parser.nextText();
                    }
                    break;
                }
                }
               eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(product != null && TextUtils.isEmpty(product.supported_mod) && ProductType.WALL_PAPER.equals(product.type)) {
            product.supported_mod = SupportedMod.LANDSCAPE;
        }
        return product;
    }

    public static String test(File file) {
        /** Pull方式，解析 XML  */
        StringWriter xmlWriter = new StringWriter();

        InputStream is = null;
        try {
            is = new FileInputStream(file);
//          // 方式一：使用Android提供的实用工具类android.util.Xml
//          XmlPullParser xpp = Xml.newPullParser();

            // 方式二：使用工厂类XmlPullParserFactory的方式
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(is, "utf-8");

            List<RawPaperItem> itemList = null;      // 保存xml的person节点
            RawPaperItem item = null;
            StringBuffer xmlHeader = null;          // 保存xml头部
            String ele = null;      // Element flag

            int eventType = xpp.getEventType();
            while(XmlPullParser.END_DOCUMENT != eventType) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        itemList = new ArrayList<RawPaperItem>();        // 初始化persons
                        xmlHeader = new StringBuffer();             // 初始化xmlHeader
                        break;

                    case XmlPullParser.START_TAG:
                        if(TAG_PAPERS.equals(xpp.getName())) {
//                            String attrAuthor = xpp.getAttributeValue(0);
//                            String attrDate = xpp.getAttributeValue(1);
                            xmlHeader.append(TAG_PAPERS).append("\t\t");
//                            xmlHeader.append(attrAuthor).append("\t");
//                            xmlHeader.append(attrDate).append("\n");
                        } else if(TAG_PAPER_ITEM.equals(xpp.getName())) {
                            item = new RawPaperItem("", "", "", 1, 1);          // 创建person实例
                        } else if(TAG_PAPER_NAME.equals(xpp.getName())) {
                            ele = TAG_PAPER_NAME;
                        } else if(TAG_PAPER_TYPE.equals(xpp.getName())) {
                            ele = TAG_PAPER_TYPE;
//                        } else if (TAG_PAPER_PATH_DEFAULT.equalsIgnoreCase(xpp.getName())) {
//                            ele = TAG_PAPER_PATH_DEFAULT;
//                        } else if (TAG_PAPER_PATH_OVERLAY.equalsIgnoreCase(xpp.getName())) {
//                            ele = TAG_PAPER_PATH_OVERLAY;
                        } else if (TAG_PAPER_PATH_DECODED.equalsIgnoreCase(xpp.getName())) {
                            ele = TAG_PAPER_PATH_DECODED;
                        } else {
                            ele = null;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if(null != ele) {
                            if(TAG_PAPER_NAME.equals(ele)) {
                                item.mKeyName = xpp.getText();
                            } else if(TAG_PAPER_TYPE.equals(ele)) {
                                item.mType = xpp.getText();
//                            } else if (TAG_PAPER_PATH_DEFAULT.equals(ele)) {
//                                item.mDefaultValue = xpp.getText();
//                            } else if (TAG_PAPER_PATH_OVERLAY.equals(ele)) {
//                                item.mOverlayValue = xpp.getText();
                            } else if (TAG_PAPER_PATH_DECODED.equals(ele)) {
                                item.mImagePath = xpp.getText();
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if(TAG_PAPER_ITEM.equals(xpp.getName())){
                            itemList.add(item);
                            item = null;
                        }
                        ele = null;
                        break;
                }

                eventType = xpp.next();     // 下一个事件类型
            }

            xmlWriter.append(xmlHeader);
            int paperSize = itemList.size();
            for(int i=0; i<paperSize; i++) {
                xmlWriter.append(itemList.get(i).toString());
            }

        } catch (XmlPullParserException e) {        // XmlPullParserFactory.newInstance
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return xmlWriter.toString();
    }
}
