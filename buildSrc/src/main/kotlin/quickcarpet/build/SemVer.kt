package quickcarpet.build

data class SemVer(var major: Int, var minor: Int, var patch: Int, var pre: MutableList<String> = mutableListOf(), var build: MutableList<String> = mutableListOf()) {
    constructor(version: String) : this(0, 0, 0) {
        val minus = version.indexOf('-')
        val plus = version.indexOf('+', minus + 1)
        val endMinus = if (plus >= 0) plus else version.length
        val endBase = if (minus >= 0) minus else endMinus
        val base = version.substring(0, endBase).split(".")
        require(base.size == 3) { "Expected 3 numbers" }
        major = base[0].toInt()
        minor = base[1].toInt()
        patch = base[2].toInt()
        if (minus >= 0) pre.addAll(version.substring(minus + 1, endMinus).split('.'))
        if (plus >= 0) build.addAll(version.substring(plus + 1).split('.'))
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(major).append('.').append(minor).append('.').append(patch)
        if (pre.isNotEmpty()) {
            for (i in pre.indices) {
                sb.append(if (i == 0) '-' else '.')
                sb.append(pre[i])
            }
        }
        if (build.isNotEmpty()) {
            for (i in build.indices) {
                sb.append(if (i == 0) '+' else '.')
                sb.append(build[i])
            }
        }
        return sb.toString()
    }
}
