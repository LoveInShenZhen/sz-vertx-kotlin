package sz.scaffold.tools.json

//
// Created by kk on 17/8/16.
//

fun Any.toJsonPretty() : String {
    return Json.toJsonStrPretty(this)
}

fun Any.toShortJson() : String {
    return Json.toJsonExcludeEmptyFields(this)
}