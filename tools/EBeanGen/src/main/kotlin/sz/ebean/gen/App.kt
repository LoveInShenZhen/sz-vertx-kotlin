package sz.ebean.gen

import sz.ebean.gen.commands.GenBean

//
// Created by kk on 2021/5/5.
//

/// Knowing the Name of Your Main Class
/// https://stackoverflow.com/questions/14733566/how-to-run-kotlin-class-from-the-command-line?answertab=votes#tab-top

object App {
    @JvmStatic
    fun main(args: Array<String>) {
        GenBean().main(args)
    }
}

//fun main(args: Array<String>) {
//    GenBean().main(args)
//}

