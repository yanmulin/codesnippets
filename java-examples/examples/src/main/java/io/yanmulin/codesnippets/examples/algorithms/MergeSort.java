package io.yanmulin.codesnippets.examples.algorithms;

public class MergeSort {
    public void sort(int[] array) {
        int[] aux = new int[array.length];
        sortImpl(array, aux, 0, array.length - 1);
    }

    private void sortImpl(int[] array, int[] aux, int start, int end) {
        if (end <= start) return;
        int mid = (end + start) / 2;
        sortImpl(array, aux, start, mid);
        sortImpl(array, aux, mid + 1, end);

        int l = start, r = mid + 1;
        for (int p=start;p<=end;p++) {
            if (r > end || (l <= mid && array[l] < array[r])) {
                aux[p] = array[l];
                l ++;
            } else {
                aux[p] = array[r];
                r ++;
            }
        }

        for (int i=start;i<=end;i++) {
            array[i] = aux[i];
        }
    }

    public static void main(String[] args) {
        int[] arr = new int[]{4, 2, 1, 5, 6, 3};
        new MergeSort().sort(arr);
        for (int i=0;i<arr.length;i++) {
            System.out.print(arr[i] + " ");
        }
    }
}
