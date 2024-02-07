package sz.scaffold.tools.template

import freemarker.template.Configuration
import jodd.exception.ExceptionUtil
import sz.logger.log
import sz.scaffold.Application
import java.io.File
import java.io.IOException
import java.io.StringWriter

//
// Created by kk on 17/8/24.
//
object FileTemplate {

    private val templateConf: Configuration = Configuration(Configuration.VERSION_2_3_21)

    init {
        templateConf.defaultEncoding = "UTF-8"
        try {
            templateConf.setDirectoryForTemplateLoading(templateDir())
        } catch (e: IOException) {
            log.error(ExceptionUtil.exceptionStackTraceToString(e))
        }

    }

    private fun templateDir(): File {
        // 约定, 所有的模板文件都放在 /conf 目录下
        return Application.getFile("${File.separator}conf")
    }

    // TemplatePath 为在 /conf 目录下的相对路径
    @Suppress("NAME_SHADOWING")
    fun process(TemplatePath: String, data: Any): String {
        val sw = StringWriter()
        sw.use { sw ->
            val template = templateConf.getTemplate(TemplatePath)
            template.process(data, sw)
            return sw.toString()
        }
    }
}