package sz.annotations

//
// Created by kk on 17/8/20.
//

@Deprecated("No longer in use")
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class DBIndexed
