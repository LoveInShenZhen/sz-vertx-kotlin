package sz.scaffold.tools.json

//
// Created by kk on 17/8/16.
//

fun Any.toJsonPretty() : String {
    return Json.toJsonStrPretty(this)
}