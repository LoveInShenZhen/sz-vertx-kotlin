package sz.scaffold.tools

/**
 * 版本号, 例如: v1.2.3
 * 首字母 v 或者 大写V, 可以省略
 * 版本号数字有3个部分组成:
 * Major : 主版本号
 * Minor : 次版本号
 * Revision: 修订号
 */
class VerNumber() : Comparable<VerNumber> {
    var Major: Int = 0
    var Minor: Int = 0
    var Revision: Int = 0

    override fun compareTo(other: VerNumber): Int {
        // 先比较主版本号
        if (this.Major > other.Major) {
            return 1
        }
        if (this.Major < other.Major) {
            return -1
        }
        // 主版本号相同, 比较次版本号
        if (this.Minor > other.Minor) {
            return 1
        }
        if (this.Minor < other.Minor) {
            return -1
        }
        // 次版本号也相同, 比较修订号
        if (this.Revision > other.Revision) {
            return 1
        }
        if (this.Revision < other.Revision) {
            return -1
        }
        // 主版本号,次版本号,修订号都相同
        return 0
    }

    override fun toString(): String {
        return "v${Major}.${Minor}.${Revision}"
    }

    /**
     * 用于排序的分数, 这里做了一个假设限制,主,次,修订号这3个版本号的数值要小于 9999
     */
    fun score(): Int {
        return this.Major * 1_0000_0000 + this.Minor * 1_0000 + this.Revision
    }

    companion object {

        fun parse(version: String): VerNumber {
            val parts = version.trim().lowercase().removePrefix("v").split(".")
            if (parts.size != 3) {
                throw IllegalArgumentException("版本号字符串不符合规范: $version")
            }

            val ver = VerNumber()
            try {
                ver.Major = parts[0].toInt()
                ver.Minor = parts[1].toInt()
                ver.Revision = parts[2].toInt()
            } catch (ex: Exception) {
                throw IllegalArgumentException("版本号字符串不符合规范: $version")
            }

            return ver
        }
    }
}