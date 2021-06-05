class GitHelper {
    static def run(File dir, String command) {
        def result = ""
        def proc = "git $command".execute(null, dir)
        proc.in.eachLine { line -> result = line }
        proc.err.eachLine { line -> println line }
        proc.waitFor()
        result
    }

    static def getCommit(File dir) {
        run(dir, "rev-parse HEAD")
    }

    static def getBranch(File dir) {
        def branch = run(dir, "rev-parse --abbrev-ref HEAD")
        branch.substring(branch.lastIndexOf('/') + 1)
    }

    static def getStatus(File dir) {
        def exitCode = "git diff --quiet".execute(null, dir).waitFor() // not added
        if (exitCode == 0) {
            exitCode = "git diff --cached --quiet".execute(null, dir).waitFor() // not committed
        }
        exitCode == 0
    }

    static SemVer getVersion(File dir, String next) {
        def description = run(dir, "describe --tags --dirty --match v*.*.*").split("-")
        if (description.length == 1) return description[0].substring(1)
        def semver = new SemVer(next)
        if (semver.pre.isEmpty()) semver.pre.add("dev")
        semver.pre.add(description[1])
        semver.build.addAll(Arrays.copyOfRange(description, 2, description.length))
        semver
    }
}
