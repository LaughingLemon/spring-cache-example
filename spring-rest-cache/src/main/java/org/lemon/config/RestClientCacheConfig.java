package org.lemon.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisCommandTimeoutException;
import io.lettuce.core.SocketOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

@EnableCaching
@Configuration
@Slf4j
public class RestClientCacheConfig extends CachingConfigurerSupport implements CachingConfigurer {

    @Value("${redis.hostname:localhost}")
    private String redisHost;

    @Value("${redis.port:6379}")
    private int redisPort;

    @Value("${redis.timeout.secs:1}")
    private int redisTimeoutInSecs;

    @Value("${redis.socket.timeout.secs:1}")
    private int redisSocketTimeoutInSecs;

    @Value("${redis.ttl.hours:1}")
    private int redisDataTTL;


    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {

        final SocketOptions socketOptions = SocketOptions.builder().connectTimeout(Duration.ofSeconds(redisSocketTimeoutInSecs)).build();

        final ClientOptions clientOptions = ClientOptions.builder().socketOptions(socketOptions).build();

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(redisTimeoutInSecs)).clientOptions(clientOptions).build();
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(redisHost, redisPort);

        final LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(serverConfig, clientConfig);
        lettuceConnectionFactory.setValidateConnection(true);
        return lettuceConnectionFactory;

    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate() {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<Object, Object>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        return redisTemplate;
    }

    @Bean
    public RedisCacheManager redisCacheManager(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisSerializer<?> thing = new JdkSerializationRedisSerializer();
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig().disableCachingNullValues()
                .entryTtl(Duration.ofHours(redisDataTTL))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(thing));

        redisCacheConfiguration.usePrefix();

        RedisCacheManager redisCacheManager = RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(lettuceConnectionFactory)
                .cacheDefaults(redisCacheConfiguration).build();

        redisCacheManager.setTransactionAware(true);
        return redisCacheManager;
    }


    @Override
    public CacheErrorHandler errorHandler() {
        return new RedisCacheErrorHandler();
    }

    private class RedisCacheErrorHandler implements CacheErrorHandler {
        @Override
        public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
            handleTimeOutException(exception);
            log.info("Unable to get from cache " + cache.getName() + " : " + exception.getMessage());
        }

        @Override
        public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
            handleTimeOutException(exception);
            log.info("Unable to put into cache " + cache.getName() + " : " + exception.getMessage());
        }

        @Override
        public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
            handleTimeOutException(exception);
            log.info("Unable to evict from cache " + cache.getName() + " : " + exception.getMessage());
        }

        @Override
        public void handleCacheClearError(RuntimeException exception, Cache cache) {
            handleTimeOutException(exception);
            log.info("Unable to clean cache " + cache.getName() + " : " + exception.getMessage());
        }

        /**
         * We handle redis connection timeout exception , if the exception is handled then it is treated as a cache miss and
         * gets the data from actual storage
         *
         * @param exception
         */
        private void handleTimeOutException(RuntimeException exception) {
            if (exception instanceof RedisCommandTimeoutException)
                return;
        }
    }
}