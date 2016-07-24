package com.gwcd.sy.barcodetool.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 *     
 * @项目名称：TwoCodeTools    
 * @类名称：StringUtils    
 * @类描述： 字符串工具类   
 * @创建人：Administrator    
 * @创建时间：2015-5-30 下午4:53:50    
 * @修改人：Administrator    
 * @修改时间：2015-5-30 下午4:53:50    
 * @修改备注：    
 * @version     
 *
 */
public class StringUtils {
    
    /**
     * @description 用正则表达式匹配，判断字符串是否是有效的网址
     * @date 2015-5-30
     * @param url
     * @return
     */
    public static boolean isUrl(String url) {
        Pattern pattern = Pattern  
                .compile("(http://|ftp://|https://|www){0,1}[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)?[^\u4e00-\u9fa5\\s]*");  
        // 空格结束  
        Matcher matcher = pattern  
                .matcher(url);
        
        if (matcher.matches()) {
            return true;
        }
        
        return false;
    }
}
