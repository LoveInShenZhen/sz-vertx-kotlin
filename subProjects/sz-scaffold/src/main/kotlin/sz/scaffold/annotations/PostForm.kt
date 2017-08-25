package sz.scaffold.annotations

import kotlin.reflect.KClass

//
// Created by kk on 17/8/24.
//

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class PostForm(val value: KClass<*> = Any::class)