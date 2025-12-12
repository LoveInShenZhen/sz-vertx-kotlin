package sz.scaffold.sequenceId

import java.time.LocalDateTime
import java.time.ZoneOffset

//
// Created by kk on 2019-06-25.
//
class SeqIdObject(val idNum: Long) {

    val idNumBin: String
        get() {
            return java.lang.Long.toBinaryString(idNum)
        }

    val workerId: Long
        get() {
            return idNum and IdGenerator.maxWorkerId
        }

    val workerIdBin: String
        get() {
            return java.lang.Long.toBinaryString(workerId)
        }

    val epochSecond: Long
        get() {
            return (idNum shr 21) and 0b1111_1111_1111_1111_1111_1111_1111_1111
        }

    val epochSecondBin: String
        get() {
            return java.lang.Long.toBinaryString(epochSecond)
        }

    val idOffset: Long
        get() {
            return (idNum shr 5) and IdGenerator.maxNext
        }

    val idOffsetBin: String
        get() {
            return java.lang.Long.toBinaryString(idOffset)
        }

    val createTime: LocalDateTime
        get() {
//            val dateTime = Date((epochSecond + IdGenerator.OffsetEpoch) * 1000)
//            return JDateTime(dateTime)
            return LocalDateTime.ofEpochSecond(epochSecond + IdGenerator.OffsetEpoch, 0, ZoneOffset.UTC)
        }


}
