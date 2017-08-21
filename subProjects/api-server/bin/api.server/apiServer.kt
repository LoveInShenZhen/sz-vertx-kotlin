@file:JvmName("MainApp")

package api.server

import jodd.util.ClassLoaderUtil
import models.User
import sz.SzEbeanConfig
import sz.scaffold.Application
import sz.scaffold.tools.json.toJsonPretty
import sz.scaffold.tools.logger.Logger

fun main(args: Array<String>) {
    Application.addConfToClassPath()

    SzEbeanConfig.loadConfig()

    User.all().forEach {
        Logger.debug(it.toJsonPretty())
    }

    ClassLoaderUtil.getDefaultClasspath(Application.classLoader).forEach {
        Logger.debug("class path: ${it.absolutePath}")
    }

    Application.run()

}