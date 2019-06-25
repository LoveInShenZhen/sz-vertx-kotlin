package sz.scaffold.sequenceId.exceptions

//
// Created by kk on 2019-06-25.
//
class FailedToGetWorkerId(private val causeEx: Throwable) : RuntimeException() {

    override val cause: Throwable?
        get() = causeEx

    override val message: String?
        get() = "Failed to get wokerId by reason: ${causeEx.message}"
}