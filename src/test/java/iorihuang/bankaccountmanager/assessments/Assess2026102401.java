package iorihuang.bankaccountmanager.assessments;

import java.util.*;
public class Assess2026102401 {

    public static int[] orderPizza(int[] orderPlaced, int size) {
        // Handle edge cases to avoid division by zero or empty-array errors
        if (orderPlaced == null || orderPlaced.length == 0 || size == 0) {
            return new int[0];
        }

        List<Integer> resultList = new ArrayList<>();
        Deque<Integer> deque = new ArrayDeque<>();

        // Sliding window
        for (int i = 0; i < orderPlaced.length; i++) {
            // If this is a meat pizza (negative number), add it to the tail of the deque
            if (orderPlaced[i] < 0) {
                deque.addLast(orderPlaced[i]);
            }

            // Once the window reaches the given size, record the result and slide the window
            if (i >= size - 1) {
                // If the deque is not empty, its head is the first meat pizza in the current window
                if (!deque.isEmpty()) {
                    resultList.add(deque.peekFirst());
                } else {
                    // If the deque is empty, the current window has no meat pizza, so output 0
                    resultList.add(0);
                }

                // Slide the window: if the leftmost element of the current window is a meat pizza
                // (i.e. equals the deque head), remove it from the deque
                if (!deque.isEmpty() && orderPlaced[i - size + 1] == deque.peekFirst()) {
                    deque.pollFirst();
                }
            }
        }

        // Convert the List to an int[] array and return it
        int[] answer = new int[resultList.size()];
        for (int i = 0; i < resultList.size(); i++) {
            answer[i] = resultList.get(i);
        }

        return answer;


        int[] answer = new int[100];
        // Write your code here

        // Handle edge cases
        if (orderPlaced == null || orderPlaced.length == 0 || size <= 0) {
            return new int[0];
        }

        int ansIdx = 0;
        // Use an array to simulate a monotonic deque, storing indices of negative numbers (meat pizzas)
        int[] deque = new int[orderPlaced.length];
        int head = 0;
        int tail = 0;

        for (int i = 0; i < orderPlaced.length; i++) {
            // If the current order is a meat pizza (negative), add its index to the deque
            if (orderPlaced[i] < 0) {
                deque[tail++] = i;
            }

            // When the window size reaches K, start recording the result and slide the window
            if (i >= size - 1) {
                // Remove indices from the head of the deque that are out of the current window
                while (head < tail && deque[head] <= i - size) {
                    head++;
                }

                // Safety check to prevent index out of bounds since answer length is fixed to 100
                if (ansIdx < 100) {
                    if (head < tail) {
                        // The head of the deque is the first meat pizza in the current window
                        answer[ansIdx++] = orderPlaced[deque[head]];
                    } else {
                        // No meat pizza in the current window
                        answer[ansIdx++] = 0;
                    }
                }
            }
        }

        // Truncate the array to the actual number of recorded results
        return Arrays.copyOf(answer, ansIdx);
    }
    public static int[] orderPizza2(int[] orderPlaced, int size)
    {
        int[] answer = new int[100];
        // Write your code here

        // Handle edge cases
        if (orderPlaced == null || orderPlaced.length == 0 || size <= 0) {
            return new int[0];
        }

        int ansIdx = 0;
        // Use an array to simulate a monotonic deque, storing indices of negative numbers (meat pizzas)
        int[] deque = new int[orderPlaced.length];
        int head = 0;
        int tail = 0;

        for (int i = 0; i < orderPlaced.length; i++) {
            // If the current order is a meat pizza (negative), add its index to the deque
            if (orderPlaced[i] < 0) {
                deque[tail++] = i;
            }

            // When the window size reaches K, start recording the result and slide the window
            if (i >= size - 1) {
                // Remove indices from the head of the deque that are out of the current window
                while (head < tail && deque[head] <= i - size) {
                    head++;
                }

                // Safety check to prevent index out of bounds since answer length is fixed to 100
                if (ansIdx < 100) {
                    if (head < tail) {
                        // The head of the deque is the first meat pizza in the current window
                        answer[ansIdx++] = orderPlaced[deque[head]];
                    } else {
                        // No meat pizza in the current window
                        answer[ansIdx++] = 0;
                    }
                }
            }
        }

        // Truncate the array to the actual number of recorded results
        return Arrays.copyOf(answer, ansIdx);
    }


    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        // input for orderPlaced
        if (!in.hasNextInt()) return;
        int orderPlaced_size = in.nextInt();
        int orderPlaced[] = new int[orderPlaced_size];
        for(int idx = 0; idx < orderPlaced_size; idx++) {
            orderPlaced[idx] = in.nextInt();
        }
        // input for size
        int size = in.nextInt();

        int[] result = orderPizza(orderPlaced, size);

        // Print the output following the provided printing logic
        for(int idx = 0; idx < result.length - 1; idx++) {
            System.out.print(result[idx] + " ");
        }
        // Guard against out-of-bounds access when the array is empty
        if (result.length > 0) {
            System.out.print(result[result.length - 1]);
        }
    }
}
