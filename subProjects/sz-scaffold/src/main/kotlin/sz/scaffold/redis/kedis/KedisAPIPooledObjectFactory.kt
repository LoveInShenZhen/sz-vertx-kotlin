package sz.scaffold.redis.kedis

import io.vertx.core.Vertx
import io.vertx.kotlin.redis.client.connectAwait
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisAPI
import io.vertx.redis.client.RedisOptions
import sz.objectPool.ObjectPool
import sz.objectPool.PooledObject
import sz.objectPool.PooledObjectFactory
import sz.scaffold.tools.logger.Logger

//
// Created by kk on 2019/11/12.
//
class KedisAPIPooledObjectFactory(private val vertx: Vertx,
                                  private val redisOptions: RedisOptions,
                                  private val operationTimeout: Long = -1) : PooledObjectFactory<KedisAPI>() {

    override suspend fun createObjectAwait(): KedisAPI {
        val client = Redis.createClient(vertx, redisOptions).connectAwait()
        val kedisApi = KedisAPI(RedisAPI.api(client), client, operationTimeout)
        client.exceptionHandler {
            kedisApi.markBroken()
            Logger.debug("[exceptionHandler - markBroken] - Redis client occur exception: $it")
        }.endHandler {
            kedisApi.markBroken()
//            Logger.debug("[endHandler - markBroken and close]")
        }
//        Logger.debug("[KedisAPIPooledObjectFactory] create a redis client")
        return kedisApi
    }

    override suspend fun wrapObject(pool: ObjectPool<KedisAPI>): PooledObject<KedisAPI>? {
        val box = super.wrapObject(pool)
        box?.target?.connectWithBox(box)

        return box
    }

    override fun destoryObject(target: KedisAPI) {
        target.destory()
    }
}