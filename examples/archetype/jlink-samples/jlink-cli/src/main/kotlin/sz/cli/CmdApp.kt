package sz.cli

import sz.cli.cmds.Hello

//
// Created by kk on 2021/9/20.
//
class CmdApp {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Hello().main(args)
        }
    }

}