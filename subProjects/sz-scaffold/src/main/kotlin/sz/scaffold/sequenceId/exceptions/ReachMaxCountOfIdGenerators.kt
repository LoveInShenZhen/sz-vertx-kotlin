package sz.scaffold.sequenceId.exceptions

//
// Created by kk on 2019-06-25.
//
class ReachMaxCountOfIdGenerators : RuntimeException() {
    override val message: String?
        get() = "Id生成器个数已经达到上限"
}