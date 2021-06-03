class SemVer {
    public int major
    public int minor
    public int patch
    public List<String> pre
    public List<String> build

    public SemVer(int major, int minor, int patch, List<String> pre, List<String> build) {
        this.major = major
        this.minor = minor
        this.patch = patch
        this.pre = pre == null ? new ArrayList<>() : new ArrayList<>(pre)
        this.build = build == null ? new ArrayList<>() : new ArrayList<>(build)
    }

    public SemVer(String version) {
        int minus = version.indexOf('-')
        int plus = version.indexOf('+', minus + 1)
        int endMinus = plus >= 0 ? plus : version.length()
        int endBase = minus >= 0 ? minus : endMinus
        String[] base = version.substring(0, endBase).split("\\.")
        if (base.length != 3) throw new IllegalArgumentException("Expected 3 numbers")
        this.major = Integer.parseInt(base[0])
        this.minor = Integer.parseInt(base[1])
        this.patch = Integer.parseInt(base[2])
        this.pre = minus >= 0 ? version.substring(minus + 1, endMinus).split("\\.").toList() : new ArrayList<>()
        this.build = plus >= 0 ? version.substring(plus + 1).split("\\.").toList() : new ArrayList<>()
    }

    @Override
    String toString() {
        StringBuilder sb = new StringBuilder()
        sb.append(major).append('.').append(minor).append('.').append(patch)
        if (!pre.isEmpty()) {
            for (int i = 0; i < pre.size(); i++) {
                sb.append(i == 0 ? '-' : '.')
                sb.append(pre[i])
            }
        }
        if (!build.isEmpty()) {
            for (int i = 0; i < build.size(); i++) {
                sb.append(i == 0 ? '+' : '.')
                sb.append(build[i])
            }
        }
        return sb.toString()
    }
}
