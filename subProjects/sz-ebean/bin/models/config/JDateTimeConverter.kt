package models.config

import io.ebean.config.ScalarTypeConverter
import jodd.datetime.JDateTime
import java.util.*

//
// Created by kk on 17/8/20.
//

class JDateTimeConverter : ScalarTypeConverter<JDateTime, Date> {
    override fun getNullValue(): JDateTime? {
        return null
    }

    override fun wrapValue(scalarType: Date): JDateTime {
        return JDateTime(scalarType)
    }

    override fun unwrapValue(beanType: JDateTime): Date {
        return beanType.convertToDate()
    }
}
