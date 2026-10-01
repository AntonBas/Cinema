package ua.lviv.bas.cinema.config.ratelimit;

import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.generic.compare_and_swap.AbstractCompareAndSwapBasedProxyManager;
import io.github.bucket4j.distributed.proxy.generic.compare_and_swap.AsyncCompareAndSwapOperation;
import io.github.bucket4j.distributed.proxy.generic.compare_and_swap.CompareAndSwapOperation;
import io.github.bucket4j.distributed.remote.RemoteBucketState;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProxyManager extends AbstractCompareAndSwapBasedProxyManager<String> {

    private final ConcurrentHashMap<String, byte[]> storage = new ConcurrentHashMap<>();

    InMemoryProxyManager() {
        super(ClientSideConfig.getDefault());
    }

    public void clear() {
        storage.clear();
    }

    @Override
    public void removeProxy(String key) {
        storage.remove(key);
    }

    @Override
    public boolean isAsyncModeSupported() {
        return false;
    }

    @Override
    protected CompareAndSwapOperation beginCompareAndSwapOperation(String key) {
        return new CompareAndSwapOperation() {
            @Override
            public Optional<byte[]> getStateData(Optional<Long> timeoutNanos) {
                return Optional.ofNullable(storage.get(key));
            }

            @Override
            public synchronized boolean compareAndSwap(byte[] originalData, byte[] newData,
                                                       RemoteBucketState newState, Optional<Long> timeoutNanos) {
                if (!Arrays.equals(storage.get(key), originalData)) {
                    return false;
                }
                storage.put(key, newData);
                return true;
            }
        };
    }

    @Override
    protected AsyncCompareAndSwapOperation beginAsyncCompareAndSwapOperation(String key) {
        throw new UnsupportedOperationException("Async mode is not supported by InMemoryProxyManager");
    }

    @Override
    protected CompletableFuture<Void> removeAsync(String key) {
        storage.remove(key);
        return CompletableFuture.completedFuture(null);
    }
}
