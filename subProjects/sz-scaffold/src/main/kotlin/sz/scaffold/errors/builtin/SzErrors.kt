package sz.scaffold.errors.builtin

//
// Created by kk on 2019-06-27.
//
enum class SzErrors(val code: Int, val desc: String) {
    UnDefined(-1, "Undefined error"),
    ExceedQpsLimit(-2, "Reach the limit of QPS")
}