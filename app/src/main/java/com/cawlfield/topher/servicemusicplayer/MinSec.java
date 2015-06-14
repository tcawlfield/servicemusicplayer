/**
 *   Copyright (C) 2014-2015 Topher Cawlfield <tcawlfield_at_gmail_dot_com>
 *
 *   This file is part of ServiceMusicPlayer.
 *
 *   ServiceMusicPlayer is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   ServiceMusicPlayer is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with ServiceMusicPlayer.  If not, see <http://www.gnu.org/licenses/>.
 **/

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
