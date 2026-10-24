package iorihuang.bankaccountmanager.assessments;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
public class Assess2026102402 {
    public static int cacheMisses(int[] page_requests, int max_cache_size)
    {
        int answer = 0;
        // Write your code here

        // Edge case: if cache size is 0, every request is a miss
        if (max_cache_size <= 0) {
            return page_requests.length;
        }

        // Use a Queue (LinkedList) to implement the FIFO cache
        Queue<Integer> cache = new LinkedList<>();

        for (int page : page_requests) {
            // Check if the requested page is already in the cache (Cache Hit)
            if (cache.contains(page)) {
                // For strict FIFO, cache hit does not change the order of elements.
                // The element stays at its original position in the queue.
                continue;
            }

            // Cache Miss occurs
            answer++;

            // If the cache is full, remove the oldest element (the head of the queue)
            if (cache.size() == max_cache_size) {
                cache.poll();
            }

            // Add the new page to the cache
            cache.offer(page);
        }

        return answer;
    }

    public static void main(String[] args)
    {
        Scanner in = new Scanner(System.in);
        //input for page_requests
        if (!in.hasNextInt()) return;
        int page_requests_size = in.nextInt();
        int page_requests[] = new int[page_requests_size];
        for(int idx = 0; idx < page_requests_size; idx++)
        {
            page_requests[idx] = in.nextInt();
        }
        // input for max_cache_size
        int max_cache_size = in.nextInt();

        int result = cacheMisses(page_requests, max_cache_size);
        System.out.print(result);
    }
}
