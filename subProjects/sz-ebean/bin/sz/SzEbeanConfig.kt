package sz

import com.google.common.reflect.ClassPath
import com.typesafe.config.Config
import com.typesafe.config.ConfigValueType
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ebean.EbeanServerFactory
import io.ebean.config.ServerConfig
import sz.scaffold.Application
import sz.scaffold.tools.logger.Logger
import java.util.*

//
// Created by kk on 17/8/20.
//
object SzEbeanConfig {

    private val ebeanConfig: Config
    private val defaultDatasourceName: String

    val hasDbConfiged:Boolean

    init {
        ebeanConfig = Application.config.getConfig("ebean")
        defaultDatasourceName = ebeanConfig.getString("defaultDatasource")
        val dataSources = ebeanConfig.getConfig("dataSources")
        hasDbConfiged = dataSources.root().size > 0
    }

    fun loadConfig() {
        val dataSources = ebeanConfig.getConfig("dataSources")
        dataSources.root().keys.forEach {
            val dataSourceName = it
            val dataSourceConfig = dataSources.getConfig(it)
            val dataSourceProps = dataSourceConfig.toProperties()
            val hiDsConfig = HikariConfig(dataSourceProps)
            val ds = HikariDataSource(hiDsConfig)

            val ebeanServerCfg = ServerConfig()
            ebeanServerCfg.name = dataSourceName
            ebeanServerCfg.loadFromProperties()
            ebeanServerCfg.dataSource = ds

            if (ebeanServerCfg.name == defaultDatasourceName) {
                ebeanServerCfg.isDefaultServer = true
            }

            val modelClassSet = modelsOfDatasource(dataSourceConfig)
            ebeanServerCfg.addModelClasses(modelClassSet)

            EbeanServerFactory.create(ebeanServerCfg)
        }
    }

    private fun modelsOfDatasource(datasourceCfg: Config): Set<String> {
        if (datasourceCfg.hasPath("ebeanModels")) {
            val cfgVal = datasourceCfg.getValue("ebeanModels")
            if (cfgVal.valueType() == ConfigValueType.STRING) {
                return cfgVal.unwrapped().toString().split(",").map { it.trim() }.toSet()
            } else {
                return datasourceCfg.getStringList("ebeanModels").map { it.trim() }.toSet()
            }
        } else {
            return setOf("models.*")
        }
    }
}

private fun Config.toProperties(): Properties {
    val props = Properties()
    this.root().forEach { key, cfgValue ->
        if (cfgValue.valueType() in arrayOf(ConfigValueType.NUMBER,
                ConfigValueType.STRING,
                ConfigValueType.BOOLEAN,
                ConfigValueType.NULL)) {
            props.setProperty(key, cfgValue.unwrapped().toString())
        }
    }
    return props
}

private fun ServerConfig.addModelClasses(modelClasses: Set<String>) {
    modelClasses.forEach {
        if (it.endsWith(".*")) {
            val packagePath = it.dropLast(2)
            ClassPath.from(Application.classLoader).getTopLevelClassesRecursive(packagePath).forEach { classInfo ->
                this.addModelClass(classInfo.load())
            }
        } else {
            try {
                val clazz = Application.classLoader.loadClass(it)
                this.addModelClass(clazz)
            } catch (ex: Exception) {
                Logger.debug("Load class: [$it] failed. For reason: ${ex.message}")
            }

        }
    }
}

private fun ServerConfig.addModelClass(clazz: Class<*>) {
    try {
        Logger.debug("add class for ebean server: $clazz")
        this.addClass(clazz)
    } catch (ex:Exception) {
        Logger.debug("ebean.dataSources.${this.name} Cannot register class [${clazz}] in Ebean server. For reason:${ex.message}")
    }
}