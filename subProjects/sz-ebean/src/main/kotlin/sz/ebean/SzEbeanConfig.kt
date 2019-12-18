package sz.ebean

import com.google.common.reflect.ClassPath
import com.typesafe.config.Config
import com.typesafe.config.ConfigValueType
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ebean.EbeanServerFactory
import io.ebean.config.ServerConfig
import jodd.introspector.ClassIntrospector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import sz.crypto.RsaUtil
import sz.ebean.SzEbeanConfig.hikariConfigKeys
import sz.ebean.registeredClass.JDateTimeConverter
import sz.scaffold.Application
import sz.scaffold.ext.getStringListOrEmpty
import sz.scaffold.tools.BizLogicException
import sz.scaffold.tools.logger.Logger
import java.io.File
import java.util.*
import java.util.concurrent.*

//
// Created by kk on 17/8/20.
//
@Suppress("MemberVisibilityCanBePrivate", "ObjectPropertyName", "unused", "LiftReturnOrAssignment")
object SzEbeanConfig {

    private val ebeanConfig: Config = Application.config.getConfig("ebean")

    private val defaultDatasourceName = ebeanConfig.getString("defaultDatasource")

    private val _ebeanServerConfigs = ConcurrentHashMap<String, ServerConfig>()

    private val workerPoolMap = ConcurrentHashMap<String, ThreadPoolExecutor>()

    private var _defaultDatasourceReady = false
    val defaultDatasourceReady
        get() = _defaultDatasourceReady

    val ebeanServerConfigs: Map<String, ServerConfig>
        get() = _ebeanServerConfigs

    val hasDbConfiged: Boolean

    val hikariConfigKeys by lazy {
        val classDescriptor = ClassIntrospector.get().lookup(HikariConfig::class.java)
        return@lazy classDescriptor.allPropertyDescriptors.filter {
            it.writeMethodDescriptor != null && it.writeMethodDescriptor.isPublic
                && it.readMethodDescriptor != null
                && (it.readMethodDescriptor.rawReturnType.isPrimitive || it.readMethodDescriptor.rawReturnType == String::class.java)
        }.map {
            it.name
        }.toSet()
    }

    private val privateKey by lazy {
        val privateKeyFile = File(ebeanConfig.getString("privateKeyFile"))
        RsaUtil.privateKeyFromPem(privateKeyFile.readText())
    }

    init {
        val dataSources = ebeanConfig.getConfig("dataSources")
        hasDbConfiged = dataSources.root().size > 0
    }

    fun loadConfig() {
        val dataSources = ebeanConfig.getConfig("dataSources")
        val modelClassSet = ebeanModels()

        dataSources.root().keys.forEach { dataSourceName ->

            GlobalScope.launch(Dispatchers.IO) {
                while (true) {
                    try {
                        val dataSourceConfig = dataSources.getConfig(dataSourceName)
                        val dataSourceProps = dataSourceConfig.toProperties()
                        decryptPassword(dataSourceProps)
                        val hikariConfig = HikariConfig(dataSourceProps)
                        val ds = HikariDataSource(hikariConfig)

                        val ebeanServerCfg = ServerConfig()
                        ebeanServerCfg.name = dataSourceName
                        ebeanServerCfg.loadFromProperties()
                        ebeanServerCfg.dataSource = ds
                        ebeanServerCfg.isDefaultServer = ebeanServerCfg.name == defaultDatasourceName
                        ebeanServerCfg.addModelClasses(modelClassSet)

                        EbeanServerFactory.create(ebeanServerCfg)
                        _ebeanServerConfigs[dataSourceName] = ebeanServerCfg

                        val threadFactory = BasicThreadFactory.Builder()
                            .wrappedFactory(Executors.defaultThreadFactory())
                            .namingPattern("ebean-worker-$dataSourceName-%d")
                            .build()

                        workerPoolMap[dataSourceName] = ThreadPoolExecutor(0,
                            ds.maximumPoolSize,
                            60,
                            TimeUnit.SECONDS,
                            LinkedBlockingQueue<Runnable>(1024),
                            threadFactory)

                        if (ebeanServerCfg.isDefaultServer) {
                            _defaultDatasourceReady = true
                        }
                        break

                    } catch (ex: Exception) {
                        workerPoolMap[dataSourceName]?.let {
                            it.shutdown()
                            workerPoolMap.remove(dataSourceName)
                        }
                        Logger.warn("Failed to initialize ebean data source [$dataSourceName]. Caused by: ${ex.message} Try again after 5 seconds.")
                        delay(5000)
                    }
                }

                Logger.info("Successfully initialize the ebean data source [$dataSourceName].")
            }
        }
    }

    private val _dataSourceByTagCache = mutableMapOf("" to arrayOf(""))

    fun dataSourceByTag(tag: String): Array<String> {
        return _dataSourceByTagCache.getOrPut(tag) {
            val dataSources = ebeanConfig.getConfig("dataSources")
            val dsNames = dataSources.root().keys.filter {
                val config = dataSources.getConfig(it)
                config.getStringListOrEmpty("tags").toSet().contains(tag)
            }.toSet().toTypedArray()

            if (dsNames.isEmpty()) {
                throw BizLogicException("There is no data source that matches this tag [$tag], please check application.conf.")
            }
            dsNames
        }
    }

    private fun decryptPassword(props: Properties): Properties {
        if (ebeanConfig.getBoolean("encryptPasswd")) {
            // 数据库密码为加密后的密文, 需要进行解密
            if (props.containsKey("password")) {
                val encryptedTxt = props.getProperty("password")
                props.setProperty("password", RsaUtil.decrypt(encryptedTxt, privateKey))
            }
            return props
        } else {
            return props
        }
    }

    @Suppress("UnstableApiUsage")
    private fun ebeanModels(): Set<Class<*>> {
        val cfgVal = ebeanConfig.getValue("ebeanModels")
        val models = if (cfgVal.valueType() == ConfigValueType.STRING) {
            cfgVal.unwrapped().toString().split(",").map { it.trim() }.toSet()
        } else {
            ebeanConfig.getStringList("ebeanModels").map { it.trim() }.toSet()
        }
        val modelClassSet = mutableSetOf<Class<*>>()
        models.forEach {
            if (it.endsWith(".*")) {
                val packagePath = it.dropLast(2)
                ClassPath.from(Application.classLoader).getTopLevelClassesRecursive(packagePath)
                    .map { classInfo -> classInfo.load() }
                    .filter { clazz -> isEntityClass(clazz) }
                    .forEach { clazz ->
//                        Logger.debug("ebean add class: ${clazz.name}")
                        modelClassSet.add(clazz)
                    }
            } else {
                try {
                    val clazz = Application.classLoader.loadClass(it)
                    modelClassSet.add(clazz)
                } catch (ex: Exception) {
                    Logger.error("Failed to load class [$it] caused by: ${ex.message}")
                }

            }
        }
        modelClassSet.add(JDateTimeConverter::class.java)
        return modelClassSet
    }

    fun jdbcUrl(dataSource: String = defaultDatasourceName): String {
        val dsConfig = ebeanConfig.getConfig("dataSources.$dataSource")
        return dsConfig.getString("jdbcUrl")
    }

    fun isMySql(dataSource: String = defaultDatasourceName): Boolean {
        return jdbcUrl(dataSource).startsWith("jdbc:mysql:")
    }

    fun isH2(dataSource: String = defaultDatasourceName): Boolean {
        return jdbcUrl(dataSource).startsWith("jdbc:h2:")
    }

    fun isHsqldb(dataSource: String = defaultDatasourceName): Boolean {
        return jdbcUrl(dataSource).startsWith("jdbc:hsqldb:")
    }

    fun workerOf(dataSource: String): ExecutorService {
        return workerPoolMap[dataSource] ?: throw RuntimeException("Invalid data source name [$dataSource]")
    }
}

private fun Config.toProperties(): Properties {
    val props = Properties()
    this.root().forEach { key, cfgValue ->
        if (key in hikariConfigKeys) {
            val value = cfgValue.unwrapped()
            if (value != null) {
                props.setProperty(key, cfgValue.unwrapped().toString())
            }
        }
    }
    return props
}

private fun ServerConfig.addModelClasses(modelClasses: Set<Class<*>>) {
    modelClasses.forEach { clazz ->
        this.addModelClass(clazz)
    }
}

private fun ServerConfig.addModelClass(clazz: Class<*>) {
    try {
//        Logger.debug("add class for ebean server: $clazz")
        this.addClass(clazz)
    } catch (ex: Exception) {
        Logger.error("ebean.dataSources.${this.name} cannot register class [${clazz.name}] in ebean server. Caused by: ${ex.message}")
    }
}