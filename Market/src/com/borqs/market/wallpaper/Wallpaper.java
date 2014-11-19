package com.borqs.market.wallpaper;

import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.borqs.market.json.Product;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by b608 on 13-8-1.
 */
public class Wallpaper {
    private static final int INIT_VERSION = 0;
    private static final String INIT_CHANGE = "Init share wallpaper.";
    private static final String TAG = "Wallpaper";

    String hostObject; // the name of the object to host this wallpaper
    String hostPackageName; // the name of package that host this wallpaper
    String cloudUrl; // the remote url to publish to or retrieve from
    int appVersion;

    String userName;
    String accountId;

    String title;
    String summary;
    String description;
    String website;
    String email;

    PaperOrientation paperOrientation; // the wallpaper export for landscape, portrait or both.

    PaperImageFormat paperFormat; // the format of wallpaper which will resize/map/scale raw bitmap
    ArrayList paperUnits; // the list of wall paper of each unit (wall, floor, or everything else)
    ArrayList screenShot; // the names list of screenshot images.

    String versionName;

    public Wallpaper() {
    }

    public static class PaperOrientation {
        // 0 - none, 1 - portrait only, 2 - landscape only
        public static final int NONE = 0;
        public static final int PORT = 1;
        public static final int LAND = 2;
        public int orientation;
    }

    public static class PaperImageFormat {
        int edgeLen; // as render requirements of some GPU, it will be round to power of 2
        int paddingLeft; // left/top/right/bottom padding to convert raw bitmap as expected texture bitmap
        int paddingTop;
        int paddingRight;
        int paddingBottom;
        public PaperImageFormat() {
            edgeLen = 1024;
            paddingLeft = 0;
            paddingTop = 0;
            paddingRight = 0;
            paddingBottom = 0;
        }
    }

    /// A unit item of wallpaper package, which
/// map a name with a image file path.
    public static class PaperUnit extends HashMap<String, String> {
    };

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

    private static void serializeXmlTag(XmlSerializer serializer, String name, String value) throws IOException {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(value)) {
            Log.i(TAG, "serializeXmlTag, skip invalid name/value " + name + "/" + value);
            return;
        }
        serializer.startTag(null, name);
        serializer.text(value);
        serializer.endTag(null, name);
    }
    private static void serializeXmlLangTag(XmlSerializer serializer, String topName, String value) throws IOException {
        serializeXmlLangTag(serializer, topName, TAG_EN, value);
    }
    private static void serializeXmlLangTag(XmlSerializer serializer, String topName, String name, String value) throws IOException {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(value)) {
            Log.i(TAG, "serializeXmlTag, skip invalid name/value " + name + "/" + value);
            return;
        }
        serializer.startTag(null, topName);
        serializer.startTag(null, TAG_LANG);
        serializer.startTag(null, name);
        serializer.text(value);
        serializer.endTag(null, name);
        serializer.endTag(null, TAG_LANG);
        serializer.endTag(null, topName);
    }

    public static class Builder {
        String hostObject; // the name of the object to host this wallpaper
        String hostPackageName; // the name of package that host this wallpaper
        String cloudUrl; // the remote url to publish to or retrieve from
        int appVersion;

        String userName;
        String accountId;

        String title;
        String summary;
        String description;
        String website;
        String email;

        PaperOrientation paperOrientation; // the wallpaper export for landscape, portrait or both.

        PaperImageFormat paperFormat; // the format of wallpaper which will resize/map/scale raw bitmap
        ArrayList paperUnits; // the list of wall paper of each unit (wall, floor, or everything else)
        ArrayList screenShot; // the names list of screenshot images.

        String versionName;

        public Builder(String id) {
            accountId = id;
        }
        public Builder setObjectName(String name) {
            hostObject = name;
            return this;
        }
        public Builder setPackageName(String name) {
            hostPackageName = name;
            return this;
        }
        public Builder setAppVersion(int version) {
            appVersion = version;
            return this;
        }
        public Builder setUrl(String url) {
            cloudUrl = url;
            return this;
        }
        public Builder setUserName(String name) {
            userName = name;
            return this;
        }
        public Builder setTitle(String t) {
            title = t;
            return this;
        }
        public Builder setSummary(String s) {
            summary = s;
            return this;
        }
        public Builder setDescription(String d) {
            description = d;
            return this;
        }
        public Builder setWebsite(String web) {
            website = web;
            return this;
        }
        public Builder setEmail(String e) {
            email = e;
            return this;
        }
        public Builder setOrientation(PaperOrientation o) {
            paperOrientation = o;
            return this;
        }
        public Builder setFormat(PaperImageFormat format) {
            paperFormat = format;
            return this;
        }
        public Builder setPapers(ArrayList<RawPaperItem> units) {
            paperUnits = units;
            return this;
        }
        public Builder setScreenShot(ArrayList<String> screens) {
            screenShot = screens;
            return this;
        }

        public Builder setVersionName(String verName) {
            verName = verName;
            return this;
        }

        public Wallpaper create() {
            Wallpaper w = new Wallpaper();
            w.accountId = TextUtils.isEmpty(accountId) ? "INVALID" : accountId;
            w.userName = userName;
            w.appVersion = appVersion;

            w.hostObject = hostObject;
            w.hostPackageName = hostPackageName;
            w.cloudUrl = cloudUrl;

            w.title = title;
            w.summary = summary;
            w.description = description;
            w.website = website;
            w.email = email;

            w.paperOrientation = paperOrientation;
            w.paperFormat = paperFormat;
            w.paperUnits = paperUnits;
            w.screenShot = screenShot;
            return w;
        }

        private static final String START_TAG = "BorqsResource";
        public void buildManifest(File manifestFile) throws Exception {
            FileOutputStream outputStream = new FileOutputStream(manifestFile);
            XmlSerializer serializer= Xml.newSerializer();
            serializer.setOutput(outputStream, "utf-8");

            serializer.startDocument("UTF-8", true);
            //第一个参数为命名空间,如果不使用命名空间,可以设置为null
            serializer.startTag(null, START_TAG);
            serializeXmlTags(serializer);
            serializer.endTag(null, START_TAG);
            serializer.endDocument();
            outputStream.close();
        }

        private void serializeXmlTags(XmlSerializer serializer) throws IOException {
            serializeXmlTag(serializer, TAG_ID, generateIdentifier());
            serializeXmlTag(serializer, TAG_PKG_NAME, hostPackageName);
            serializeXmlTag(serializer, TAG_CATEGORY, Product.ProductType.WALL_PAPER);

            serializeXmlLangTag(serializer, TAG_NAME, title);
            serializeXmlLangTag(serializer, TAG_DESC, description);

            serializeXmlTag(serializer, TAG_AUTHOR, userName);
            // todo: fill these later
//        serializeXmlTag(serializer, TAG_LOGO, );
//        serializeXmlTag(serializer, TAG_COVER, );
//        int i = 0;
//        for (Object str : screenShot) {
//            i++;
//            serializeXmlTag(serializer, getScreenShotTag(i), (String)str);
//        }
            serializeXmlTag(serializer, TAG_VERSION, String.valueOf(INIT_VERSION));
            serializeXmlTag(serializer, TAG_APP_VERSION, String.valueOf(appVersion));
            serializeXmlTag(serializer, TAG_VER_NAME, TextUtils.isEmpty(versionName) ? "V" + INIT_VERSION : versionName);
            serializeXmlTag(serializer, TAG_RECENT_CHANGE, INIT_CHANGE);

            serializeXmlPaperItemTag(serializer);
        }

        private void serializeXmlPaperItemTag(XmlSerializer serializer)  throws IOException {
            if (null == paperUnits || paperUnits.isEmpty()) {
                return;
            }

            ArrayList<String> tmpImageList = new ArrayList<String>();

            serializer.startTag(null, TAG_PAPERS);
            ArrayList<RawPaperItem> reversedList = new ArrayList<RawPaperItem>();
            reversedList.addAll(paperUnits);
            Collections.reverse(reversedList);
            for (Object item : reversedList) {
                if (item instanceof RawPaperItem) {
                    RawPaperItem paperItem = (RawPaperItem)item;
                    serializer.startTag(null, TAG_PAPER_ITEM);
                    serializeXmlTag(serializer, TAG_PAPER_NAME, paperItem.mKeyName);
                    serializeXmlTag(serializer, TAG_PAPER_TYPE, paperItem.mType);
                    serializeXmlTag(serializer, TAG_PAPER_PATH_DECODED, paperItem.mImagePath);
                    serializer.endTag(null, TAG_PAPER_ITEM);

                    tmpImageList.add(0, paperItem.mImagePath);
                } else {
                    Log.i(TAG, "serializeXmlPaperItemTag, unexpected item type " + item.getClass().getName());
                }
            }
            serializer.endTag(null, TAG_PAPERS);

            postSerializeXmlScreenShot(serializer, tmpImageList);
        }

        private void postSerializeXmlScreenShot(XmlSerializer serializer, ArrayList<String> tmpImageList) throws IOException {
            final boolean asScreenShot = screenShot == null || screenShot.isEmpty();
            if (asScreenShot) {
                if (null != tmpImageList && !tmpImageList.isEmpty()) {
                    for (int i = 0; i < tmpImageList.size(); i++) {
                        serializePaperScreenShot(serializer, i, getBaseName(tmpImageList.get(i)));
                    }
                    final String image = getBaseName(tmpImageList.get(0));
                    serializeXmlTag(serializer, LOGO, image);
                    serializeXmlTag(serializer, COVER, image);
                }
            } else {
                for (int i = 0; i < screenShot.size(); i ++) {
                    serializePaperScreenShot(serializer, i , getBaseName((String)screenShot.get(i)));
                }
                final String image = getBaseName((String)screenShot.get(0));
                serializeXmlTag(serializer, LOGO, image);
                serializeXmlTag(serializer, COVER, image);
            }
        }

        private void serializePaperScreenShot(XmlSerializer serializer, int i, String screenShot)   throws IOException {
            serializeXmlTag(serializer, getScreenShotTag(i), screenShot);
        }

        private static String getBaseName(String fileName) {
            if (TextUtils.isEmpty(fileName)) {
                return fileName;
            }
            return fileName.substring(fileName.lastIndexOf(File.separator) + 1);
        }

        private static final String ID_PREFIX = "USP_pp_";
        private String generateIdentifier() {
            return ID_PREFIX + hostPackageName + System.currentTimeMillis() + generateSuffix(4);
        }

        private String generateSuffix(int len) {
            double factor = Math.pow(10, len);
            double seed = factor + Math.random() * factor;
            String value =  String.valueOf((int)(seed));
            return value.substring(1);
        }

        private static final String SCREEN_SHOT_PREFIX = "screenshot";
//        private static final String IMG_SUFFIX = "_image";
        private static final String LOGO = "logo";
        private static final String COVER = "cover";
        private static String getScreenShotTag(int i) {
//            return SCREEN_SHOT_PREFIX + i + IMG_SUFFIX;
            return SCREEN_SHOT_PREFIX + i;
        }

        public static Builder createPaperUnitFromFile(File file) {
            Builder builder = null;
            InputStream is = null;
            try {
                is = new FileInputStream(file);
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(is, "utf-8");

                ArrayList<RawPaperItem> itemList = null;
                RawPaperItem item = null;
                String ele = null;

                int eventType = xpp.getEventType();
                while(XmlPullParser.END_DOCUMENT != eventType) {
                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            itemList = new ArrayList<RawPaperItem>();
                            break;

                        case XmlPullParser.START_TAG:
                            if(TAG_PAPERS.equals(xpp.getName())) {
                            } else if(TAG_PAPER_ITEM.equals(xpp.getName())) {
                                item = new RawPaperItem("", "", "", 1, 1);
                            } else if(TAG_PAPER_NAME.equals(xpp.getName())) {
                                ele = TAG_PAPER_NAME;
                            } else if(TAG_PAPER_TYPE.equals(xpp.getName())) {
                                ele = TAG_PAPER_TYPE;
//                            } else if (TAG_PAPER_PATH_DEFAULT.equalsIgnoreCase(xpp.getName())) {
//                                ele = TAG_PAPER_PATH_DEFAULT;
//                            } else if (TAG_PAPER_PATH_OVERLAY.equalsIgnoreCase(xpp.getName())) {
//                                ele = TAG_PAPER_PATH_OVERLAY;
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
//                                } else if (TAG_PAPER_PATH_DEFAULT.equals(ele)) {
//                                    item.mDefaultValue = xpp.getText();
//                                } else if (TAG_PAPER_PATH_OVERLAY.equals(ele)) {
//                                    item.mOverlayValue = xpp.getText();
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

                    eventType = xpp.next();
                }

                builder = new Builder("");
                builder.paperUnits = itemList;
            } catch (XmlPullParserException e) {
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

            return builder;
        }
    }
}
