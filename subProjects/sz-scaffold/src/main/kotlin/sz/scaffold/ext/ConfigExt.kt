package sz.scaffold.ext

import com.typesafe.config.Config

//
// Created by kk on 17/8/19.
//

fun Config.getString(path: String, defaultValue: String): String {
    val v = this.getString(path)
    if (v.isNullOrBlank()) {
        return defaultValue
    } else {
        return v
    }
}