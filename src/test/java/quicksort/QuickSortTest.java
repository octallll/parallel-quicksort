package quicksort;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class QuickSortTest {
    private static final int[] PARALLELISMS = {1, 2, 4, 8};
    private static final int LARGE_SIZE = 1000008;

    private final Random random = new Random(572);

    @Test
    @DisplayName("Empty and single element arrays are sorted correct")
    void testEmptyAndSingle() {
        assertSortsCorrect(new int[]{}, "empty");
        assertSortsCorrect(new int[]{7}, "single");
    }

    @Test
    @DisplayName("All small sizes are sorted correct")
    void testSmallSizes() {
        for (int size = 2; size <= 100; size++) {
            for (int i = 0; i < 5; i++) {
                assertSortsCorrect(randomArray(size, size / 2 + 1), "small random");
            }
        }
    }

    @Test
    @DisplayName("Random arrays around sequential threshold and large ones are sorted correct")
    void testRandom() {
        int threshold = ParallelQuickSort.SEQUENTIAL_THRESHOLD;

        for (int size : new int[]{threshold - 1, threshold, threshold + 1, 2000000}) {
            assertSortsCorrect(randomArray(size, 0), "random");
        }
    }

    @Test
    @DisplayName("Structured arrays are sorted correct")
    void testStructured() {
        int n = LARGE_SIZE;

        assertSortsCorrect(generate(n, i -> i), "sorted");
        assertSortsCorrect(generate(n, i -> n - i), "reversed");
        assertSortsCorrect(generate(n, i -> 5), "all equal");
        assertSortsCorrect(generate(n, i -> i < n / 2 ? i : n - i), "organ pipe");
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 10})
    @DisplayName("Arrays with many duplicates are sorted correct")
    void testManyDuplicates(int distinct) {
        assertSortsCorrect(randomArray(LARGE_SIZE, distinct), distinct + " distinct values");
    }

    @Test
    @DisplayName("Extreme values are sorted correct")
    void testExtremeValues() {
        int[] values = {Integer.MIN_VALUE, Integer.MIN_VALUE + 1, -1, 0, 1, Integer.MAX_VALUE - 1, Integer.MAX_VALUE};

        assertSortsCorrect(generate(LARGE_SIZE, i -> values[random.nextInt(values.length)]), "extreme values");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Partition splits array correct")
    void testPartition(boolean strict) {
        for (int test = 0; test < 1000; test++) {
            int n = 1 + random.nextInt(200);
            int[] array = randomArray(n, 1 + random.nextInt(n));
            int[] before = array.clone();
            int pivot = array[random.nextInt(n)];

            int less = Partition.sequential(array, new int[n], 0, n - 1, pivot, strict);

            for (int i = 0; i < n; i++) {
                boolean goesLeft = strict ? array[i] < pivot : array[i] <= pivot;
                assertEquals(i < less, goesLeft, "element " + array[i] + " at " + i + " is on wrong side, less=" + less);
            }

            Arrays.sort(before);
            Arrays.sort(array);
            assertArrayEquals(before, array, "partition lost or duplicated elements");
        }
    }

    @Test
    @DisplayName("Non positive parallelism is rejected")
    void testInvalidParallelism() {
        assertThrows(IllegalArgumentException.class, () -> ParallelQuickSort.sort(new int[]{2, 1}, 0));
    }

    private void assertSortsCorrect(int[] input, String label) {
        int[] expected = input.clone();
        Arrays.sort(expected);

        int[] seq = input.clone();
        SequentialQuickSort.sort(seq);
        assertArrayEquals(expected, seq, "seq failed on " + label + " (n=" + input.length + ")");

        for (int parallelism : PARALLELISMS) {
            int[] par = input.clone();
            ParallelQuickSort.sort(par, parallelism);
            assertArrayEquals(
                    expected, par, "par(" + parallelism + ") failed on " + label + " (n=" + input.length + ")");
        }
    }

    private int[] randomArray(int size, int bound) {
        return generate(size, i -> bound > 0 ? random.nextInt(bound) : random.nextInt());
    }

    private static int[] generate(int size, Function<Integer, Integer> generator) {
        int[] array = new int[size];

        for (int i = 0; i < size; i++) {
            array[i] = generator.apply(i);
        }

        return array;
    }
}
