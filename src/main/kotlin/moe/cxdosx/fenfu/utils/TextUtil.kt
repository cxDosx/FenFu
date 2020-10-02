package moe.cxdosx.fenfu.utils

object TextUtil {
    /**
     * 转换数值可读性
     */
    fun convertNumber(i: Int): String {
        return if (i < 10000) {
            i.toString()
        } else {
            val r: Double = i / 10000.toDouble()
            String.format("%.2f万", r)
        }
    }

}