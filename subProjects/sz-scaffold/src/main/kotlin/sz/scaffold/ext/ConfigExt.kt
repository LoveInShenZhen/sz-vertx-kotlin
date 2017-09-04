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

fun Config.getStringListOrEmpty(path: String): List<String> {
    if (this.hasPath(path)) {
        return this.getStringList(path)
    } else {
        return listOf()
    }
}

fun Config.getStringOrNll(path: String):String? {
    return if (this.hasPath(path)) {
        this.getString(path)
    } else {
        null
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

fun Config.existThenApply(path: String, block: ()->Unit) {
    if (this.hasPath(path)) {
        block()
    }
}