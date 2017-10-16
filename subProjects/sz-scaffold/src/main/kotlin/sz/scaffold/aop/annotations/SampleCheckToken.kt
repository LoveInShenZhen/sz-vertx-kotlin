package sz.scaffold.aop.annotations


import sz.scaffold.aop.actions.SampleCheckTokenAction
import sz.scaffold.aop.annotations.WithAction

/**
 * Created by frank.zhang on 2017/5/13.
 */
@WithAction(SampleCheckTokenAction::class)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SampleCheckToken(
        val token: String = "token",
        val roles: IntArray = intArrayOf()
)