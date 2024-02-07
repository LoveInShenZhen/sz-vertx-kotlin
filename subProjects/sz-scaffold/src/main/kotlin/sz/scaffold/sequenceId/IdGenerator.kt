package sz.scaffold.sequenceId

import jodd.datetime.JDateTime
import sz.logger.log
import sz.scaffold.tools.SzException

/**
 * 53 bits sequence Id:
 *
 * |--------|--------|--------|--------|--------|--------|--------|--------|
 * |00000000|00011111|11111111|11111111|11111111|11111111|11111111|11111111|
 * |--------|---xxxxx|xxxxxxxx|xxxxxxxx|xxxxxxxx|xxx-----|--------|--------|
 * |--------|--------|--------|--------|--------|---xxxxx|xxxxxxxx|xxx-----|
 * |--------|--------|--------|--------|--------|--------|--------|---xxxxx|
 *
 * Maximum ID = 11111_11111111_11111111_11111111_11111111_11111111_11111111
 *
 * Maximum TS = 11111_11111111_11111111_11111111_111
 *
 * Maximum Next = ----- -------- -------- -------- ---11111_11111111_111 = 65535
 *
 * Maximum WorkerId = ----- -------- -------- -------- -------- -------- ---11111 = 31
 *
 * It can generate 64k unique id per IP and up to 2155-07-08 18:28:15.
 *
 * 为什么采用最多53位整型，而不是64位整型？这是因为考虑到大部分应用程序是Web应用，如果要和JavaScript打交道，
 * 由于JavaScript支持的最大整型就是53位，超过这个位数，JavaScript将丢失精度。因此，使用53位整数可以直接由JavaScript读取，而超过53位时，
 * 就必须转换成字符串才能保证JavaScript处理正确，这会给API接口带来额外的复杂度。这也是为什么新浪微博的API接口会同时返回 id 和 idstr 的原因。
 */
class IdGenerator(val workerId: Long) {
    private var lastEpoch: Long = 0
    private var idOffset: Long = 0


    init {
        checkWorkerId(workerId)
    }

    private fun checkWorkerId(value: Long) {
        if (value < minWokerId || value > maxWorkerId) {
            throw SzException("Invalid woker id: $value")
        }
    }

    fun nextId(): Long {
        return nextIdInner(System.currentTimeMillis() / 1000)
    }

    @Synchronized
    private fun nextIdInner(epochSecond: Long): Long {
        var epoch = epochSecond
        if (epoch < lastEpoch) {
            log.warn("clock is back: $epoch from previous: $lastEpoch. [diff: ${lastEpoch - epoch} secs] or reach the maximum id per second, epochSecond borrow from future.")
            epoch = lastEpoch
        }

        if (lastEpoch != epoch) {
            // 不在同一秒内
            lastEpoch = epoch
            idOffset = 0
        }
        idOffset++
        val next = idOffset and maxNext
        if (next == 0L) {
            log.warn("maximum id reached in 1 second in epoch: $epoch")
            return nextIdInner(epoch + 1)
        }
        return generateId(epoch, next)
    }

    private fun generateId(epochSecond: Long, next: Long): Long {
        return ((epochSecond - OffsetEpoch) shl 21) or (next shl 5) or workerId
    }

    companion object {
        val OffsetEpoch = JDateTime("2019-06-01 12:00:00", "YYYY-MM-DD hh:mm:ss").convertToDate().time / 1000
        val minWokerId: Long = 0
        val maxWorkerId: Long = 0b1111_1
        val maxNext: Long = 0b1111_1111_1111_1111 // 65535
    }
}