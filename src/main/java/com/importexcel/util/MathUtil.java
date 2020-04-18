package com.importexcel.util;

import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;

public class MathUtil {

    private static String[] NUMBER_ZH = new String[]{"零","一","二","三","四","五","六","七","八","九","十"};
    //最大支持9千兆
    private static String[] NUMBER_UNIT = new String[]{"","十","百","千","万","十","百","千","亿","十","百","千","兆","十","百","千"};

    /**
     * @param number
     * @return
     */
    public static String digital2Chinese(long number){
        Long value = number;
        Boolean isNegative = false;
        if (value < 0){
            value = -value;
            isNegative= true;
        }
        BigDecimal bigDecimal = BigDecimal.valueOf(value);

        String valueStr = value.toString();

        Integer [] digits = new Integer[valueStr.length()];

        for (int i=0;i<valueStr.length();i++){ //循环数存储每一位数字，从低到高
            digits[i] = bigDecimal.divideAndRemainder(BigDecimal.valueOf(10))[1].intValue();  //value.intValue()%10;
            bigDecimal = bigDecimal.divide(BigDecimal.valueOf(10));
        }
        StringBuilder sb = new StringBuilder();

        CollectionUtils.reverseArray(digits);  // 从高到低

        Boolean flush = false;
        Boolean needFilling = true;

        if (digits.length > 16){
            throw new ArrayIndexOutOfBoundsException("数字太大了，超出汉字可读范围");
        }

        for (int i=0;i<digits.length;i++){
            if (digits[i].equals(0)){
                if (needFilling){
                    if ( digits.length-i-1  == 4){
                        sb.append("万");
                        needFilling = false;
                    }
                    if ( digits.length-i-1 == 8){
                        sb.append("亿");
                        needFilling = false;
                    }
                    if ( digits.length-i-1 == 12){
                        sb.append("兆");
                        needFilling = false;
                    }
                }

                flush = true;
                continue;
            }
            if (flush){
                sb.append("零");
                flush = false;
            }
            sb.append( NUMBER_ZH[ digits[i] ] ).append( NUMBER_UNIT[digits.length-i-1] );
            if (Arrays.asList(4,8,12).contains( digits.length-i-1 )){
                needFilling = false;
            }else{
                needFilling = true;
            }
        }

        if (isNegative){
            return "负"+sb.toString();
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(MathUtil.digital2Chinese(56));
        System.out.println(MathUtil.digital2Chinese(Long.parseLong("15")));

    }

}
