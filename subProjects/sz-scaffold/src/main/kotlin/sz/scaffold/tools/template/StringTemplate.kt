package sz.scaffold.tools.template

import freemarker.cache.StringTemplateLoader
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateNotFoundException
import jodd.crypt.DigestEngine
import java.io.StringWriter

//
// Created by kk on 2018/10/8.
//
object StringTemplate {

    private val templateConf: Configuration = Configuration(Configuration.VERSION_2_3_21)
    private val stringTemplateLoader = StringTemplateLoader()

    init {
        templateConf.defaultEncoding = "UTF-8"
        templateConf.templateLoader = stringTemplateLoader

    }

    @Suppress("NAME_SHADOWING")
    fun process(templateStr: String, data: Any): String {
        val sw = StringWriter()
        sw.use { sw ->
            val template = getTemplate(templateStr)
            template.process(data, sw)
            return sw.toString()
        }
    }

    private fun getTemplate(templateStr: String): Template {
        val name = templateNameOf(templateStr)
        try {
            return templateConf.getTemplate(name)
        } catch (ex: TemplateNotFoundException) {
            stringTemplateLoader.putTemplate(name, templateStr)
        }
        return templateConf.getTemplate(name)
    }

    private fun templateNameOf(templateStr: String): String {
        return DigestEngine.md5().digestString(templateStr)
    }
}