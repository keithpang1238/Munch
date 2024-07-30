package com.example.munch;

import java.util.Calendar;

class Holiday {
    static boolean isChristmasPeriod() {
        Calendar calendar = Calendar.getInstance();
        int month  = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return month == 11 || (month == 0 && day < 6);
    }
}
