package sz

import io.ebean.Model

//
// Created by kk on 2018/4/19.
//
open class BaseModel(val dataSource: String = "") : Model() {

    override fun save() {
        if (this.dataSource.isBlank()) {
            super.save()
        } else {
            db(dataSource).save(this)
        }
    }
}