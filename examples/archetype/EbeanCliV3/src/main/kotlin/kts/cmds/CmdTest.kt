package kts.cmds

import com.github.ajalt.clikt.core.CliktCommand

//
// Created by drago on 2024/8/8 周四.
//
class CmdTest : CliktCommand(name = "test", help = "仅开发时临时测试") {
    override fun run() {
        println("仅开发时临时测试")
    }
}