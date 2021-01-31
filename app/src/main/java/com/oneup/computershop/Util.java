package com.oneup.computershop;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Util {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    public static String formatDate(long millis) {
        return DATE_FORMAT.format(millis);
    }
}
