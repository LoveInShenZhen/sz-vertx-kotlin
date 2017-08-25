package sz.scaffold.tools.template

import freemarker.cache.ClassTemplateLoader
import freemarker.template.Configuration
import java.io.StringWriter

//
// Created by kk on 17/8/24.
//
object ResourceTemplate {

    private val classConfMap = mutableMapOf<String, Configuration>()

    fun FindConfigBy(clazz: Class<*>): Configuration {
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

    fun Process(clazz: Class<*>, templatePath: String, data: Any): String {
        var sw: StringWriter? = null
        try {
            sw = StringWriter()
            val template = FindConfigBy(clazz).getTemplate(templatePath)
            template!!.process(data, sw)
            return sw.toString()
        } finally {
            if (sw != null) sw.close()
        }
    }
}