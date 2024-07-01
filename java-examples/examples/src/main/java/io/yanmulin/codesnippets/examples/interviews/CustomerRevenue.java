package io.yanmulin.codesnippets.examples.interviews;

import java.util.*;

/*
Customer Revenue
https://www.1point3acres.com/bbs/interview/databricks-software-engineer-775327.html
https://www.1point3acres.com/bbs/thread-1024685-1-1.html
 */
public class CustomerRevenue {
    long nextId = 0;
    Map<Long, Integer> id2Revenue = new HashMap<>();
    TreeMap<Integer, Set<Long>> revenue2Id = new TreeMap<>();
    Map<Long, Long> referrer = new HashMap<>();

    long insert(int revenue) {
        long id = nextId;
        id2Revenue.put(id, revenue);
        revenue2Id.compute(revenue, (key, ids) -> {
            if (ids == null) ids = new HashSet<>();
            ids.add(id);
            return ids;
        });
        return nextId ++;
    }

    long insert(int revenue, long referrerId) {
        int referrerRevenue = id2Revenue.get(referrerId);
        id2Revenue.put(referrerId, referrerRevenue + revenue);
        revenue2Id.compute(referrerRevenue, (key, ids) -> {
            ids.remove(referrerId);
            return ids;
        });
        revenue2Id.compute(referrerRevenue + revenue, (key, ids) -> {
            if (ids == null) ids = new HashSet<>();
            ids.add(referrerId);
            return ids;
        });

        long id = insert(revenue);
        referrer.put(id, referrerId);
        return id;
    }

    List<Long> getKLowestRevenue(int k, int targetRevenue) {
        List<Long> result = new ArrayList<>();
        NavigableMap<Integer, Set<Long>> m = revenue2Id.tailMap(targetRevenue, false);
        while (result.size() < k) {
            Map.Entry<Integer, Set<Long>> entry = m.pollFirstEntry();
            result.addAll(entry.getValue());
        }
        return result;
    }

    List<Long> getReferrerChain(long id) {
        List<Long> chain = new ArrayList<>();
        chain.add(id);
        long currentId = id;
        while (referrer.containsKey(currentId)) {
            Long referrerId = referrer.get(currentId);
            chain.add(referrerId);
            currentId = referrerId;
        }
        return chain;
    }

    public static void main(String[] args) {
        CustomerRevenue cr = new CustomerRevenue();
        long c1 = cr.insert(500);
        long c2 = cr.insert(750);
        long c3 = cr.insert(850);
        long c4 = cr.insert(300, c1);
        long c5 = cr.insert(150, c4);

        // expected [0, 1]
        System.out.println(cr.getKLowestRevenue(2, 650));
        // expected [4, 3, 0]
        System.out.println(cr.getReferrerChain(c5));
    }
}
