package com.b_knabe.packet_capture.core.util.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

//Locale fix for inconsistent state
public class TimeFormatter {
    private static DateFormat HHMMSSSFormat = new SimpleDateFormat("HH:mm:ss:s",
    Locale.getDefault());
    private static DateFormat formatYYMMDDHHMMSSFormat = new SimpleDateFormat("yyyy:MM:dd-HH:mm:ss:s",
    Locale.getDefault());

    public static String formatToHHMMSSMM(long time) {
        Date date = new Date(time);
        return HHMMSSSFormat.format(date);
    }

    public static String formatToYYMMDDHHMMSS(long time) {
        Date date = new Date(time);
        return formatYYMMDDHHMMSSFormat.format(date);
    }
}