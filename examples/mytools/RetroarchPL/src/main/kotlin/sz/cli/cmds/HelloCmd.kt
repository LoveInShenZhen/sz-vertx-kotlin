package sz.cli.cmds

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import org.slf4j.LoggerFactory

//
// Created by kk on 2021/9/20.
//
class Hello : CliktCommand() {
    val count: Int by option(help="Number of greetings").int().default(3)
    val name: String by option(help="The person to greet").default("KK")

    override fun run() {
        repeat(count) {
            echo("Hello $name!")
        }
        logger.info("Hello $name!")
    }

    companion object {
        val logger = LoggerFactory.getLogger("HelloCommand")!!
    }
}