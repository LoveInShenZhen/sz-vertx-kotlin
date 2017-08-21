package aop.annotations


import sz.scaffold.aop.actions.CheckTokenAction
import sz.scaffold.aop.annotations.WithAction

/**
 * Created by frank.zhang on 2017/5/13.
 */
@WithAction(CheckTokenAction::class)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CheckToken(
        val token: String = "token",
        val roles: IntArray = intArrayOf()
)