package iorihuang.bankaccountmanager.helper.snowflakeid;

import java.util.concurrent.atomic.AtomicLong;

/**
 * loop from 0 to threshold
 */
public class RecyclableAtomicLong extends AtomicLong {
    long threshold;
    int maxTry;

    /**
     * @param threshold 0 ~ threshold (include)
     * @param maxTry    try without lock
     */
    public RecyclableAtomicLong(long threshold, int maxTry) {
        super(0);
        this.threshold = threshold;
        this.maxTry = maxTry;
    }

    public final long getAndIncrementWithRecycle() {
        for (int times = 0; ; times++) {
            long current = getAndIncrement();
            if (current <= threshold) {
                return current;
            }
            compareAndSet(current, 0);
            if (times < maxTry) {
                continue;
            }
            synchronized (this) {
                if (get() >= threshold) {
                    set(0);
                }
            }
        }
    }

}
