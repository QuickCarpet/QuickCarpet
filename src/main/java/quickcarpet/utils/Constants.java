package quickcarpet.utils;

import quickcarpet.settings.Settings;

public final class Constants {
    private Constants() {}

    public static final class SetBlockState {
        private SetBlockState() {}

        public static final int DEFAULT = 3;

        public static final int UPDATE_NEIGHBORS = 1;
        public static final int SEND_TO_CLIENT = 2;
        public static final int NO_RERENDER = 4;
        public static final int RERENDER_MAIN_THREAD = 8;
        public static final int NO_OBSERVER_UPDATE = 16;
        public static final int FLAG_32 = 32;
        public static final int CALL_ON_ADDED_ON_REMOVED = 64;
        public static final int CHECK_LIGHT = 128;
        public static final int NO_FILL_UPDATE = 1024;

        public static int modifyFlags(int flags) {
            if (Settings.fillUpdates) return flags;
            return (flags & ~UPDATE_NEIGHBORS) | NO_FILL_UPDATE;
        }
    }
}
