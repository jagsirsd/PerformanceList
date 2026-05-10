package portfolio;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A segmented data structure designed for high-concurrency additions.
 * It uses multiple internal segments, each with its own lock, to reduce contention.
 * Best-effort routing is used to find an available segment quickly.
 */
public class SegmentedList<T> implements Iterable<T> {
    private final Segment<T>[] segments;
    private final int mask;

    @SuppressWarnings("unchecked")
    public SegmentedList(int concurrencyLevel) {
        // Ensure power of two for mask optimization
        int capacity = 1;
        while (capacity < concurrencyLevel) capacity <<= 1;
        
        this.segments = new Segment[capacity];
        for (int i = 0; i < capacity; i++) {
            segments[i] = new Segment<>();
        }
        this.mask = capacity - 1;
    }

    public SegmentedList() {
        this(Runtime.getRuntime().availableProcessors());
        //System.out.println("%d".formatted(Runtime.getRuntime().availableProcessors()));
    }

    private static class Segment<T> {
        final List<T> list = new ArrayList<>();
        final ReentrantLock lock = new ReentrantLock();
    }

    /**
     * Adds an item using best-effort routing to minimize lock contention.
     */
    public void add(T item) {
        int startIdx = (int) (Thread.currentThread().threadId() & mask);
        
        // Try to find an immediately available segment
        for (int i = 0; i < segments.length; i++) {
            int idx = (startIdx + i) & mask;
            if (segments[idx].lock.tryLock()) {
                try {
                    segments[idx].list.add(item);
                    return;
                } finally {
                    segments[idx].lock.unlock();
                }
            }
        }

        // Fallback: block on the preferred segment
        segments[startIdx].lock.lock();
        try {
            segments[startIdx].list.add(item);
        } finally {
            segments[startIdx].lock.unlock();
        }
    }

    public boolean remove(Object o) {
        // Removal is more expensive as we might need to check multiple segments
        for (Segment<T> segment : segments) {
            segment.lock.lock();
            try {
                if (segment.list.remove(o)) {
                    return true;
                }
            } finally {
                segment.lock.unlock();
            }
        }
        return false;
    }

    public boolean contains(Object o) {
        for (Segment<T> segment : segments) {
            segment.lock.lock();
            try {
                if (segment.list.contains(o)) {
                    return true;
                }
            } finally {
                segment.lock.unlock();
            }
        }
        return false;
    }

    public int size() {
        int total = 0;
        for (Segment<T> segment : segments) {
            segment.lock.lock();
            try {
                total += segment.list.size();
            } finally {
                segment.lock.unlock();
            }
        }
        return total;
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return stream().spliterator();
    }

    /**
     * Returns a sequential stream of all items in the segments.
     */
    public Stream<T> stream() {
        return Arrays.stream(segments)
                .flatMap(s -> {
                    s.lock.lock();
                    try {
                        return new ArrayList<>(s.list).stream();
                    } finally {
                        s.lock.unlock();
                    }
                });
    }

    /**
     * Returns a parallel stream of all items in the segments, 
     * processing segments in parallel.
     */
    public Stream<T> parallelStream() {
        return Arrays.stream(segments).parallel()
                .flatMap(s -> {
                    s.lock.lock();
                    try {
                        // Snapshot each segment to allow parallel processing without 
                        // holding locks for the entire duration of the stream operation.
                        return new ArrayList<>(s.list).stream();
                    } finally {
                        s.lock.unlock();
                    }
                });
    }
}
