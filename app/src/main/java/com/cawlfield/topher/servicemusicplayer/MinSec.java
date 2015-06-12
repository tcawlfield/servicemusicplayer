package com.cawlfield.topher.servicemusicplayer;

import java.text.DecimalFormat;

/**
 * Created by ccawlfield on 6/11/15.
 */
public class MinSec {
    private static final DecimalFormat formatSeconds = new DecimalFormat("00");
    private int millis;

    public MinSec(int millis) {
        this.millis = millis;
    }

    public String toString() {
        int duration = (millis + 500) / 1000;
        return (duration / 60) + ":" + formatSeconds.format((duration % 60));
    }

    public static String toString(int millis) {
        int duration = (millis + 500) / 1000;
        return (duration / 60) + ":" + formatSeconds.format((duration % 60));
    }
}
