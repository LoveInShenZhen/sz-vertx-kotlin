@file:JvmName("MainApp")

package api.server

import sz.SzEbeanConfig
import sz.scaffold.Application


fun main(args: Array<String>) {
    SzEbeanConfig.loadConfig()

    Application.setupVertx()
    Application.run()

}

