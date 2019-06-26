package sz.scaffold.aop.interceptors

import io.vertx.core.json.JsonObject
import sz.scaffold.aop.actions.Action

//
// Created by kk on 2019-06-26.
//
abstract class GlobalInterceptorBase : Action<JsonObject>()