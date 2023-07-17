package com.stit.jhbarcode.utils;


import java.text.SimpleDateFormat;

public class DateUtils {
    private static SimpleDateFormat dateFormatWithSlash = new SimpleDateFormat("yyyy/MM/dd");
    private static SimpleDateFormat dateFormatNoSlash = new SimpleDateFormat("yyyyMMdd");

    public static java.util.Date string2Date(String strDate) {
        java.util.Date date = null;

        try {
            date = dateFormatWithSlash.parse(strDate);
        } catch(Exception ex) {
            try {
                date = dateFormatNoSlash.parse(strDate);
            } catch (Exception ex2) {
                date = null;
            }
        }

        //if (date != null) {
        //    System.out.println(dateFormatWithSlash.format(date));
        //}

        return date;
    }

} // end class
