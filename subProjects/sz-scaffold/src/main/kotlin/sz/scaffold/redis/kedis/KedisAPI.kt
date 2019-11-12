package sz.scaffold.redis.kedis

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisAPI
import io.vertx.redis.client.Response
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import sz.objectPool.PooledObject

//
// Created by kk on 2019-06-10.
//
class KedisAPI(private val delegate: RedisAPI, private val redisClient: Redis, private val operationTimeout: Long) : AutoCloseable {

    private var poolBox: PooledObject<KedisAPI>? = null

    fun markBroken() {
        poolBox?.markBroken()
    }

    fun connectWithBox(box: PooledObject<KedisAPI>): KedisAPI {
        poolBox = box
        return this
    }

    override fun close() {
        poolBox?.close()
    }

    internal fun destory() {
        poolBox = null
        redisClient.close()
    }

    private suspend fun <T> awaitWithTimeout(timeOut: Long, block: (h: Handler<AsyncResult<T>>) -> Unit): T {
        try {
            return if (timeOut > 0L) {
                withTimeout(timeOut) { awaitResult(block) }
            } else {
                awaitResult(block)
            }
        } catch (ex: Throwable) {
//            if ((ex is NoSuchElementException || ex is TimeoutCancellationException).not()) {
////                Logger.debug("redis client markBroken by: ${ex.message}")
//                markBroken()
//            }
            if ((ex is TimeoutCancellationException).not()) {
//                Logger.debug("redis client markBroken by: ${ex.message}")
                markBroken()
            }
            throw ex
        }
    }


    suspend fun appendAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.append(arg0, arg1, it)
        }
    }

    suspend fun askingAwait(): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.asking(it)
        }
    }

    suspend fun authAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.auth(arg0, it)
        }
    }

    suspend fun bgrewriteaofAwait(): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.bgrewriteaof(it)
        }
    }

    suspend fun bgsaveAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.bgsave(args, it)
        }
    }

    suspend fun bitcountAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.bitcount(args, it)
        }
    }

    suspend fun bitfieldAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.bitfield(args, it)
        }
    }

    suspend fun bitopAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.bitop(args, it)
        }
    }

    suspend fun bitposAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.bitpos(args, it)
        }
    }

    suspend fun blpopAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.blpop(args, it)
        }
    }

    suspend fun brpopAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.brpop(args, it)
        }
    }

    suspend fun brpoplpushAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.brpoplpush(arg0, arg1, arg2, it)
        }
    }

    suspend fun bzpopmaxAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.bzpopmax(args, it)
        }
    }

    suspend fun bzpopminAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.bzpopmin(args, it)
        }
    }

    suspend fun clientAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.client(args, it)
        }
    }

    suspend fun clusterAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.cluster(args, it)
        }
    }

    suspend fun commandAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.command(args, it)
        }
    }

    suspend fun configAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.config(args, it)
        }
    }

    suspend fun dbsizeAwait(): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.dbsize(it)
        }
    }

    suspend fun debugAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.debug(args, it)
        }
    }

    suspend fun decrAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.decr(arg0, it)
        }
    }

    suspend fun decrbyAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.decrby(arg0, arg1, it)
        }
    }

    suspend fun delAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.del(args, it)
        }
    }

    suspend fun discardAwait(): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.discard(it)
        }
    }

    suspend fun dumpAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.dump(arg0, it)
        }
    }

    suspend fun echoAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.echo(arg0, it)
        }
    }

    suspend fun evalAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.eval(args, it)
        }
    }

    suspend fun evalshaAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.evalsha(args, it)
        }
    }

    suspend fun execAwait(): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.exec(it)
        }
    }

    suspend fun existsAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.exists(args, it)
        }
    }

    suspend fun expireAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.expire(arg0, arg1, it)
        }
    }

    suspend fun expireatAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.expireat(arg0, arg1, it)
        }
    }

    suspend fun flushallAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.flushall(args, it)
        }
    }

    suspend fun flushdbAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.flushdb(args, it)
        }
    }

    suspend fun geoaddAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.geoadd(args, it)
        }
    }

    suspend fun geodistAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.geodist(args, it)
        }
    }

    suspend fun geohashAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.geohash(args, it)
        }
    }

    suspend fun geoposAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.geopos(args, it)
        }
    }

    suspend fun georadiusAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.georadius(args, it)
        }
    }

    suspend fun georadiusRoAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.georadiusRo(args, it)
        }
    }

    suspend fun georadiusbymemberAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.georadiusbymember(args, it)
        }
    }

    suspend fun georadiusbymemberRoAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.georadiusbymemberRo(args, it)
        }
    }

    suspend fun getAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.get(arg0, it)
        }
    }

    suspend fun getbitAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.getbit(arg0, arg1, it)
        }
    }

    suspend fun getrangeAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.getrange(arg0, arg1, arg2, it)
        }
    }

    suspend fun getsetAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.getset(arg0, arg1, it)
        }
    }

    suspend fun hdelAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.hdel(args, it)
        }
    }

    suspend fun hexistsAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.hexists(arg0, arg1, it)
        }
    }

    suspend fun hgetAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.hget(arg0, arg1, it)
        }
    }

    suspend fun hgetallAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.hgetall(arg0, it)
        }
    }

    suspend fun hincrbyAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.hincrby(arg0, arg1, arg2, it)
        }
    }

    suspend fun hincrbyfloatAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.hincrbyfloat(arg0, arg1, arg2, it)
        }
    }

    suspend fun hkeysAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.hkeys(arg0, it)
        }
    }

    suspend fun hlenAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.hlen(arg0, it)
        }
    }

    suspend fun hmgetAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.hmget(args, it)
        }
    }

    suspend fun hmsetAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.hmset(args, it)
        }
    }

    suspend fun hostAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.host(args, it)
        }
    }

    suspend fun hscanAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.hscan(args, it)
        }
    }

    suspend fun hsetAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.hset(args, it)
        }
    }

    suspend fun hsetnxAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.hsetnx(arg0, arg1, arg2, it)
        }
    }

    suspend fun hstrlenAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.hstrlen(arg0, arg1, it)
        }
    }

    suspend fun hvalsAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.hvals(arg0, it)
        }
    }

    suspend fun incrAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.incr(arg0, it)
        }
    }

    suspend fun incrbyAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.incrby(arg0, arg1, it)
        }
    }

    suspend fun incrbyfloatAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.incrbyfloat(arg0, arg1, it)
        }
    }

    suspend fun infoAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.info(args, it)
        }
    }

    suspend fun keysAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.keys(arg0, it)
        }
    }

    suspend fun lastsaveAwait(): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.lastsave(it)
        }
    }

    suspend fun latencyAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.latency(args, it)
        }
    }

    suspend fun lindexAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.lindex(arg0, arg1, it)
        }
    }

    suspend fun linsertAwait(arg0: String, arg1: String, arg2: String, arg3: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.linsert(arg0, arg1, arg2, arg3, it)
        }
    }

    suspend fun llenAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.llen(arg0, it)
        }
    }

    suspend fun lolwutAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.lolwut(args, it)
        }
    }

    suspend fun lpopAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.lpop(arg0, it)
        }
    }

    suspend fun lpushAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.lpush(args, it)
        }
    }

    suspend fun lpushxAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.lpushx(args, it)
        }
    }

    suspend fun lrangeAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.lrange(arg0, arg1, arg2, it)
        }
    }

    suspend fun lremAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.lrem(arg0, arg1, arg2, it)
        }
    }

    suspend fun lsetAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.lset(arg0, arg1, arg2, it)
        }
    }

    suspend fun ltrimAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.ltrim(arg0, arg1, arg2, it)
        }
    }

    suspend fun memoryAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.memory(args, it)
        }
    }

    suspend fun mgetAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.mget(args, it)
        }
    }

    suspend fun migrateAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.migrate(args, it)
        }
    }

    suspend fun moduleAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.module(args, it)
        }
    }

    suspend fun monitorAwait(): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.monitor(it)
        }
    }

    suspend fun moveAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.move(arg0, arg1, it)
        }
    }

    suspend fun msetAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.mset(args, it)
        }
    }

    suspend fun msetnxAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.msetnx(args, it)
        }
    }

    suspend fun multiAwait(): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.multi(it)
        }
    }

    suspend fun objectAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.`object`(args, it)
        }
    }

    suspend fun persistAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.persist(arg0, it)
        }
    }

    suspend fun pexpireAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.pexpire(arg0, arg1, it)
        }
    }

    suspend fun pexpireatAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.pexpireat(arg0, arg1, it)
        }
    }

    suspend fun pfaddAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.pfadd(args, it)
        }
    }

    suspend fun pfcountAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.pfcount(args, it)
        }
    }

    suspend fun pfdebugAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.pfdebug(args, it)
        }
    }

    suspend fun pfmergeAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.pfmerge(args, it)
        }
    }

    suspend fun pfselftestAwait(): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.pfselftest(it)
        }
    }

    suspend fun pingAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.ping(args, it)
        }
    }

    suspend fun postAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.post(args, it)
        }
    }

    suspend fun psetexAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.psetex(arg0, arg1, arg2, it)
        }
    }

    suspend fun psubscribeAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.psubscribe(args, it)
        }
    }

    suspend fun psyncAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.psync(arg0, arg1, it)
        }
    }

    suspend fun pttlAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.pttl(arg0, it)
        }
    }

    suspend fun publishAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.publish(arg0, arg1, it)
        }
    }

    suspend fun pubsubAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.pubsub(args, it)
        }
    }

    suspend fun punsubscribeAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.punsubscribe(args, it)
        }
    }

    suspend fun randomkeyAwait(): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.randomkey(it)
        }
    }

    suspend fun readonlyAwait(): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.readonly(it)
        }
    }

    suspend fun readwriteAwait(): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.readwrite(it)
        }
    }

    suspend fun renameAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.rename(arg0, arg1, it)
        }
    }

    suspend fun renamenxAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.renamenx(arg0, arg1, it)
        }
    }

    suspend fun replconfAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.replconf(args, it)
        }
    }

    suspend fun replicaofAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.replicaof(arg0, arg1, it)
        }
    }

    suspend fun restoreAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.restore(args, it)
        }
    }

    suspend fun restoreAskingAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.restoreAsking(args, it)
        }
    }

    suspend fun roleAwait(): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.role(it)
        }
    }

    suspend fun rpopAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.rpop(arg0, it)
        }
    }

    suspend fun rpoplpushAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.rpoplpush(arg0, arg1, it)
        }
    }

    suspend fun rpushAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.rpush(args, it)
        }
    }

    suspend fun rpushxAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.rpushx(args, it)
        }
    }

    suspend fun saddAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.sadd(args, it)
        }
    }

    suspend fun saveAwait(): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.save(it)
        }
    }

    suspend fun scanAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.scan(args, it)
        }
    }

    suspend fun scardAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.scard(arg0, it)
        }
    }

    suspend fun scriptAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.script(args, it)
        }
    }

    suspend fun sdiffAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.sdiff(args, it)
        }
    }

    suspend fun sdiffstoreAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.sdiffstore(args, it)
        }
    }

    suspend fun selectAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.select(arg0, it)
        }
    }

    suspend fun setAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.set(args, it)
        }
    }

    suspend fun setbitAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.setbit(arg0, arg1, arg2, it)
        }
    }

    suspend fun setexAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.setex(arg0, arg1, arg2, it)
        }
    }

    suspend fun setnxAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.setnx(arg0, arg1, it)
        }
    }

    suspend fun setrangeAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.setrange(arg0, arg1, arg2, it)
        }
    }

    suspend fun shutdownAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.shutdown(args, it)
        }
    }

    suspend fun sinterAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.sinter(args, it)
        }
    }

    suspend fun sinterstoreAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.sinterstore(args, it)
        }
    }

    suspend fun sismemberAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.sismember(arg0, arg1, it)
        }
    }

    suspend fun slaveofAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.slaveof(arg0, arg1, it)
        }
    }

    suspend fun slowlogAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.slowlog(args, it)
        }
    }

    suspend fun smembersAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.smembers(arg0, it)
        }
    }

    suspend fun smoveAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.smove(arg0, arg1, arg2, it)
        }
    }

    suspend fun sortAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.sort(args, it)
        }
    }

    suspend fun spopAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.spop(args, it)
        }
    }

    suspend fun srandmemberAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.srandmember(args, it)
        }
    }

    suspend fun sremAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.srem(args, it)
        }
    }

    suspend fun sscanAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.sscan(args, it)
        }
    }

    suspend fun strlenAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.strlen(arg0, it)
        }
    }

    suspend fun subscribeAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.subscribe(args, it)
        }
    }

    suspend fun substrAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.substr(arg0, arg1, arg2, it)
        }
    }

    suspend fun sunionAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.sunion(args, it)
        }
    }

    suspend fun sunionstoreAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.sunionstore(args, it)
        }
    }

    suspend fun swapdbAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.swapdb(arg0, arg1, it)
        }
    }

    suspend fun syncAwait(): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.sync(it)
        }
    }

    suspend fun timeAwait(): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.time(it)
        }
    }

    suspend fun touchAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.touch(args, it)
        }
    }

    suspend fun ttlAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.ttl(arg0, it)
        }
    }

    suspend fun typeAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.type(arg0, it)
        }
    }

    suspend fun unlinkAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.unlink(args, it)
        }
    }

    suspend fun unsubscribeAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.unsubscribe(args, it)
        }
    }

    suspend fun unwatchAwait(): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.unwatch(it)
        }
    }

    suspend fun waitAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.wait(arg0, arg1, it)
        }
    }

    suspend fun watchAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.watch(args, it)
        }
    }

    suspend fun xackAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.xack(args, it)
        }
    }

    suspend fun xaddAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.xadd(args, it)
        }
    }

    suspend fun xclaimAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.xclaim(args, it)
        }
    }

    suspend fun xdelAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.xdel(args, it)
        }
    }

    suspend fun xgroupAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.xgroup(args, it)
        }
    }

    suspend fun xinfoAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.xinfo(args, it)
        }
    }

    suspend fun xlenAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.xlen(arg0, it)
        }
    }

    suspend fun xpendingAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.xpending(args, it)
        }
    }

    suspend fun xrangeAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.xrange(args, it)
        }
    }

    suspend fun xreadAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.xread(args, it)
        }
    }

    suspend fun xreadgroupAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.xreadgroup(args, it)
        }
    }

    suspend fun xrevrangeAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.xrevrange(args, it)
        }
    }

    suspend fun xsetidAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.xsetid(arg0, arg1, it)
        }
    }

    suspend fun xtrimAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.xtrim(args, it)
        }
    }

    suspend fun zaddAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zadd(args, it)
        }
    }

    suspend fun zcardAwait(arg0: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zcard(arg0, it)
        }
    }

    suspend fun zcountAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zcount(arg0, arg1, arg2, it)
        }
    }

    suspend fun zincrbyAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zincrby(arg0, arg1, arg2, it)
        }
    }

    suspend fun zinterstoreAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zinterstore(args, it)
        }
    }

    suspend fun zlexcountAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zlexcount(arg0, arg1, arg2, it)
        }
    }

    suspend fun zpopmaxAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zpopmax(args, it)
        }
    }

    suspend fun zpopminAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zpopmin(args, it)
        }
    }

    suspend fun zrangeAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zrange(args, it)
        }
    }

    suspend fun zrangebylexAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zrangebylex(args, it)
        }
    }

    suspend fun zrangebyscoreAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zrangebyscore(args, it)
        }
    }

    suspend fun zrankAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zrank(arg0, arg1, it)
        }
    }

    suspend fun zremAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zrem(args, it)
        }
    }

    suspend fun zremrangebylexAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zremrangebylex(arg0, arg1, arg2, it)
        }
    }

    suspend fun zremrangebyrankAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zremrangebyrank(arg0, arg1, arg2, it)
        }
    }

    suspend fun zremrangebyscoreAwait(arg0: String, arg1: String, arg2: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zremrangebyscore(arg0, arg1, arg2, it)
        }
    }

    suspend fun zrevrangeAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zrevrange(args, it)
        }
    }

    suspend fun zrevrangebylexAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zrevrangebylex(args, it)
        }
    }

    suspend fun zrevrangebyscoreAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zrevrangebyscore(args, it)
        }
    }

    suspend fun zrevrankAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zrevrank(arg0, arg1, it)
        }
    }

    suspend fun zscanAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zscan(args, it)
        }
    }

    suspend fun zscoreAwait(arg0: String, arg1: String): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zscore(arg0, arg1, it)
        }
    }

    suspend fun zunionstoreAwait(args: List<String>): Response? {
        return awaitWithTimeout(operationTimeout) {
            delegate.zunionstore(args, it)
        }
    }

}