package quickcarpet.build;

import java.io.*;
import java.util.Arrays;
import java.util.stream.Stream;

public class GitHelper {
    public static String run(File dir, String command) {
        try {
            StringBuilder result = new StringBuilder();
            Process proc = Runtime.getRuntime().exec("git " + command, null, dir);
            lines(proc.getInputStream()).forEach(line -> result.append(line).append('\n'));
            lines(proc.getErrorStream()).forEach(System.out::println);
            proc.waitFor();
            return result.toString().trim();
        } catch (IOException|InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Stream<String> lines(InputStream in) {
        return new BufferedReader(new InputStreamReader(in)).lines();
    }

    public static String getCommit(File dir) {
        return run(dir, "rev-parse HEAD");
    }

    public static String getBranch(File dir) {
        String branch = run(dir, "rev-parse --abbrev-ref HEAD");
        return branch.substring(branch.lastIndexOf('/') + 1);
    }

    public static boolean getStatus(File dir) {
        try {
            // not added
            int exitCode = Runtime.getRuntime().exec("git diff --quiet", null, dir).waitFor();
            if (exitCode == 0) {
                // not committed
                exitCode = Runtime.getRuntime().exec("git diff --cached --quiet", null, dir).waitFor();
            }
            return exitCode == 0;
        } catch (IOException|InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static SemVer getVersion(File dir, String next) {
        String[] description = run(dir, "describe --tags --dirty --match v*.*.*").split("-");
        if (description.length == 1) return new SemVer(description[0].substring(1));
        SemVer semver = new SemVer(next);
        if (semver.pre.isEmpty()) semver.pre.add("dev");
        semver.pre.add(description[1]);
        semver.build.addAll(Arrays.asList(Arrays.copyOfRange(description, 2, description.length)));
        return semver;
    }
}
