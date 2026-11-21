package quicksort;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;

public class Application {
    private static final int DEFAULT_SIZE = 100000000;
    private static final int DEFAULT_RUNS = 5;
    private static final int DEFAULT_THREADS = 4;

    public static void main(String[] args) {
        int size = argOrDefault(args, 0, DEFAULT_SIZE);
        int runs = argOrDefault(args, 1, DEFAULT_RUNS);
        int threads = argOrDefault(args, 2, DEFAULT_THREADS);

        System.out.printf(
                "size=%d runs=%d threads=%d cpus=%d%n",
                size, runs, threads, Runtime.getRuntime().availableProcessors());

        Random random = new Random(572);
        int[] source = new int[size];

        for (int i = 0; i < size; i++) {
            source[i] = random.nextInt();
        }

        warmup(source, threads);

        int[] seqArray = new int[size];
        int[] parArray = new int[size];

        double seqTotal = 0;
        double parTotal = 0;

        for (int run = 1; run <= runs; run++) {
            double seqTime = measure("seq", source, seqArray, SequentialQuickSort::sort);
            double parTime = measure("par", source, parArray, a -> ParallelQuickSort.sort(a, threads));

            if (!Arrays.equals(seqArray, parArray)) {
                throw new IllegalStateException("seq and par results differ");
            }

            System.out.printf("  run %d: seq %.0f ms, par %.0f ms%n", run, seqTime, parTime);

            seqTotal += seqTime;
            parTotal += parTime;
        }

        System.out.printf("seq average: %.0f ms%n", seqTotal / runs);
        System.out.printf("par average: %.0f ms%n", parTotal / runs);
        System.out.printf("speedup seq/par = %.2f%n", seqTotal / parTotal);
    }

    private static int argOrDefault(String[] args, int index, int defaultValue) {
        return args.length > index ? Integer.parseInt(args[index]) : defaultValue;
    }

    private static void warmup(int[] source, int threads) {
        SequentialQuickSort.sort(source.clone());
        ParallelQuickSort.sort(source.clone(), threads);
    }

    private static double measure(String name, int[] source, int[] array, Consumer<int[]> sort) {
        System.arraycopy(source, 0, array, 0, source.length);

        long start = System.nanoTime();
        sort.accept(array);
        double millis = (System.nanoTime() - start) / 1e6;

        if (!isSorted(array)) {
            throw new IllegalStateException(name + " produced unsorted array");
        }

        return millis;
    }

    private static boolean isSorted(int[] array) {
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] > array[i]) {
                return false;
            }
        }

        return true;
    }
}
