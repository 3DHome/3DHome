package com.borqs.market.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import com.borqs.market.db.DownLoadProvider;
import com.borqs.market.db.PlugInColumns;
import com.borqs.market.json.Product;
import com.borqs.market.json.Product.ProductType;
import com.borqs.market.json.Product.SupportedMod;

public class ThemeXmlParser {
    private static final String ID_TAG = "id";
    private static final String APP_TAG = "app";
    private static final String CATEGORY_TAG = "category";
    private static final String AUTHOR_NAME_TAG = "authorName";
    private static final String AUTHOR_EMAIL_TAG = "authorEmail";
    private static final String NAME_TAG = "name";
    private static final String DESCRIPTION_TAG = "description";
    private static final String EN_TAG = "en_US";
    private static final String ZH_TAG = "zh_CN";
    private static final String LOGO_TAG = "logo";
    private static final String COVER_TAG = "cover";
    private static final String VERSION_TAG = "version";
    private static final String VERSION_NAME_TAG = "versionName";
    private static final String SCREENSHOT1_TAG = "screenshot1";
    private static final String SCREENSHOT2_TAG = "screenshot2";
    private static final String SCREENSHOT3_TAG = "screenshot3";
    private static final String SCREENSHOT4_TAG = "screenshot4";
    private static final String SCREENSHOT5_TAG = "screenshot5";
    private static final String SUPPORTED_MOD_TAG = "supportedMod";
//    <name>
//      <langs>
//        5857
//      </langs>
//    </name>
//    <description>
//      <langs>
//        <en_US></en_US>
//        <zh_CN></zh_CN>
//      </langs>
//    </description>
//    <maxAppVC>10000</maxAppVC>
//    <versionName>
//      <langs>
//        <en_US>2.0</en_US>
//        <zh_CN></zh_CN>
//      </langs>
//    </versionName>
//    <recentChange>
//      <langs>
//        <en_US></en_US>
//        <zh_CN></zh_CN>
//      </langs>
//    </recentChange>
    
    public static Product parserAssert(Context context, String assertPath) {
        String nameTag = EN_TAG;
        Product product = null;
        XmlPullParserFactory factory;
        try {
            product = new Product();
            product.installed_file_path = "asset://" + assertPath;
            product.screenshots = new ArrayList<String>();
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);  
            XmlPullParser parser = factory.newPullParser(); 
//            InputStream stream = new FileInputStream(configFile);
            InputStream stream = context.getResources().getAssets().open(assertPath + File.separator + MarketUtils.MARKET_CONFIG_FILE_NAME);  
            if(stream == null) return null;
            parser.setInput(stream,  "UTF-8");
            int eventType = parser.getEventType();
            while(eventType != XmlPullParser.END_DOCUMENT) {
                switch(eventType) {
                case XmlPullParser.START_TAG: {
                    String tag = parser.getName();
                    if(ID_TAG.equalsIgnoreCase(tag)) {
                        product.product_id =  parser.nextText();
                    }else if(NAME_TAG.equalsIgnoreCase(tag)) {
                        while(!(NAME_TAG.equalsIgnoreCase(tag) && eventType == XmlPullParser.END_TAG)) {
                            String tag_name = parser.getName();
                            if(eventType == XmlPullParser.START_TAG && nameTag.equalsIgnoreCase(tag_name)) {
                                product.name =  parser.nextText();
                            }
                            eventType = parser.next();
                        }
                    }else if(DESCRIPTION_TAG.equalsIgnoreCase(tag)) {
                        while(!(DESCRIPTION_TAG.equalsIgnoreCase(tag) && eventType == XmlPullParser.END_TAG)) {
                            String tag_des = parser.getName();
                            if(eventType == XmlPullParser.START_TAG && nameTag.equalsIgnoreCase(tag_des)) {
                                product.description =  parser.nextText();
                            }
                            eventType = parser.next();
                        }
                    }else if(VERSION_NAME_TAG.equalsIgnoreCase(tag)) {
                        while(!(VERSION_NAME_TAG.equalsIgnoreCase(tag) && eventType == XmlPullParser.END_TAG)) {
                            String tag_ver = parser.getName();
                            if(eventType == XmlPullParser.START_TAG && nameTag.equalsIgnoreCase(tag_ver)) {
                                product.version_name =  parser.nextText();
                            }
                            eventType = parser.next();
                        }
                    }else if(APP_TAG.equalsIgnoreCase(tag)) {
                        product.app_package =  parser.nextText();
                    }else if(CATEGORY_TAG.equalsIgnoreCase(tag)) {
                        product.type =  parser.nextText();
                    }else if(AUTHOR_EMAIL_TAG.equalsIgnoreCase(tag)) {
                        product.author_email =  parser.nextText();
                    }else if(AUTHOR_NAME_TAG.equalsIgnoreCase(tag)) {
                        product.author =  parser.nextText();
                    }else if(LOGO_TAG.equalsIgnoreCase(tag)) {
                        product.logo_url =  parser.nextText();
                        if (!TextUtils.isEmpty(product.logo_url)) {
                            product.logo_url = getAssetPath(assertPath, product.logo_url);
                        }
                    }else if(COVER_TAG.equalsIgnoreCase(tag)) {
                        product.cover_url =  parser.nextText();
                        if (!TextUtils.isEmpty(product.cover_url)) {
                            product.cover_url = getAssetPath(assertPath, product.cover_url);
                        }
                    }else if(VERSION_TAG.equalsIgnoreCase(tag)) {
                        product.version_code =  Integer.parseInt(parser.nextText());
                    }else if(SCREENSHOT1_TAG.equalsIgnoreCase(tag) ||
                            SCREENSHOT2_TAG.equalsIgnoreCase(tag) ||
                            SCREENSHOT3_TAG.equalsIgnoreCase(tag) ||
                            SCREENSHOT4_TAG.equalsIgnoreCase(tag) ||
                            SCREENSHOT5_TAG.equalsIgnoreCase(tag)) {
                        String url = parser.nextText();
                        if (!TextUtils.isEmpty(url)) {
                            product.screenshots.add(getAssetPath(assertPath, url));
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
        if(product != null && TextUtils.isEmpty(product.supported_mod)) {
            product.supported_mod = SupportedMod.LANDSCAPE;
        }
        return product;
    }
    
    private static String getAssetPath(String asserPath, String path) {
    	StringBuilder sb = new StringBuilder("file:///assets/");
    	sb.append(asserPath);
    	sb.append("/");
    	sb.append(path);
    	return sb.toString();
    }
    
    
    public static Product parser(File file) {
        String nameTag = EN_TAG;
//        if("zh_CN".equalsIgnoreCase(Locale.getDefault().toString())) {
//            nameTag = ZH_TAG;
//        }
        File configFile = new File(file.getPath() + File.separator + MarketUtils.MARKET_CONFIG_FILE_NAME);
        if(!configFile.exists()) return null;
        Product product = null;
        XmlPullParserFactory factory;
        try {
            product = new Product();
            product.installed_file_path = file.getPath();
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
                    if(ID_TAG.equalsIgnoreCase(tag)) {
                        product.product_id =  parser.nextText();
                    }else if(NAME_TAG.equalsIgnoreCase(tag)) {
                        while(!(NAME_TAG.equalsIgnoreCase(tag) && eventType == XmlPullParser.END_TAG)) {
                            String tag_name = parser.getName();
                            if(eventType == XmlPullParser.START_TAG && nameTag.equalsIgnoreCase(tag_name)) {
                                product.name =  parser.nextText();
                            }
                            eventType = parser.next();
                        }
                    }else if(DESCRIPTION_TAG.equalsIgnoreCase(tag)) {
                        while(!(DESCRIPTION_TAG.equalsIgnoreCase(tag) && eventType == XmlPullParser.END_TAG)) {
                            String tag_des = parser.getName();
                            if(eventType == XmlPullParser.START_TAG && nameTag.equalsIgnoreCase(tag_des)) {
                                product.description =  parser.nextText();
                            }
                            eventType = parser.next();
                        }
                    }else if(VERSION_NAME_TAG.equalsIgnoreCase(tag)) {
                        while(!(VERSION_NAME_TAG.equalsIgnoreCase(tag) && eventType == XmlPullParser.END_TAG)) {
                            String tag_ver = parser.getName();
                            if(eventType == XmlPullParser.START_TAG && nameTag.equalsIgnoreCase(tag_ver)) {
                                product.version_name =  parser.nextText();
                            }
                            eventType = parser.next();
                        }
                    }else if(APP_TAG.equalsIgnoreCase(tag)) {
                        product.app_package =  parser.nextText();
                    }else if(CATEGORY_TAG.equalsIgnoreCase(tag)) {
                        product.type =  parser.nextText();
                    }else if(AUTHOR_EMAIL_TAG.equalsIgnoreCase(tag)) {
                        product.author_email =  parser.nextText();
                    }else if(AUTHOR_NAME_TAG.equalsIgnoreCase(tag)) {
                        product.author =  parser.nextText();
                    }else if(LOGO_TAG.equalsIgnoreCase(tag)) {
                        product.logo_url =  parser.nextText();
                        if (!TextUtils.isEmpty(product.logo_url)) {
                            product.logo_url = Uri.fromFile(new File(file.getAbsolutePath() + File.separator + product.logo_url)).toString();
                        }
                    }else if(COVER_TAG.equalsIgnoreCase(tag)) {
                        product.cover_url =  parser.nextText();
                        if (!TextUtils.isEmpty(product.cover_url)) {
                            product.cover_url = Uri.fromFile(new File(file.getAbsolutePath() + File.separator + product.cover_url)).toString();
                        }
                    }else if(VERSION_TAG.equalsIgnoreCase(tag)) {
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
    
    public static boolean isExistingFolderPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File root = new File(path);
        return root.exists() && root.isDirectory();
    }
    
    public static void initLocalData(final Context context, final String parentPath) {
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                if (isExistingFolderPath(parentPath)) {
                    List<Product> result = new ArrayList<Product>();
                    File folder = new File(parentPath);
                    File[] files = folder.listFiles();
                    for (File f : files) {
                        parseLocalWallpaper(result, f);
                    }
                    resetWallpaperPlugIn(context);
                    if(result != null && result.size() > 0) {
                        MarketUtils.bulkInsertPlugInFromProduct(context, result);
                    }
                    
                    SharedPreferences sp = MarketConfiguration.getMarketConfigPreferences(context);
                    SharedPreferences.Editor Editor = sp.edit();
                    Editor.putBoolean(MarketConfiguration.SP_EXTRAS_INIT_LOCAL_DATA, true);
                    Editor.commit();
                }
            }
        }).start();
    }
    
    public static int resetWallpaperPlugIn(Context context) {
        String where = PlugInColumns.TYPE + "=?" ;
        return context.getContentResolver().delete(DownLoadProvider.getContentURI(context, DownLoadProvider.TABLE_PLUGIN), where, new String[] {Product.ProductType.WALL_PAPER});
    }
    
    private static void parseLocalWallpaper(List<Product> resultList, File file) {
        if (file != null && resultList != null) {
            if (file.exists() && file.isDirectory()) {
                Product product = parser(file);
                if(product != null) {
                    resultList.add(product);
                }
            }
        }
    }
}
