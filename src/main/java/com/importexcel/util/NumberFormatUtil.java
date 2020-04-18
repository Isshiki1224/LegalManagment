package com.importexcel.util;


/**
 *  * 数字转换工具类
 *  *
 *  * @author Zero
 *  * @date 2016年12月24日 下午4:52:06
 *  * @version 2016年12月24日 Zero TODO简要描述修改内容，修改原因
 *  
 */
public class NumberFormatUtil {

    /**
     *    * 将阿拉伯数字转换成中文
     *    * （d>=0 && d<100000000）
     *    *
     *    * @param d
     *    * @return
     *   
     */


    public static String numberFormat(int d) {

        String[] str = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};

        String ss[] = new String[]{"", "十", "百", "千", "万", "十", "百", "千", "亿"};
        String s = String.valueOf(d);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            String index = String.valueOf(s.charAt(i));
            sb = sb.append(str[Integer.parseInt(index)]);
        }
        String sss = String.valueOf(sb);
        int i = 0;
        for (int j = sss.length(); j > 0; j--) {
            if (sss.charAt(j - 1) != '零') {
                sb = sb.insert(j, ss[i++]);
            } else if (i == 4 || i == 8) {
                sb.replace(j - 1, j, ss[i++]);
            } else {
                i++;
            }
        }
        // “一十”开头的去掉“一”
        if (sb.length() >= 2 && sb.charAt(0) == '一' && sb.charAt(1) == '十') {
            sb.replace(0, 1, "");
        }

        // 中间连续为零保留1个零
        if (sss.length() > 2) {
            for (int j = 1; j < sb.length() - 1; j++) {
                if (sb.charAt(j) == '零' && sb.charAt(j + 1) == '零') {
                    sb.replace(j, j + 1, "");
                    j--;
                }
            }
        }
        // 去掉尾数为零的
        while (sb.charAt(sb.length() - 1) == '零') {
            sb.replace(sb.length() - 1, sb.length(), "");
        }
        return sb.toString();
    }


    public static void main(String[] args) {
        System.out.println(NumberFormatUtil.numberFormat(15));
    }

}

