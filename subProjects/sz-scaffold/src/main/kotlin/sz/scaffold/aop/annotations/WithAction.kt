package sz.scaffold.aop.annotations

import sz.scaffold.controller.ApiController
import kotlin.reflect.KClass

//
// Created by kk on 17/8/16.
//

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class WithAction(val value: KClass<*> = ApiController::class)