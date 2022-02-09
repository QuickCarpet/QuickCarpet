package quickcarpet.build

import java.io.*
import java.util.stream.Stream

object GitHelper {
    fun run(dir: File, command: String): String {
        return try {
            val result = StringBuilder()
            val proc = Runtime.getRuntime().exec("git $command", null, dir)
            lines(proc.inputStream).forEach {
                result.append(it).append('\n')
            }
            lines(proc.errorStream).forEach(::println)
            proc.waitFor()
            result.toString().trim { it <= ' ' }
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    private fun lines(`in`: InputStream): Stream<String> {
        return BufferedReader(InputStreamReader(`in`)).lines()
    }

    fun getCommit(dir: File): String {
        return run(dir, "rev-parse HEAD")
    }

    fun getBranch(dir: File): String {
        val branch = run(dir, "rev-parse --abbrev-ref HEAD")
        return branch.substring(branch.lastIndexOf('/') + 1)
    }

    fun getStatus(dir: File): Boolean {
        return try {
            // not added
            var exitCode = Runtime.getRuntime().exec("git diff --quiet", null, dir).waitFor()
            if (exitCode == 0) {
                // not committed
                exitCode = Runtime.getRuntime().exec("git diff --cached --quiet", null, dir).waitFor()
            }
            exitCode == 0
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    fun getVersion(dir: File, next: String): SemVer {
        val description = run(dir, "describe --tags --dirty --match v*.*.*").split("-")
        if (description.size == 1) return SemVer(description[0].substring(1))
        val semver = SemVer(next)
        if (semver.pre.isEmpty()) semver.pre.add("dev")
        semver.pre.add(description[1])
        semver.build.addAll(description.subList(2, description.size))
        return semver
    }
}