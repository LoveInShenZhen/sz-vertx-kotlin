@file:JvmName("MainApp")

package api.server

import sz.SzEbeanConfig
import sz.scaffold.Application
import sz.scaffold.controller.reply.ReplyBase
import sz.scaffold.tools.json.Json
import sz.scaffold.tools.json.toJsonPretty
import sz.scaffold.tools.logger.Logger


fun main(args: Array<String>) {
    SzEbeanConfig.loadConfig()

    val replySchema = Json.generateSchema(ReplyBase::class)

    Logger.debug(replySchema.toJsonPretty())

    Application.setupVertx()
    Application.run()

}

