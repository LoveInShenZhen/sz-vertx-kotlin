package sz.scaffold.ext

import com.typesafe.config.Config

//
// Created by kk on 17/8/19.
//

fun Config.getStringOrElse(path: String, defaultValue: String): String {
    if (this.hasPath(path)) {
        val v = this.getString(path)
        if (v.isNullOrBlank()) {
            return defaultValue
        } else {
            return v
        }
    } else {
        return defaultValue
    }
}

fun Config.getIntOrElse(path: String, defaultValue: Int): Int {
    if (this.hasPath(path)) {
        return this.getInt(path)
    } else {
        return defaultValue
    }
}

fun Config.getBooleanOrElse(path: String, defaultValue: Boolean): Boolean {
    if (this.hasPath(path)) {
        return this.getBoolean(path)
    } else {
        return defaultValue
    }
}