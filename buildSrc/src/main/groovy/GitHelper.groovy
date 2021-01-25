class GitHelper {
    static def run(String command) {
        def result = ""
        def proc = "git $command".execute()
        proc.in.eachLine { line -> result = line }
        proc.err.eachLine { line -> println line }
        proc.waitFor()
        result
    }

    static def getCommit() {
        run("rev-parse HEAD")
    }

    static def getBranch() {
        def branch = run("rev-parse --abbrev-ref HEAD")
        branch.substring(branch.lastIndexOf('/') + 1)
    }

    static def getStatus() {
        def exitCode = "git diff --quiet".execute().waitFor() // not added
        if (exitCode == 0) {
            exitCode = "git diff --cached --quiet".execute().waitFor() // not committed
        }
        exitCode == 0
    }
}
