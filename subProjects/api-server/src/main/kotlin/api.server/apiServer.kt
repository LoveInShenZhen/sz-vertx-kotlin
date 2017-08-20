@file:JvmName("MainApp")

package api.server

import models.User
import sz.SzEbeanConfig
import sz.scaffold.Application
import sz.scaffold.tools.json.toJsonPretty
import sz.scaffold.tools.logger.Logger

fun main(args: Array<String>) {

    SzEbeanConfig.loadConfig()

    val sql = "select * from user"

    User.all().forEach {
        Logger.debug(it.toJsonPretty())
    }

    Application.run()

}