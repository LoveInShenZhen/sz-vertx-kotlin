package sz.scaffold.redis.kedis.pool

import org.apache.commons.pool2.impl.GenericObjectPool
import sz.scaffold.redis.kedis.KedisPoolConfig

//
// Created by kk on 2019-06-11.
//
class KedisAPIPool(factory: KedisAPIPooledObjectFactory, poolConfig: KedisPoolConfig) : GenericObjectPool<KedisAPI>(factory, poolConfig)