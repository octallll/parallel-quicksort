package quicksort;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.IntConsumer;

public class ParallelQuickSort {
    static final int SEQUENTIAL_THRESHOLD = 10000;

    public static void sort(int[] array, int parallelism) {
        if (parallelism < 1) {
            throw new IllegalArgumentException("Parallelism must be positive: " + parallelism);
        }

        if (array.length <= 1) {
            return;
        }

        int[] buffer = new int[array.length];

        try (ForkJoinPool pool = new ForkJoinPool(parallelism)) {
            pool.invoke(new SortTask(array, buffer, 0, array.length - 1));
        }
    }

    private static class SortTask extends RecursiveAction {
        private final int[] array;
        private final int[] buffer;
        private final int left;
        private final int right;

        SortTask(int[] array, int[] buffer, int left, int right) {
            this.array = array;
            this.buffer = buffer;
            this.left = left;
            this.right = right;
        }

        private SortTask subtask(int from, int to) {
            return new SortTask(array, buffer, from, to);
        }

        @Override
        protected void compute() {
            if (right - left + 1 <= SEQUENTIAL_THRESHOLD) {
                SequentialQuickSort.sort(array, buffer, left, right);
                return;
            }

            int pivot = SequentialQuickSort.pivot(array, left, right);

            int less = partition(array, buffer, left, right, pivot, true);

            if (less > 0) {
                invokeAll(subtask(left, left + less - 1), subtask(left + less, right));
                return;
            }

            // если pivot оказался минимальным на текущем отрезке, то сортируем всю
            // часть строго большую его
            int equal = partition(array, buffer, left, right, pivot, false);

            subtask(left + equal, right).compute();
        }
    }

    private static int partition(int[] array, int[] buffer, int left, int right, int pivot, boolean strict) {
        int blocks = Partition.blocks(left, right);
        int[] offsets = new int[blocks + 1];

        parallelFor(blocks, block -> {
            offsets[block + 1] = Partition.splitBlock(array, buffer, left, right, block, pivot, strict);
        });

        Partition.scan(offsets);

        parallelFor(blocks, block -> Partition.copyBlock(array, buffer, left, right, block, offsets));

        return offsets[blocks];
    }

    private static void parallelFor(int count, IntConsumer body) {
        new ParallelFor(0, count, body).invoke();
    }

    private static class ParallelFor extends RecursiveAction {
        private final int from;
        private final int to;
        private final IntConsumer body;

        ParallelFor(int from, int to, IntConsumer body) {
            this.from = from;
            this.to = to;
            this.body = body;
        }

        @Override
        protected void compute() {
            if (to - from == 1) {
                body.accept(from);
                return;
            }

            int mid = from + (to - from) / 2;

            invokeAll(new ParallelFor(from, mid, body), new ParallelFor(mid, to, body));
        }
    }
}
