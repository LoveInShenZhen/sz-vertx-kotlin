package sz.scaffold.aop.interceptors.builtin.api

//
// Created by kk on 2019/12/30.
//

import sz.scaffold.Application
import sz.scaffold.aop.actions.Action
import sz.scaffold.aop.annotations.WithAction

@WithAction(DevModeOnlyAction::class)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DevModeOnly

class DevModeOnlyAction : Action<DevModeOnly>() {

    override suspend fun call(): Any? {
        return if (Application.inProductionMode) {
            this.httpContext.response().setStatusCode(404).end()
        } else {
            delegate.call()
        }
    }
}