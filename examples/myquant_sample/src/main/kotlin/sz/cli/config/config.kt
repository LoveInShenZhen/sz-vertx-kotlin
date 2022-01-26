package sz.cli.config

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract

//
// Created by kk on 2021/10/4.
//

data class MyquantConfig(val entry: Entry)

data class Entry(val host: String, val port: Int, val token: String, val orgCode: String)

val config = ConfigFactory.defaultApplication()

val myquantConfig: MyquantConfig = config.extract("myquant")