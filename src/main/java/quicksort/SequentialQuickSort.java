package quicksort;

public class SequentialQuickSort {
    private static final int BUBBLE_SORT_THRESHOLD = 24;

    public static void sort(int[] array) {
        if (array.length > 1) {
            sort(array, new int[array.length], 0, array.length - 1);
        }
    }

    static void sort(int[] array, int[] buffer, int left, int right) {
        if (right - left < BUBBLE_SORT_THRESHOLD) {
            bubbleSort(array, left, right);
            return;
        }

        int pivot = pivot(array, left, right);

        int less = Partition.sequential(array, buffer, left, right, pivot, true);

        if (less > 0) {
            sort(array, buffer, left, left + less - 1);
            sort(array, buffer, left + less, right);
            return;
        }

        // если pivot оказался минимальным на отрезке, то сортируем всю часть строго большую его
        int equal = Partition.sequential(array, buffer, left, right, pivot, false);

        sort(array, buffer, left + equal, right);
    }

    /**
        Берет медиану из трех -- середины, четверти и 3/4 на отрезке
    **/
    static int pivot(int[] array, int left, int right) {
        int quarter = (right - left) / 4;
        int mid = left + (right - left) / 2;

        return median(array[left + quarter], array[mid], array[right - quarter]);
    }

    private static int median(int a, int b, int c) {
        if (a < b) {
            return b < c ? b : (a < c ? c : a);
        }

        return a < c ? a : (b < c ? c : b);
    }

    private static void bubbleSort(int[] array, int left, int right) {
        for (int end = right; end > left; end--) {
            boolean swapped = false;

            for (int i = left; i < end; i++) {
                if (array[i] > array[i + 1]) {
                    swap(array, i, i + 1);
                    swapped = true;
                }
            }

            if (!swapped) {
                return;
            }
        }
    }

    private static void swap(int[] array, int i, int j) {
        int tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }
}
