package sz.scaffold.tools.template

import freemarker.cache.ClassTemplateLoader
import freemarker.template.Configuration
import java.io.StringWriter

//
// Created by kk on 17/8/24.
//
object ResourceTemplate {

    private val classConfMap = mutableMapOf<String, Configuration>()

    fun findConfigBy(clazz: Class<*>): Configuration {
        if (classConfMap.containsKey(clazz.typeName)) {
            return classConfMap[clazz.typeName]!!
        } else {
            val config = Configuration(Configuration.VERSION_2_3_23)
            config.defaultEncoding = "UTF-8"
            val loader = ClassTemplateLoader(clazz, "/")
            config.templateLoader = loader
            classConfMap[clazz.typeName] = config
            return config
        }
    }

    @Suppress("NAME_SHADOWING")
    fun process(clazz: Class<*>, templatePath: String, data: Any): String {
        val sw = StringWriter()
        sw.use { sw ->
            val template = findConfigBy(clazz).getTemplate(templatePath)
            template.process(data, sw)
            return sw.toString()
        }
    }
}