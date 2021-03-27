package sz.scaffold.sequenceId.vertcles

import io.vertx.core.Vertx
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.shareddata.Lock
import io.vertx.kotlin.core.eventbus.requestAwait
import io.vertx.kotlin.core.shareddata.getAsyncMapAwait
import io.vertx.kotlin.core.shareddata.getAwait
import io.vertx.kotlin.core.shareddata.getLockAwait
import io.vertx.kotlin.core.shareddata.putAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import sz.scaffold.Application
import sz.scaffold.sequenceId.IdGenerator
import sz.scaffold.sequenceId.exceptions.FailedToGetWorkerId
import sz.scaffold.sequenceId.exceptions.ReachMaxCountOfIdGenerators
import sz.scaffold.tools.SzException
import sz.scaffold.tools.json.toJsonPretty
import sz.scaffold.tools.json.toMutableMap
import sz.scaffold.tools.logger.Logger
import java.util.*

//
// Created by kk on 2019-06-24.
//
class SeqIdServiceVerticle : CoroutineVerticle() {

    private var idGenerator: IdGenerator? = null
    private var workerId: Long = -1
    private var msgConsumer: MessageConsumer<Any>? = null

    override suspend fun start() {
        workerId = registWorker(vertx, workerName)
        if (workerId >= 0) {
            idGenerator = IdGenerator(workerId)
            val evBus = vertx.eventBus()
            msgConsumer = evBus.consumer(idServiceBusAddress) { message ->
                try {
                    message.reply(idGenerator!!.nextId())
                } catch (ex: Throwable) {
                    message.fail(-1, ex.message)
                }
            }
        }

        Logger.info("Start SeqIdServiceVerticle, workerId: $workerId")
    }

    override suspend fun stop() {
        if (workerId >= 0) {
            unRegistWorker(vertx, workerId)
            workerId = -1
        }
    }

    private val workerName: String by lazy {
        val uid = UUID.randomUUID().toString()
        return@lazy if (vertx.isClustered) {
            if (vertx != Application.vertx) {
                throw SzException("SeqIdServiceVerticle is not deployed by Application.vertx")
            }
            "$uid@${Application.vertxOptions.clusterManager.nodeId}"
        } else {
            "$uid@local"
        }
    }

    companion object {
        val idServiceBusAddress = "sz.SeqIdService"
        val idMapLockName = "sz.SeqIdService.lock"
        val idMapName = "sz.SeqIdService.idMap"
        val mapkey = "idMap"

        /**
         * 注册一个 id 生成器的worker, 成功则分配一个 workerId, 失败则返回 -1
         */
        suspend fun registWorker(vertx: Vertx, workerName: String): Long {
            val shareData = vertx.sharedData()
            var lock: Lock? = null

            try {
                lock = shareData.getLockAwait(idMapLockName)
                val shareMap = shareData.getAsyncMapAwait<String, String>(idMapName)
                val mapJson = shareMap.getAwait(mapkey)
                val idMap: MutableMap<Long, String> = if (mapJson.isNullOrBlank()) {
                    mutableMapOf()
                } else {
                    mapJson.toMutableMap()
                }

                if (vertx.isClustered) {
                    removeOffLineWorkerIdWhenClustered(idMap)
                }

                for (id in 0..IdGenerator.maxWorkerId) {
                    if (idMap.containsKey(id).not()) {
                        idMap[id] = workerName
                        shareMap.putAwait(mapkey, idMap.toJsonPretty())
                        return id
                    }
                }
                throw ReachMaxCountOfIdGenerators()
            } catch (ex: Throwable) {
                throw when (ex) {
                    is ReachMaxCountOfIdGenerators -> ex
                    else -> FailedToGetWorkerId(ex)
                }
            } finally {
                lock?.release()
            }
        }

        suspend fun unRegistWorker(vertx: Vertx, workerId: Long) {
            val shareData = vertx.sharedData()
            var lock: Lock? = null

            try {
                lock = shareData.getLockAwait(idMapLockName)

                val shareMap = shareData.getAsyncMapAwait<String, String>(idMapName)
                val mapJson = shareMap.getAwait(mapkey)
                val idMap: MutableMap<Long, String> = if (mapJson.isNullOrBlank()) {
                    mutableMapOf()
                } else {
                    mapJson.toMutableMap()
                }

                if (idMap.containsKey(workerId)) {
                    idMap.remove(workerId)
                }

                shareMap.putAwait(mapkey, idMap.toJsonPretty())
            } finally {
                lock?.release()
            }
        }

        private fun removeOffLineWorkerIdWhenClustered(idMap: MutableMap<Long, String>) {
            val nodes = Application.vertxOptions.clusterManager.nodes.toSet()
            val offlineIdList = idMap.filter { nodes.contains(it.value.nodeId()).not() }.keys

            offlineIdList.forEach {
                idMap.remove(it)
            }
        }

        private fun String.nodeId(): String {
            return this.split("@").last()
        }

        suspend fun nextIdAwait(): Long {
            return Application.vertx.eventBus().requestAwait<Long>(idServiceBusAddress, "").body()
        }
    }

}