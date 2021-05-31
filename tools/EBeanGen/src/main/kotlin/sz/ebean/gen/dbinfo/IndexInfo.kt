package sz.ebean.gen.dbinfo

//
// Created by kk on 2021/5/23.
//
class IndexInfo {
    var column_name = ""
    var non_unique = false
    val unique: Boolean
        get() = non_unique.not()
}