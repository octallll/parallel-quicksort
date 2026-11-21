package quicksort;

class Partition {
    private static final int BLOCK_SIZE = 50000;

    static int sequential(int[] array, int[] buffer, int left, int right, int pivot, boolean strict) {
        int blocks = blocks(left, right);
        int[] offsets = new int[blocks + 1];

        for (int block = 0; block < blocks; block++) {
            offsets[block + 1] = splitBlock(array, buffer, left, right, block, pivot, strict);
        }

        scan(offsets);

        for (int block = 0; block < blocks; block++) {
            copyBlock(array, buffer, left, right, block, offsets);
        }

        return offsets[blocks];
    }

    static int blocks(int left, int right) {
        return (right - left) / BLOCK_SIZE + 1;
    }

    static int splitBlock(int[] array, int[] buffer, int left, int right, int block, int pivot, boolean strict) {
        return split(array, buffer, blockStart(left, block), blockEnd(left, right, block), pivot, strict);
    }

    static void scan(int[] offsets) {
        for (int block = 1; block < offsets.length; block++) {
            offsets[block] += offsets[block - 1];
        }
    }

    /**
        Копирует элементы блока под номером {@param block} в итоговый массив после уже скопированных блоков.
    **/
    static void copyBlock(int[] array, int[] buffer, int left, int right, int block, int[] offsets) {
        int start = blockStart(left, block);
        int end = blockEnd(left, right, block);
        int total = offsets[offsets.length - 1];
        int less = offsets[block + 1] - offsets[block];
        int lessTo = left + offsets[block];
        int greaterTo = left + total + (start - left) - offsets[block];

        for (int i = 0; i < less; i++) {
            array[lessTo + i] = buffer[start + i];
        }

        for (int i = start + less; i <= end; i++) {
            array[greaterTo + i - start - less] = buffer[i];
        }
    }

    /**
        Раскладывает часть массива от {@param left} до {@param right} в {@param buffer} соответствующий этому отрезку,
        но элементы левой части в {@param buffer} меньше либо равны (в зависимости от {@param strict}) {@param pivot}
    **/
    private static int split(int[] array, int[] buffer, int left, int right, int pivot, boolean strict) {
        long bound = strict ? pivot : pivot + 1L;
        int lessIndex = left;
        int greaterIndex = right;

        for (int i = left; i <= right; i++) {
            int value = array[i];
            int isLess = value < bound ? 1 : 0;

            // Написано не через if, чтобы процессор не пытался угадать результат ветвления
            // Неверно записанный элемент будет перетерт следующим верным
            buffer[lessIndex] = value;
            buffer[greaterIndex] = value;
            lessIndex += isLess;
            greaterIndex -= 1 - isLess;
        }

        return lessIndex - left;
    }

    private static int blockStart(int left, int block) {
        return left + block * BLOCK_SIZE;
    }

    private static int blockEnd(int left, int right, int block) {
        int end = left + block * BLOCK_SIZE + BLOCK_SIZE - 1;

        return Math.min(end, right);
    }
}
