package quickcarpet.utils;

import net.minecraft.block.Block;
import quickcarpet.settings.Settings;

public final class Constants {
    private Constants() {}

    public static final class SetBlockState {
        private SetBlockState() {}

        public static final int DEFAULT = Block.NOTIFY_ALL;

        public static final int UPDATE_NEIGHBORS = Block.NOTIFY_NEIGHBORS;
        public static final int SEND_TO_CLIENT = Block.NOTIFY_LISTENERS;
        public static final int NO_RERENDER = Block.NO_REDRAW;
        public static final int RERENDER_MAIN_THREAD = Block.REDRAW_ON_MAIN_THREAD;
        public static final int NO_OBSERVER_UPDATE = Block.FORCE_STATE;
        public static final int SKIP_DROPS = Block.SKIP_DROPS;
        public static final int CALL_ON_ADDED_ON_REMOVED = Block.MOVED;
        public static final int SKIP_LIGHTING_UPDATES = Block.SKIP_LIGHTING_UPDATES;
        public static final int NO_FILL_UPDATE = 1024;

        public static int modifyFlags(int flags) {
            if (Settings.fillUpdates) return flags;
            return (flags & ~UPDATE_NEIGHBORS) | NO_FILL_UPDATE;
        }
    }
}
