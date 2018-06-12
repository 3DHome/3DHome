/*
 * Copyright (C) 2011 Borqs Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.borqs.se.home3d;

import android.text.TextUtils;
import android.util.Log;

import com.borqs.borqsweather.weather.WeatherController;

import java.text.Collator;
import java.util.Locale;


/**
 * An object to convert Chinese character to its corresponding pinyin string.
 * For characters with multiple possible pinyin string, only one is selected
 * according to collator. Polyphone is not supported in this implementation.
 * This class is implemented to achieve the best runtime performance and minimum
 * runtime resources with tolerable sacrifice of accuracy. This implementation
 * highly depends on zh_CN ICU collation data and must be always synchronized
 * with ICU.
 */
public class HanziToPinyin {
//     private static final String TAG = "HanziToPinyin";
    /**
     * Unihans array. Each unihans is the first one within same pinyin. Use it
     * to determine pinyin for all ~20k unihans.
     */
    public static final char[] UNIHANS = {
            '\u5475', '\u54ce', '\u5b89', '\u80ae', '\u51f9',
            '\u516b', '\u6300', '\u6273', '\u90a6', '\u5305', '\u5351', '\u5954', '\u4f3b',
            '\u5c44', '\u8fb9', '\u6807', '\u618b', '\u90a0', '\u69df', '\u7676', '\u5cec',
            '\u5693', '\u5a47', '\u98e1', '\u4ed3', '\u64cd', '\u518a', '\u5d7e', '\u564c',
            '\u53c9', '\u9497', '\u8fbf', '\u4f25', '\u6284', '\u8f66', '\u62bb', '\u67fd',
            '\u5403', '\u5145', '\u62bd', '\u51fa', '\u6b3b', '\u63e3', '\u5ddd', '\u75ae',
            '\u5439', '\u6776', '\u9034', '\u75b5', '\u5306', '\u51d1', '\u7c97', '\u6c46',
            '\u5d14', '\u90a8', '\u6413', '\u5491', '\u5927', '\u75b8', '\u5f53', '\u5200',
            '\u6dc2', '\u5f97', '\u6265', '\u706f', '\u6c10', '\u55f2', '\u7538', '\u5201',
            '\u7239', '\u4ec3', '\u4e1f', '\u4e1c', '\u5517', '\u561f', '\u5073', '\u5806',
            '\u9413', '\u591a', '\u5a40', '\u8bf6', '\u5940', '\u97a5', '\u800c', '\u53d1',
            '\u5e06', '\u65b9', '\u98de', '\u5206', '\u4e30', '\u8985', '\u4ecf', '\u7d11',
            '\u4f15', '\u65ee', '\u8be5', '\u7518', '\u5188', '\u768b', '\u6208', '\u7d66',
            '\u6839', '\u5e9a', '\u5de5', '\u52fe', '\u4f30', '\u74dc', '\u7f6b', '\u5173',
            '\u5149', '\u5f52', '\u886e', '\u5459', '\u54c8', '\u54b3', '\u9878', '\u82c0',
            '\u84bf', '\u8bc3', '\u9ed2', '\u62eb', '\u4ea8', '\u5677', '\u543d', '\u9f41',
            '\u5322', '\u82b1', '\u6000', '\u72bf', '\u5ddf', '\u7070', '\u660f', '\u5419',
            '\u4e0c', '\u52a0', '\u620b', '\u6c5f', '\u827d', '\u9636', '\u5dfe', '\u52a4',
            '\u5182', '\u52fc', '\u530a', '\u5a1f', '\u5658', '\u519b', '\u5494', '\u5f00',
            '\u520a', '\u95f6', '\u5c3b', '\u533c', '\u524b', '\u80af', '\u962c', '\u7a7a',
            '\u62a0', '\u5233', '\u5938', '\u84af', '\u5bbd', '\u5321', '\u4e8f', '\u5764',
            '\u6269', '\u5783', '\u6765', '\u5170', '\u5577', '\u635e', '\u4ec2', '\u52d2',
            '\u5844', '\u5215', '\u5006', '\u5941', '\u826f', '\u64a9', '\u5217', '\u62ce',
            '\u3007', '\u6e9c', '\u9f99', '\u779c', '\u565c', '\u5a08', '\u7567', '\u62a1',
            '\u7f57', '\u5463', '\u5988', '\u973e', '\u5ada', '\u9099', '\u732b', '\u9ebc',
            '\u6c92', '\u95e8', '\u753f', '\u54aa', '\u7720', '\u55b5', '\u54a9', '\u6c11',
            '\u540d', '\u8c2c', '\u6478', '\u54de', '\u6bea', '\u62cf', '\u5b7b', '\u56e1',
            '\u56ca', '\u5b6c', '\u8bb7', '\u9981', '\u6041', '\u80fd', '\u59ae', '\u62c8',
            '\u5b22', '\u9e1f', '\u634f', '\u60a8', '\u5b81', '\u599e', '\u519c', '\u7fba',
            '\u5974', '\u597b', '\u8650', '\u632a', '\u5594', '\u8bb4', '\u8db4', '\u62cd',
            '\u7705', '\u4e53', '\u629b', '\u5478', '\u55b7', '\u5309', '\u4e15', '\u504f',
            '\u527d', '\u6c15', '\u59d8', '\u4e52', '\u948b', '\u5256', '\u4ec6', '\u4e03',
            '\u6390', '\u5343', '\u545b', '\u6084', '\u767f', '\u4fb5', '\u9751', '\u909b',
            '\u4e18', '\u66f2', '\u5f2e', '\u7f3a', '\u590b', '\u5465', '\u7a63', '\u5a06',
            '\u60f9', '\u4eba', '\u6254', '\u65e5', '\u8338', '\u53b9', '\u5982', '\u5827',
            '\u6875', '\u95f0', '\u82e5', '\u4ee8', '\u6be2', '\u4e09', '\u6852', '\u63bb',
            '\u8272', '\u68ee', '\u50e7', '\u6740', '\u7b5b', '\u5c71', '\u4f24', '\u5f30',
            '\u5962', '\u7533', '\u5347', '\u5c38', '\u53ce', '\u4e66', '\u5237', '\u6454',
            '\u95e9', '\u53cc', '\u8c01', '\u542e', '\u5981', '\u53b6', '\u5fea', '\u635c',
            '\u82cf', '\u72fb', '\u590a', '\u5b59', '\u5506', '\u4ed6', '\u82d4', '\u574d',
            '\u94f4', '\u5932', '\u5fd1', '\u71a5', '\u5254', '\u5929', '\u4f7b', '\u5e16',
            '\u5385', '\u56f2', '\u5077', '\u92c0', '\u6e4d', '\u63a8', '\u541e', '\u6258',
            '\u6316', '\u6b6a', '\u5f2f', '\u5c2a', '\u5371', '\u586d', '\u7fc1', '\u631d',
            '\u5140', '\u5915', '\u867e', '\u4eda', '\u4e61', '\u7071', '\u4e9b', '\u5fc3',
            '\u661f', '\u51f6', '\u4f11', '\u65f4', '\u8f69', '\u75b6', '\u52cb', '\u4e2b',
            '\u6079', '\u592e', '\u5e7a', '\u8036', '\u4e00', '\u6b2d', '\u5e94', '\u54df',
            '\u4f63', '\u4f18', '\u625c', '\u9e22', '\u66f0', '\u6655', '\u531d', '\u707d',
            '\u7ccc', '\u7242', '\u50ae', '\u5219', '\u8d3c', '\u600e', '\u5897', '\u5412',
            '\u635a', '\u6cbe', '\u5f20', '\u948a', '\u8707', '\u8d1e', '\u4e89', '\u4e4b',
            '\u4e2d', '\u5dde', '\u6731', '\u6293', '\u8de9', '\u4e13', '\u5986', '\u96b9',
            '\u5b92', '\u5353', '\u5b5c', '\u5b97', '\u90b9', '\u79df', '\u94bb', '\u539c',
            '\u5c0a', '\u6628',
    };

    /**
     * Pinyin array. Each pinyin is corresponding to unihans of same offset in
     * the unihans array.
     */
    public static final byte[][] PINYINS = {
            { 65, 0, 0, 0, 0, 0 }, { 65, 73, 0, 0, 0, 0 }, { 65, 78, 0, 0, 0, 0 },
            { 65, 78, 71, 0, 0, 0 }, { 65, 79, 0, 0, 0, 0 }, { 66, 65, 0, 0, 0, 0 },
            { 66, 65, 73, 0, 0, 0 }, { 66, 65, 78, 0, 0, 0 }, { 66, 65, 78, 71, 0, 0 },
            { 66, 65, 79, 0, 0, 0 }, { 66, 69, 73, 0, 0, 0 }, { 66, 69, 78, 0, 0, 0 },
            { 66, 69, 78, 71, 0, 0 }, { 66, 73, 0, 0, 0, 0 }, { 66, 73, 65, 78, 0, 0 },
            { 66, 73, 65, 79, 0, 0 }, { 66, 73, 69, 0, 0, 0 }, { 66, 73, 78, 0, 0, 0 },
            { 66, 73, 78, 71, 0, 0 }, { 66, 79, 0, 0, 0, 0 }, { 66, 85, 0, 0, 0, 0 },
            { 67, 65, 0, 0, 0, 0 }, { 67, 65, 73, 0, 0, 0 },
            { 67, 65, 78, 0, 0, 0 }, { 67, 65, 78, 71, 0, 0 }, { 67, 65, 79, 0, 0, 0 },
            { 67, 69, 0, 0, 0, 0 }, { 67, 69, 78, 0, 0, 0 }, { 67, 69, 78, 71, 0, 0 },
            { 67, 72, 65, 0, 0, 0 }, { 67, 72, 65, 73, 0, 0 }, { 67, 72, 65, 78, 0, 0 },
            { 67, 72, 65, 78, 71, 0 }, { 67, 72, 65, 79, 0, 0 }, { 67, 72, 69, 0, 0, 0 },
            { 67, 72, 69, 78, 0, 0 }, { 67, 72, 69, 78, 71, 0 }, { 67, 72, 73, 0, 0, 0 },
            { 67, 72, 79, 78, 71, 0 }, { 67, 72, 79, 85, 0, 0 }, { 67, 72, 85, 0, 0, 0 },
            { 67, 72, 85, 65, 0, 0 }, { 67, 72, 85, 65, 73, 0 }, { 67, 72, 85, 65, 78, 0 },
            { 67, 72, 85, 65, 78, 71 }, { 67, 72, 85, 73, 0, 0 }, { 67, 72, 85, 78, 0, 0 },
            { 67, 72, 85, 79, 0, 0 }, { 67, 73, 0, 0, 0, 0 }, { 67, 79, 78, 71, 0, 0 },
            { 67, 79, 85, 0, 0, 0 }, { 67, 85, 0, 0, 0, 0 }, { 67, 85, 65, 78, 0, 0 },
            { 67, 85, 73, 0, 0, 0 }, { 67, 85, 78, 0, 0, 0 }, { 67, 85, 79, 0, 0, 0 },
            { 68, 65, 0, 0, 0, 0 }, { 68, 65, 73, 0, 0, 0 }, { 68, 65, 78, 0, 0, 0 },
            { 68, 65, 78, 71, 0, 0 }, { 68, 65, 79, 0, 0, 0 }, { 68, 69, 0, 0, 0, 0 },
            { 68, 69, 73, 0, 0, 0 }, { 68, 69, 78, 0, 0, 0 }, { 68, 69, 78, 71, 0, 0 },
            { 68, 73, 0, 0, 0, 0 }, { 68, 73, 65, 0, 0, 0 }, { 68, 73, 65, 78, 0, 0 },
            { 68, 73, 65, 79, 0, 0 }, { 68, 73, 69, 0, 0, 0 }, { 68, 73, 78, 71, 0, 0 },
            { 68, 73, 85, 0, 0, 0 }, { 68, 79, 78, 71, 0, 0 }, { 68, 79, 85, 0, 0, 0 },
            { 68, 85, 0, 0, 0, 0 }, { 68, 85, 65, 78, 0, 0 }, { 68, 85, 73, 0, 0, 0 },
            { 68, 85, 78, 0, 0, 0 }, { 68, 85, 79, 0, 0, 0 }, { 69, 0, 0, 0, 0, 0 },
            { 69, 73, 0, 0, 0, 0 }, { 69, 78, 0, 0, 0, 0 }, { 69, 78, 71, 0, 0, 0 },
            { 69, 82, 0, 0, 0, 0 }, { 70, 65, 0, 0, 0, 0 }, { 70, 65, 78, 0, 0, 0 },
            { 70, 65, 78, 71, 0, 0 }, { 70, 69, 73, 0, 0, 0 }, { 70, 69, 78, 0, 0, 0 },
            { 70, 69, 78, 71, 0, 0 }, { 70, 73, 65, 79, 0, 0 }, { 70, 79, 0, 0, 0, 0 },
            { 70, 79, 85, 0, 0, 0 }, { 70, 85, 0, 0, 0, 0 }, { 71, 65, 0, 0, 0, 0 },
            { 71, 65, 73, 0, 0, 0 }, { 71, 65, 78, 0, 0, 0 }, { 71, 65, 78, 71, 0, 0 },
            { 71, 65, 79, 0, 0, 0 }, { 71, 69, 0, 0, 0, 0 }, { 71, 69, 73, 0, 0, 0 },
            { 71, 69, 78, 0, 0, 0 }, { 71, 69, 78, 71, 0, 0 }, { 71, 79, 78, 71, 0, 0 },
            { 71, 79, 85, 0, 0, 0 }, { 71, 85, 0, 0, 0, 0 }, { 71, 85, 65, 0, 0, 0 },
            { 71, 85, 65, 73, 0, 0 }, { 71, 85, 65, 78, 0, 0 }, { 71, 85, 65, 78, 71, 0 },
            { 71, 85, 73, 0, 0, 0 }, { 71, 85, 78, 0, 0, 0 }, { 71, 85, 79, 0, 0, 0 },
            { 72, 65, 0, 0, 0, 0 }, { 72, 65, 73, 0, 0, 0 }, { 72, 65, 78, 0, 0, 0 },
            { 72, 65, 78, 71, 0, 0 }, { 72, 65, 79, 0, 0, 0 }, { 72, 69, 0, 0, 0, 0 },
            { 72, 69, 73, 0, 0, 0 }, { 72, 69, 78, 0, 0, 0 }, { 72, 69, 78, 71, 0, 0 },
            { 72, 77, 0, 0, 0, 0 }, { 72, 79, 78, 71, 0, 0 }, { 72, 79, 85, 0, 0, 0 },
            { 72, 85, 0, 0, 0, 0 }, { 72, 85, 65, 0, 0, 0 }, { 72, 85, 65, 73, 0, 0 },
            { 72, 85, 65, 78, 0, 0 }, { 72, 85, 65, 78, 71, 0 }, { 72, 85, 73, 0, 0, 0 },
            { 72, 85, 78, 0, 0, 0 }, { 72, 85, 79, 0, 0, 0 }, { 74, 73, 0, 0, 0, 0 },
            { 74, 73, 65, 0, 0, 0 }, { 74, 73, 65, 78, 0, 0 }, { 74, 73, 65, 78, 71, 0 },
            { 74, 73, 65, 79, 0, 0 }, { 74, 73, 69, 0, 0, 0 }, { 74, 73, 78, 0, 0, 0 },
            { 74, 73, 78, 71, 0, 0 }, { 74, 73, 79, 78, 71, 0 }, { 74, 73, 85, 0, 0, 0 },
            { 74, 85, 0, 0, 0, 0 }, { 74, 85, 65, 78, 0, 0 }, { 74, 85, 69, 0, 0, 0 },
            { 74, 85, 78, 0, 0, 0 }, { 75, 65, 0, 0, 0, 0 }, { 75, 65, 73, 0, 0, 0 },
            { 75, 65, 78, 0, 0, 0 }, { 75, 65, 78, 71, 0, 0 }, { 75, 65, 79, 0, 0, 0 },
            { 75, 69, 0, 0, 0, 0 }, { 75, 69, 73, 0, 0, 0 }, { 75, 69, 78, 0, 0, 0 },
            { 75, 69, 78, 71, 0, 0 }, { 75, 79, 78, 71, 0, 0 }, { 75, 79, 85, 0, 0, 0 },
            { 75, 85, 0, 0, 0, 0 }, { 75, 85, 65, 0, 0, 0 }, { 75, 85, 65, 73, 0, 0 },
            { 75, 85, 65, 78, 0, 0 }, { 75, 85, 65, 78, 71, 0 }, { 75, 85, 73, 0, 0, 0 },
            { 75, 85, 78, 0, 0, 0 }, { 75, 85, 79, 0, 0, 0 }, { 76, 65, 0, 0, 0, 0 },
            { 76, 65, 73, 0, 0, 0 }, { 76, 65, 78, 0, 0, 0 }, { 76, 65, 78, 71, 0, 0 },
            { 76, 65, 79, 0, 0, 0 }, { 76, 69, 0, 0, 0, 0 }, { 76, 69, 73, 0, 0, 0 },
            { 76, 69, 78, 71, 0, 0 }, { 76, 73, 0, 0, 0, 0 }, { 76, 73, 65, 0, 0, 0 },
            { 76, 73, 65, 78, 0, 0 }, { 76, 73, 65, 78, 71, 0 }, { 76, 73, 65, 79, 0, 0 },
            { 76, 73, 69, 0, 0, 0 }, { 76, 73, 78, 0, 0, 0 }, { 76, 73, 78, 71, 0, 0 },
            { 76, 73, 85, 0, 0, 0 }, { 76, 79, 78, 71, 0, 0 }, { 76, 79, 85, 0, 0, 0 },
            { 76, 85, 0, 0, 0, 0 }, { 76, 85, 65, 78, 0, 0 }, { 76, 85, 69, 0, 0, 0 },
            { 76, 85, 78, 0, 0, 0 }, { 76, 85, 79, 0, 0, 0 }, { 77, 0, 0, 0, 0, 0 },
            { 77, 65, 0, 0, 0, 0 }, { 77, 65, 73, 0, 0, 0 }, { 77, 65, 78, 0, 0, 0 },
            { 77, 65, 78, 71, 0, 0 }, { 77, 65, 79, 0, 0, 0 }, { 77, 69, 0, 0, 0, 0 },
            { 77, 69, 73, 0, 0, 0 }, { 77, 69, 78, 0, 0, 0 }, { 77, 69, 78, 71, 0, 0 },
            { 77, 73, 0, 0, 0, 0 }, { 77, 73, 65, 78, 0, 0 }, { 77, 73, 65, 79, 0, 0 },
            { 77, 73, 69, 0, 0, 0 }, { 77, 73, 78, 0, 0, 0 }, { 77, 73, 78, 71, 0, 0 },
            { 77, 73, 85, 0, 0, 0 }, { 77, 79, 0, 0, 0, 0 }, { 77, 79, 85, 0, 0, 0 },
            { 77, 85, 0, 0, 0, 0 }, { 78, 65, 0, 0, 0, 0 }, { 78, 65, 73, 0, 0, 0 },
            { 78, 65, 78, 0, 0, 0 }, { 78, 65, 78, 71, 0, 0 }, { 78, 65, 79, 0, 0, 0 },
            { 78, 69, 0, 0, 0, 0 }, { 78, 69, 73, 0, 0, 0 }, { 78, 69, 78, 0, 0, 0 },
            { 78, 69, 78, 71, 0, 0 }, { 78, 73, 0, 0, 0, 0 }, { 78, 73, 65, 78, 0, 0 },
            { 78, 73, 65, 78, 71, 0 }, { 78, 73, 65, 79, 0, 0 }, { 78, 73, 69, 0, 0, 0 },
            { 78, 73, 78, 0, 0, 0 }, { 78, 73, 78, 71, 0, 0 }, { 78, 73, 85, 0, 0, 0 },
            { 78, 79, 78, 71, 0, 0 }, { 78, 79, 85, 0, 0, 0 }, { 78, 85, 0, 0, 0, 0 },
            { 78, 85, 65, 78, 0, 0 }, { 78, 85, 69, 0, 0, 0 }, { 78, 85, 79, 0, 0, 0 },
            { 79, 0, 0, 0, 0, 0 }, { 79, 85, 0, 0, 0, 0 }, { 80, 65, 0, 0, 0, 0 },
            { 80, 65, 73, 0, 0, 0 }, { 80, 65, 78, 0, 0, 0 }, { 80, 65, 78, 71, 0, 0 },
            { 80, 65, 79, 0, 0, 0 }, { 80, 69, 73, 0, 0, 0 }, { 80, 69, 78, 0, 0, 0 },
            { 80, 69, 78, 71, 0, 0 }, { 80, 73, 0, 0, 0, 0 }, { 80, 73, 65, 78, 0, 0 },
            { 80, 73, 65, 79, 0, 0 }, { 80, 73, 69, 0, 0, 0 }, { 80, 73, 78, 0, 0, 0 },
            { 80, 73, 78, 71, 0, 0 }, { 80, 79, 0, 0, 0, 0 }, { 80, 79, 85, 0, 0, 0 },
            { 80, 85, 0, 0, 0, 0 }, { 81, 73, 0, 0, 0, 0 }, { 81, 73, 65, 0, 0, 0 },
            { 81, 73, 65, 78, 0, 0 }, { 81, 73, 65, 78, 71, 0 }, { 81, 73, 65, 79, 0, 0 },
            { 81, 73, 69, 0, 0, 0 }, { 81, 73, 78, 0, 0, 0 }, { 81, 73, 78, 71, 0, 0 },
            { 81, 73, 79, 78, 71, 0 }, { 81, 73, 85, 0, 0, 0 }, { 81, 85, 0, 0, 0, 0 },
            { 81, 85, 65, 78, 0, 0 }, { 81, 85, 69, 0, 0, 0 }, { 81, 85, 78, 0, 0, 0 },
            { 82, 65, 78, 0, 0, 0 }, { 82, 65, 78, 71, 0, 0 }, { 82, 65, 79, 0, 0, 0 },
            { 82, 69, 0, 0, 0, 0 }, { 82, 69, 78, 0, 0, 0 }, { 82, 69, 78, 71, 0, 0 },
            { 82, 73, 0, 0, 0, 0 }, { 82, 79, 78, 71, 0, 0 }, { 82, 79, 85, 0, 0, 0 },
            { 82, 85, 0, 0, 0, 0 }, { 82, 85, 65, 78, 0, 0 }, { 82, 85, 73, 0, 0, 0 },
            { 82, 85, 78, 0, 0, 0 }, { 82, 85, 79, 0, 0, 0 }, { 83, 65, 0, 0, 0, 0 },
            { 83, 65, 73, 0, 0, 0 }, { 83, 65, 78, 0, 0, 0 }, { 83, 65, 78, 71, 0, 0 },
            { 83, 65, 79, 0, 0, 0 }, { 83, 69, 0, 0, 0, 0 }, { 83, 69, 78, 0, 0, 0 },
            { 83, 69, 78, 71, 0, 0 }, { 83, 72, 65, 0, 0, 0 }, { 83, 72, 65, 73, 0, 0 },
            { 83, 72, 65, 78, 0, 0 }, { 83, 72, 65, 78, 71, 0 }, { 83, 72, 65, 79, 0, 0 },
            { 83, 72, 69, 0, 0, 0 }, { 83, 72, 69, 78, 0, 0 }, { 83, 72, 69, 78, 71, 0 },
            { 83, 72, 73, 0, 0, 0 }, { 83, 72, 79, 85, 0, 0 }, { 83, 72, 85, 0, 0, 0 },
            { 83, 72, 85, 65, 0, 0 }, { 83, 72, 85, 65, 73, 0 }, { 83, 72, 85, 65, 78, 0 },
            { 83, 72, 85, 65, 78, 71 }, { 83, 72, 85, 73, 0, 0 }, { 83, 72, 85, 78, 0, 0 },
            { 83, 72, 85, 79, 0, 0 }, { 83, 73, 0, 0, 0, 0 }, { 83, 79, 78, 71, 0, 0 },
            { 83, 79, 85, 0, 0, 0 }, { 83, 85, 0, 0, 0, 0 }, { 83, 85, 65, 78, 0, 0 },
            { 83, 85, 73, 0, 0, 0 }, { 83, 85, 78, 0, 0, 0 }, { 83, 85, 79, 0, 0, 0 },
            { 84, 65, 0, 0, 0, 0 }, { 84, 65, 73, 0, 0, 0 }, { 84, 65, 78, 0, 0, 0 },
            { 84, 65, 78, 71, 0, 0 }, { 84, 65, 79, 0, 0, 0 }, { 84, 69, 0, 0, 0, 0 },
            { 84, 69, 78, 71, 0, 0 }, { 84, 73, 0, 0, 0, 0 }, { 84, 73, 65, 78, 0, 0 },
            { 84, 73, 65, 79, 0, 0 }, { 84, 73, 69, 0, 0, 0 }, { 84, 73, 78, 71, 0, 0 },
            { 84, 79, 78, 71, 0, 0 }, { 84, 79, 85, 0, 0, 0 }, { 84, 85, 0, 0, 0, 0 },
            { 84, 85, 65, 78, 0, 0 }, { 84, 85, 73, 0, 0, 0 }, { 84, 85, 78, 0, 0, 0 },
            { 84, 85, 79, 0, 0, 0 }, { 87, 65, 0, 0, 0, 0 }, { 87, 65, 73, 0, 0, 0 },
            { 87, 65, 78, 0, 0, 0 }, { 87, 65, 78, 71, 0, 0 }, { 87, 69, 73, 0, 0, 0 },
            { 87, 69, 78, 0, 0, 0 }, { 87, 69, 78, 71, 0, 0 }, { 87, 79, 0, 0, 0, 0 },
            { 87, 85, 0, 0, 0, 0 }, { 88, 73, 0, 0, 0, 0 }, { 88, 73, 65, 0, 0, 0 },
            { 88, 73, 65, 78, 0, 0 }, { 88, 73, 65, 78, 71, 0 }, { 88, 73, 65, 79, 0, 0 },
            { 88, 73, 69, 0, 0, 0 }, { 88, 73, 78, 0, 0, 0 }, { 88, 73, 78, 71, 0, 0 },
            { 88, 73, 79, 78, 71, 0 }, { 88, 73, 85, 0, 0, 0 }, { 88, 85, 0, 0, 0, 0 },
            { 88, 85, 65, 78, 0, 0 }, { 88, 85, 69, 0, 0, 0 }, { 88, 85, 78, 0, 0, 0 },
            { 89, 65, 0, 0, 0, 0 }, { 89, 65, 78, 0, 0, 0 }, { 89, 65, 78, 71, 0, 0 },
            { 89, 65, 79, 0, 0, 0 }, { 89, 69, 0, 0, 0, 0 }, { 89, 73, 0, 0, 0, 0 },
            { 89, 73, 78, 0, 0, 0 }, { 89, 73, 78, 71, 0, 0 }, { 89, 79, 0, 0, 0, 0 },
            { 89, 79, 78, 71, 0, 0 }, { 89, 79, 85, 0, 0, 0 }, { 89, 85, 0, 0, 0, 0 },
            { 89, 85, 65, 78, 0, 0 }, { 89, 85, 69, 0, 0, 0 }, { 89, 85, 78, 0, 0, 0 },
            { 90, 65, 0, 0, 0, 0 }, { 90, 65, 73, 0, 0, 0 }, { 90, 65, 78, 0, 0, 0 },
            { 90, 65, 78, 71, 0, 0 }, { 90, 65, 79, 0, 0, 0 }, { 90, 69, 0, 0, 0, 0 },
            { 90, 69, 73, 0, 0, 0 }, { 90, 69, 78, 0, 0, 0 }, { 90, 69, 78, 71, 0, 0 },
            { 90, 72, 65, 0, 0, 0 }, { 90, 72, 65, 73, 0, 0 }, { 90, 72, 65, 78, 0, 0 },
            { 90, 72, 65, 78, 71, 0 }, { 90, 72, 65, 79, 0, 0 }, { 90, 72, 69, 0, 0, 0 },
            { 90, 72, 69, 78, 0, 0 }, { 90, 72, 69, 78, 71, 0 }, { 90, 72, 73, 0, 0, 0 },
            { 90, 72, 79, 78, 71, 0 }, { 90, 72, 79, 85, 0, 0 }, { 90, 72, 85, 0, 0, 0 },
            { 90, 72, 85, 65, 0, 0 }, { 90, 72, 85, 65, 73, 0 }, { 90, 72, 85, 65, 78, 0 },
            { 90, 72, 85, 65, 78, 71 }, { 90, 72, 85, 73, 0, 0 }, { 90, 72, 85, 78, 0, 0 },
            { 90, 72, 85, 79, 0, 0 }, { 90, 73, 0, 0, 0, 0 }, { 90, 79, 78, 71, 0, 0 },
            { 90, 79, 85, 0, 0, 0 }, { 90, 85, 0, 0, 0, 0 }, { 90, 85, 65, 78, 0, 0 },
            { 90, 85, 73, 0, 0, 0 }, { 90, 85, 78, 0, 0, 0 }, { 90, 85, 79, 0, 0, 0 }, };

    /** First and last Chinese character with known Pinyin according to zh collation */

    private static final Collator COLLATOR = Collator.getInstance(Locale.CHINA);

    private static HanziToPinyin sInstance;

    public static class Token {
        /**
         * Separator between target string for each source char
         */

        public static final int LATIN = 1;
        public static final int PINYIN = 2;
        public static final int UNKNOWN = 3;

        public static final int DUOYIN_MASK_BIT = 0x10;
        public static final int TYPE_MASK_BIT = 0x0F;

        public Token() {
            target = new String[2]; // support duoyinzi ,max is 2
        }

        public String toString() {
            return "source:" + source + " target:" + target[0] + " / "
                    + target[1] + " type:" + type + " hanzi:" + isHanzi();
        }

        public void addDefaultTarget(String str) {
            if (target != null && !TextUtils.isEmpty(str)) {
                target[0] = str;
            }
        }

        public void addAdditionalTarget(String str) {
            if (target != null && !TextUtils.isEmpty(str)) {
                target[1] = str;
            }
            markAsDuoyin();
        }

        private void markAsDuoyin() {
            if (getType() == PINYIN) {
                type |= DUOYIN_MASK_BIT;
            }
        }

        public int getType() {
            return (type & TYPE_MASK_BIT);
        }

        public boolean isDuoyin() {
            return (type & DUOYIN_MASK_BIT) == DUOYIN_MASK_BIT;
        }

        public boolean isHanzi() {
            return getType() == PINYIN;
        }

        /**
         * Type of this token, ASCII, PINYIN or UNKNOWN.
         */
        public int type;
        /**
         * Original string before translation.
         */
        public String source;
        /**
         * Translated string of source. For Han, target is corresponding Pinyin.
         * Otherwise target is original string in source.
         */
        public String[] target;
    }

    protected HanziToPinyin() {

    }

    public static HanziToPinyin getInstance() {

        synchronized (HanziToPinyin.class) {
            if (sInstance != null) {
                return sInstance;
            }

            final Locale locale[] = Collator.getAvailableLocales();
            for (int i = 0; i < locale.length; i++) {

                // Do self validation just once.
                if (WeatherController.DEBUG)
                    Log.d("HanziToPinyin", "locale:" + locale[i]);
            }

            sInstance = new HanziToPinyin();
            return sInstance;
        }
    }

    public Token getToken(char character) {
        // Log.d("mixiao","getToken" + character + " value:" +
        // Integer.toHexString(character));

        final String FIRST_PINYIN_UNIHAN = "\u963F";
        final String LAST_PINYIN_UNIHAN = "\u84D9";
        /** The first Chinese character in Unicode block */
        final char FIRST_UNIHAN = '\u3400';

        Token token = new Token();
        // handle the char ourselves . some pinyin
        // 's order is wrong by using the
        // default method , so let's handle them ourselves
        if (isSpecial(character)) {
            if (WeatherController.DEBUG)
                Log.d("HanziToPinyin", "it's a special char , let's handle our self");
            return handleSpecial(character);
        }

        final String letter = Character.toString(character);
        token.source = letter;
        int offset = -1;
        int cmp;
        if (character < 256) {
            token.type = Token.LATIN;
            return token;
        } else if (character < FIRST_UNIHAN) {
            token.type = Token.UNKNOWN;
            return token;
        } else {
            cmp = COLLATOR.compare(letter, FIRST_PINYIN_UNIHAN);
            if (cmp < 0) {
                token.type = Token.UNKNOWN;
                return token;
            } else if (cmp == 0) {
                token.type = Token.PINYIN;
                offset = 0;
            } else {
                cmp = COLLATOR.compare(letter, LAST_PINYIN_UNIHAN);
                if (cmp > 0) {
                    token.type = Token.UNKNOWN;
                    return token;
                } else if (cmp == 0) {
                    token.type = Token.PINYIN;
                    offset = UNIHANS.length - 1;
                }
            }
        }

        token.type = Token.PINYIN;
        if (offset < 0) {
            int begin = 0;
            int end = UNIHANS.length - 1;
            while (begin <= end) {
                offset = (begin + end) / 2;

                final String unihan = Character.toString(UNIHANS[offset]);
                cmp = COLLATOR.compare(letter, unihan);
                if (cmp == 0) {
                    break;
                } else if (cmp > 0) {
                    begin = offset + 1;
                } else {
                    end = offset - 1;
                }
            }
        }
        if (cmp < 0) {
            offset--;
        }
        StringBuilder pinyin = new StringBuilder();
        for (int j = 0; j < PINYINS[offset].length && PINYINS[offset][j] != 0; j++) {
            pinyin.append((char) PINYINS[offset][j]);
        }

        final String duoyin = checkDuoyin(character);
        if (duoyin != null && token != null) {
            token.addDefaultTarget(duoyin);
        } else {
            token.addDefaultTarget(pinyin.toString().toLowerCase());
        }
        return token;
    }

    private Token handleSpecial(final char character) {
        Token token = new Token();
        token.type = Token.PINYIN;
        token.source = Character.toString(character);

        if (character == '\u6c88') {
            token.target[0] = "shen";

        } else if (character == '\u7fdf') {
            token.target[0] = "zhai";

        } else if (character == '\u66fe') {
            token.target[0] = "zeng";
        } else if (character == '\u4ec7') {
            token.target[0] = "qiu";
        } else {
            throw new RuntimeException(
                    "the special char doesn't find in the handleSpecial which defines in the isSpecial");
        }

        return token;
    }

    private boolean isSpecial(final char character) {
        // TODO Auto-generated method stub
        final char c = character;

        return c == '\u6c88' // 沈
                || c == '\u7fdf' // 翟
                || c == '\u66fe' // 曾
                || c == '\u4ec7';

    }

    private String checkDuoyin(char ch) {

        final int unicode = (int) ch;

         switch (unicode) {
             case 0x5927:
                 return "da"; //大
             case 0x4f5b:
                 return "fo"; //佛
             case 0x884c:
                 return "hang"; //行
             case 0x957f:
                 return "chang"; //长
             case 0x5415:
                 return "lyu"; //吕
             case 0x90fd:
                 return "du";  //都
             case 0x4ec0:
                 return "shi"; //什
           /* case 0x963f:
                return "a";
            case 0x814c:
                return "an";
            case 0x8180:
                return "pang";
            case 0x8584:
                return "bao";
            case 0x5821:
                return "pu";
            case 0x4fbf:
                return "pian";
            case 0x535c:
                return "bo";
            case 0x53c2:
                return "shen";
            case 0x85cf:
                return "zang";
            case 0x5dee:
                return "chai";
            case 0x671d:
                return "chao";
            case 0x8f66:
                return "ju";
            case 0x76db:
                return "cheng";
            case 0x91cd:
                return "chong";
            case 0x81ed:
                return "xiu";
            case 0x755c:
                return "xu";
            case 0x4f20:
                return "zhuan";
            case 0x4f3a:
                return "ci";
            case 0x5355:
                return "dan";
            case 0x5f39:
                return "dan";
            case 0x63d0:
                return "di";
            case 0x5f97:
                return "dei";
            case 0x7684:
                return "di";
            case 0x7ed9:
                return "ji";
            case 0x4f1a:
                return "kuai";
            case 0x5947:
                return "ji";
            case 0x8304:
                return "jia";
            case 0x89d2:
                return "jue";
            case 0x6821:
                return "jiao";
            case 0x89e3:
                return "xie";
            case 0x52b2:
                return "jing";
            case 0x9f9f:
                return "jun";
            case 0x5361:
                return "qia";
            case 0x79d8:
                return "bi";
            case 0x6ccc:
                return "bi";
            case 0x6a21:
                return "mu";
            case 0x7f2a:
                return "mou";
            case 0x5a1c:
                return "nuo";
            case 0x8feb:
                return "pai";
            case 0x6734:
                return "pu";
            case 0x7011:
                return "bao";
            case 0x66dd:
                return "pu";
            case 0x5f3a:
                return "jiang";
            case 0x4eb2:
                return "qing";
            case 0x8272:
                return "shai";
            case 0x53a6:
                return "xia";
            case 0x6298:
                return "she";
            case 0x4ec0:
                return "shen";
            case 0x4f3c:
                return "shi";
            case 0x719f:
                return "shou";
            case 0x8c03:
                return "tiao";
            case 0x5c09:
                return "wei";
            case 0x5413:
                return "he";
            case 0x7701:
                return "xing";
            case 0x5bbf:
                return "xiu";
            case 0x6bb7:
                return "yan";
            case 0x827e:
                return "ai";
            case 0x4e50:
                return "yue";
            case 0x7740:
                return "zhao";*/

            default:
                return null;
        }
    }

}
