package quickcarpet.utils;

import net.minecraft.block.Block;
import quickcarpet.settings.Settings;

public final class Constants {
    private Constants() {}

    public static final class SetBlockState {
        private SetBlockState() {}

        public static final int DEFAULT = Block.field_31036;

        public static final int UPDATE_NEIGHBORS = Block.field_31027;
        public static final int SEND_TO_CLIENT = Block.field_31028;
        public static final int NO_RERENDER = Block.field_31029;
        public static final int RERENDER_MAIN_THREAD = Block.field_31030;
        public static final int NO_OBSERVER_UPDATE = Block.field_31031;
        public static final int FLAG_32 = Block.field_31032;
        public static final int CALL_ON_ADDED_ON_REMOVED = Block.field_31033;
        public static final int CHECK_LIGHT = Block.field_31033;
        public static final int NO_FILL_UPDATE = 1024;

        public static int modifyFlags(int flags) {
            if (Settings.fillUpdates) return flags;
            return (flags & ~UPDATE_NEIGHBORS) | NO_FILL_UPDATE;
        }
    }
}
