package sz.scaffold.tools.template

import freemarker.template.Configuration
import jodd.exception.ExceptionUtil
import sz.scaffold.Application
import sz.scaffold.tools.logger.Logger
import java.io.File
import java.io.IOException
import java.io.StringWriter

//
// Created by kk on 17/8/24.
//
object FileTemplate {

    private val templateConf: Configuration

    init {
        templateConf = Configuration(Configuration.VERSION_2_3_21)
        templateConf.defaultEncoding = "UTF-8"
        try {
            templateConf.setDirectoryForTemplateLoading(TemplateDir())
        } catch (e: IOException) {
            Logger.error(ExceptionUtil.exceptionStackTraceToString(e))
        }

    }

    private fun TemplateDir(): File {
        // 约定, 所有的模板文件都放在 /conf 目录下
        return Application.getFile("/conf")
    }

    // TemplatePath 为在 /conf 目录下的相对路径
    fun Process(TemplatePath: String, data: Any): String {
        var sw: StringWriter? = null
        try {
            sw = StringWriter()
            val template = templateConf.getTemplate(TemplatePath)
            template.process(data, sw)
            return sw.toString()
        } finally {
            if (sw != null) sw.close()
        }
    }
}