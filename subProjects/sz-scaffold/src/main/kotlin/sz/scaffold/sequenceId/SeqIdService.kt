package sz.scaffold.sequenceId

import io.vertx.kotlin.core.eventbus.requestAwait
import sz.scaffold.Application
import sz.scaffold.sequenceId.vertcles.SeqIdServiceVerticle

//
// Created by kk on 2019-06-25.
//
object SeqIdService {

    suspend fun nexIdAwait(): Long {
        return Application.vertx.eventBus().requestAwait<Long>(SeqIdServiceVerticle.idServiceBusAddress, "").body()
    }
}