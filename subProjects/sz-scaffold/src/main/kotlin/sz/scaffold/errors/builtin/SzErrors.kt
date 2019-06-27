package sz.scaffold.errors.builtin

//
// Created by kk on 2019-06-27.
//
enum class SzErrors(val code: Int, val desc: String) {
    UnDefined(-1, "未明确定义的错误"),
    ExceedQpsLimit(-2, "达到Qps限流器的限制")
}