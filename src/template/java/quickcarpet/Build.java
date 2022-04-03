package quickcarpet;

public final class Build {
    public static final String ID = "quickcarpet";
    public static final String NAME = "Quick Carpet";
    public static final String VERSION = "${version}";
    public static final boolean VERSION_IS_DEV = ${version.contains("-dev")};
    public static final String COMMIT = "${commit}";
    public static final String COMMIT_SHORT = "${commit.substring(0, 7)}";
    public static final String BRANCH = "${branch}";
    public static final String BUILD_TIMESTAMP = "${timestamp}";
    public static final String MINECRAFT_VERSION = "${minecraft_version}";
    public static final String YARN_MAPPINGS = "${yarn_mappings}";
    public static final boolean WORKING_DIR_CLEAN = ${working_dir_clean};
}
