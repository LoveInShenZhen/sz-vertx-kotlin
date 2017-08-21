package sz.scaffold.tools

/**
 * Created by kk on 14-6-11.
 */
class BizLogicException : RuntimeException {

    var ErrCode: Int = 0

    constructor(msg: String) : super(msg) {
        this.ErrCode = -1
    }

    constructor(errCode: Int, msg: String) : super(msg) {
        this.ErrCode = errCode
    }

    constructor(format: String, vararg args: Any) : super(String.format(format, *args)) {
        this.ErrCode = -1
    }
}
