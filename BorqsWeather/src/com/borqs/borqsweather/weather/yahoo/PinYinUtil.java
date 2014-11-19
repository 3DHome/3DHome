/*
 * Copyright (C) 2007-2011 Borqs Ltd. 
 * All rights reserved. ( http://www.borqs.com/ )
 */

package com.borqs.borqsweather.weather.yahoo;

import java.util.LinkedList;
import java.util.List;

public class PinYinUtil {
    public static final String KEY_KEYWORDS = "key_keywords";

    public static boolean isChinese(char ch) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(ch);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
            return true;
        }
        return false;
    }

    // modify by xiaofei for polyphone
    public static List<String> getPinYin(char ch) {
        return toPinYin(ch);
    }

    public static String getPinYinWithFirstLetter(String str) {
        String strPinYin = "";
        int i = 0;
        int pos = -1;
        int polyphoneNum = 0;
        String t;

        while (true) {
            if (i >= str.length()) {
                if (pos != -1) {
                    t = str.substring(pos, i);
                    if (t.length() > 0) {
                        //list.add(t);
                        //char[] stringArray = t.toCharArray();
                        //strPinYin +=stringArray[0];
                        strPinYin += t;
                    }
                    pos = -1;
                }
                break;
            }
            char ch = str.charAt(i);
            if (isChinese(ch)) {
                if (pos != -1) {
                    t = str.substring(pos, i);
                    if (t.length() > 0) {
                        //list.add(t);
                        //char[] stringArray = t.toCharArray();
                        //strPinYin +=stringArray[0];
                        strPinYin += t;
                    }
                    pos = -1;
                }

                List<String> pinyin = getPinYin(ch);

                if (pinyin != null) {
                    String pystr = "";
                    // when polyphone number more than 2 in the str, will only
                    // deal with the first two
                    if (1 < pinyin.size())
                        polyphoneNum++;

                    if (2 < polyphoneNum) {
                        pystr = pinyin.get(0);
                    } else {
                        if (1 < pinyin.size()) {
                            pystr += pinyin.get(1);
                            pystr += ",";
                            pystr += pinyin.get(0);
                        } else
                            pystr = pinyin.get(0);
                        /*
                         * if (pinyin.get(pinyin.size() - 1) != null &&
                         * pinyin.get(pinyin.size() - 1).length() > 0) pystr +=
                         * pinyin.get(pinyin.size() - 1); for (int j =
                         * pinyin.size() - 2; j >= 0; j--) { if (pinyin.get(j)
                         * != null && pinyin.get(j).length() > 0) { pystr +=
                         * ","; pystr += pinyin.get(j); } }
                         */
                    }
                    if (pystr.length() > 0) {
                        //list.add(pystr);
                        char[] stringArray = pystr.toCharArray();
                        strPinYin +=stringArray[0];
                    }
                }
                i++;
                continue;
            } else if (Character.isSpaceChar(ch)) {
                if (pos != -1) {
                    //when use first letter search, space can't be ignore.
                    t = str.substring(pos, i + 1);
                    if (t.length() > 0) {
                        //list.add(t);
                        //char[] stringArray = t.toCharArray();
                        //strPinYin +=stringArray[0];
                        strPinYin +=t;
                    }
                    pos = -1;
                }
                i++;
                continue;
            } else if ((0 < i) && ((Character.isDigit(ch)
                    && isEnglishLetter(str.charAt(i - 1)))
                    || (isEnglishLetter(ch)
                    && Character.isDigit(str.charAt(i - 1))))
                    && (pos != -1)) {
                t = str.substring(pos, i);
                if (t.length() > 0) {
                    //list.add(t);
                    //char[] stringArray = t.toCharArray();
                    //strPinYin +=stringArray[0];
                    strPinYin += t;
                }
                pos = -1;
                continue;
            } else {
                if (pos == -1) {
                    pos = i;
                }
                i++;
                continue;
            }
        }
        return strPinYin;
    }

    public static List<String> getOriginName(String str) {
        LinkedList<String> list = new LinkedList<String>();
        int i = 0;
        int pos = -1;
        int polyphoneNum = 0;
        String t;

        while (true) {
            if (i >= str.length()) {
                if (pos != -1) {
                    t = str.substring(pos, i);
                    if (t.length() > 0) {
                        list.add(t);
                    }
                    pos = -1;
                }
                break;
            }
            char ch = str.charAt(i);

            if (isChinese(ch)) {
                if (pos != -1) {
                    t = str.substring(pos, i);
                    if (t.length() > 0) {
                        list.add(t);
                    }
                    pos = -1;
                }

                List<String> pinyin = getPinYin(ch);

                if (pinyin != null) {
                    String pystr = "";
                    // when polyphone number more than 2 in the str, will only
                    // deal with the first two
                    if (1 < pinyin.size())
                        polyphoneNum++;

                    if (2 < polyphoneNum) {
                        pystr = pinyin.get(0);
                    } else {
                        if (1 < pinyin.size()) {
                            pystr += pinyin.get(1);
                            pystr += ",";
                            pystr += pinyin.get(0);
                        } else
                            pystr = pinyin.get(0);
                        /*
                         * if (pinyin.get(pinyin.size() - 1) != null &&
                         * pinyin.get(pinyin.size() - 1).length() > 0) pystr +=
                         * pinyin.get(pinyin.size() - 1); for (int j =
                         * pinyin.size() - 2; j >= 0; j--) { if (pinyin.get(j)
                         * != null && pinyin.get(j).length() > 0) { pystr +=
                         * ","; pystr += pinyin.get(j); } }
                         */
                    }
                    if (pystr.length() > 0) {
                        //list.add(pystr);
                        list.add(Character.toString(ch));
                    }
                }
                i++;
                continue;
            } else if (Character.isSpaceChar(ch)) {
                if (pos != -1) {
                    //we need space for calculate the correct position when use in full pinyin search.
                    t = str.substring(pos, i + 1);
                    if (t.length() > 0) {
                        list.add(t);
                    }
                    pos = -1;
                }
                i++;
                continue;
            } else if ((0 < i) && ((Character.isDigit(ch)
                    && isEnglishLetter(str.charAt(i - 1)))
                    || (isEnglishLetter(ch)
                    && Character.isDigit(str.charAt(i - 1))))
                    && (pos != -1)) {
                t = str.substring(pos, i);
                if (t.length() > 0) {
                    list.add(t);
                }
                pos = -1;
                continue;
            } else {
                if (pos == -1) {
                    pos = i;
                }
                i++;
                continue;
            }

        }
        return list;
    }

    public static List<String> getPinYin(String str) {
        LinkedList<String> list = new LinkedList<String>();
        int i = 0;
        int pos = -1;
        int polyphoneNum = 0;
        String t;

        while (true) {
            if (i >= str.length()) {
                if (pos != -1) {
                    t = str.substring(pos, i);
                    if (t.length() > 0) {
                        list.add(t);
                    }
                    pos = -1;
                }
                break;
            }
            char ch = str.charAt(i);

            if (isChinese(ch)) {
                if (pos != -1) {
                    t = str.substring(pos, i);
                    if (t.length() > 0) {
                        list.add(t);
                    }
                    pos = -1;
                }

                List<String> pinyin = getPinYin(ch);

                if (pinyin != null) {
                    String pystr = "";
                    // when polyphone number more than 2 in the str, will only
                    // deal with the first two
                    if (1 < pinyin.size())
                        polyphoneNum++;

                    if (2 < polyphoneNum) {
                        pystr = pinyin.get(0);
                    } else {
                        if (1 < pinyin.size()) {
                            pystr += pinyin.get(1);
                            pystr += ",";
                            pystr += pinyin.get(0);
                        } else
                            pystr = pinyin.get(0);
                        /*
                         * if (pinyin.get(pinyin.size() - 1) != null &&
                         * pinyin.get(pinyin.size() - 1).length() > 0) pystr +=
                         * pinyin.get(pinyin.size() - 1); for (int j =
                         * pinyin.size() - 2; j >= 0; j--) { if (pinyin.get(j)
                         * != null && pinyin.get(j).length() > 0) { pystr +=
                         * ","; pystr += pinyin.get(j); } }
                         */
                    }
                    if (pystr.length() > 0)
                        list.add(pystr);
                }
                i++;
                continue;
            } else if (Character.isSpaceChar(ch)) {
                if (pos != -1) {
                    t = str.substring(pos, i);
                    if (t.length() > 0) {
                        list.add(t);
                    }
                    pos = -1;
                }
                i++;
                continue;
            } else if ((0 < i) && ((Character.isDigit(ch)
                    && isEnglishLetter(str.charAt(i - 1)))
                    || (isEnglishLetter(ch)
                    && Character.isDigit(str.charAt(i - 1))))
                    && (pos != -1)) {
                t = str.substring(pos, i);
                if (t.length() > 0) {
                    list.add(t);
                }
                pos = -1;
                continue;
            } else {
                if (pos == -1) {
                    pos = i;
                }
                i++;
                continue;
            }

        }
        return list;
    }

    /*
     * public static String getIndexPinyinContent(String str) { List<String>
     * list = getPinYin(str); StringBuilder builder = new StringBuilder();
     * String fullPinyin = ""; String firstChar = ""; for (int i = list.size() -
     * 1; i >= 0; i--) { fullPinyin = list.get(i) + fullPinyin; firstChar =
     * list.get(i).charAt(0) + firstChar; builder.append(fullPinyin);
     * builder.append(','); builder.append(firstChar); builder.append(','); }
     * return builder.toString(); }
     */

    public static String getIndexPinyinContent(String str) {
        List<String> list = getPinYin(str);
        StringBuilder builder = new StringBuilder();
        LinkedList<String> fullPinyin = new LinkedList<String>();
        LinkedList<String> fullPinyinSwap = new LinkedList<String>();
        LinkedList<String> firstChar = new LinkedList<String>();
        LinkedList<String> firstCharSwap = new LinkedList<String>();
        LinkedList<String> switcher = null;
        fullPinyin.add("");
        firstChar.add("");

        for (int i = list.size() - 1; i >= 0; i--) {
            String[] words = list.get(i).split(",");
            for (int j = 0; j < fullPinyin.size(); j++) {
                for (int k = 0; k < words.length; k++) {
                    String rslt = words[k] + fullPinyin.get(j);
                    fullPinyinSwap.add(rslt);
                    builder.append(rslt);
                    builder.append(',');
                }
            }
            // switch list
            switcher = fullPinyin;
            switcher.clear();
            fullPinyin = fullPinyinSwap;
            fullPinyinSwap = switcher;

            for (int j = 0; j < firstChar.size(); j++) {
                for (int k = 0; k < words.length; k++) {
                    if (0 == words[k].length())
                        continue;
                    String rslt = words[k].charAt(0) + firstChar.get(j);
                    firstCharSwap.add(rslt);
                    builder.append(rslt);
                    builder.append(',');
                }
            }
            // switch list
            switcher = firstChar;
            switcher.clear();
            firstChar = firstCharSwap;
            firstCharSwap = switcher;
        }
        // if (ArtSearchUtil.LOGD)
        // LoggerFactory.logger.info(PinYinUtil.class, "getIndexPinyinContent:"
        // + str + ":"+builder.toString());
        return builder.toString();
    }

    public static List<String> getFullName(String str) {
        List<String> list = getOriginName(str);
        LinkedList<String> fullPinyin = new LinkedList<String>();
        LinkedList<String> fullPinyinSwap = new LinkedList<String>();
        LinkedList<String> switcher = null;
        fullPinyin.add("");

        for (int i = list.size() - 1; i >= 0; i--) {
            String[] words = list.get(i).split(",");
            for (int j = 0; j < fullPinyin.size(); j++) {
                for (int k = 0; k < words.length; k++) {
                    String rslt = words[k] + "," + fullPinyin.get(j);
                    fullPinyinSwap.add(rslt);
                }
            }

            // switch list
            switcher = fullPinyin;
            switcher.clear();
            fullPinyin = fullPinyinSwap;
            fullPinyinSwap = switcher;
        }
        return fullPinyin;

    }

    public static List<String> getFullPinyin(String str) {
        List<String> list = getPinYin(str);
        LinkedList<String> fullPinyin = new LinkedList<String>();
        LinkedList<String> fullPinyinSwap = new LinkedList<String>();
        LinkedList<String> switcher = null;
        fullPinyin.add("");

        for (int i = list.size() - 1; i >= 0; i--) {
            String[] words = list.get(i).split(",");
            for (int j = 0; j < fullPinyin.size(); j++) {
                for (int k = 0; k < words.length; k++) {
                    String rslt = words[k] + "," + fullPinyin.get(j);
                    fullPinyinSwap.add(rslt);
                }
            }

            // switch list
            switcher = fullPinyin;
            switcher.clear();
            fullPinyin = fullPinyinSwap;
            fullPinyinSwap = switcher;
        }
        return fullPinyin;

    }

    public static List<String> toPinYin(char ch) {
        int i = 0;
        boolean flag = false;
        LinkedList<String> result = new LinkedList<String>();
        HanziToPinyin.Token token = HanziToPinyin.getInstance().getToken(ch);
        for (i = 0; i < token.target.length; i++) {
            if (null != token.target[i]) {
                flag = true;
                result.add(token.target[i]);
            }
        }
        if (!flag)
            return null;

        return result;
    }

    // end by xiaofei

    private static boolean isEnglishLetter(char ch) {
        if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))
            return true;
        return false;
    }

    public static boolean isEnglishLetterOrDigit(char ch) {
        if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
                || (ch >= '0' && ch <= '9'))
            return true;
        return false;
    }

}
