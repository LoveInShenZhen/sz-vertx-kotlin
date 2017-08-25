package sz.scaffold.ext

//
// Created by kk on 17/8/24.
//

fun String?.escapeMarkdown(): String {
    if (this.isNullOrBlank()) return ""
    return this!!.replace("_", """\_""").replace("*", """\*""")
}