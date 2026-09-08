package ua.lviv.bas.cinema.bonus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
class BonusTransactionsCacheResolver implements CacheResolver {

    static final String CACHE_NAME_PREFIX = "bonusTransactions:";

    private final CacheManager cacheManager;

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Long userId = (Long) context.getArgs()[0];
        Cache cache = cacheManager.getCache(CACHE_NAME_PREFIX + userId);
        return cache == null ? List.of() : List.of(cache);
    }
}
