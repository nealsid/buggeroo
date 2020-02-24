package com.nealsid.buggeroo;

import java.lang.String;

class Preferences {
    private static String ACTIVE_THREAD_COLOR = ConsoleColors.WHITE + ConsoleColors.PURPLE_BACKGROUND;
    private static String ACTIVE_FRAME_COLOR = ConsoleColors.YELLOW_BOLD;

    public static String activeThreadColor() {
	return ACTIVE_THREAD_COLOR;
    }

    public static String activeFrameColor() {
	return ACTIVE_FRAME_COLOR;
    }
}
