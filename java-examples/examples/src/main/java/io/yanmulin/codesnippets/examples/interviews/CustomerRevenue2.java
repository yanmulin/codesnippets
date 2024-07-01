package io.yanmulin.codesnippets.examples.interviews;

import java.util.*;

public class CustomerRevenue2 {

    long nextId = 0;

    Map<Long, Integer> customer2Revenue = new HashMap<>();
    Map<Long, Long> referrers = new HashMap<>();
    TreeMap<Integer, Set<Long>> revenue2Customer = new TreeMap<>();

    long insert(int revenue) {
        long id = nextId;
        customer2Revenue.put(id, revenue);
        revenue2Customer.compute(revenue, (k, v) -> {
            if (v == null) {
                v = new HashSet<>();
            }
            v.add(id);
            return v;
        });
        nextId ++;
        return id;
    }

    long insert(int revenue, long referrerId) {
        long id = insert(revenue);
        referrers.put(id, referrerId);
        int referrerRevenue = customer2Revenue.get(referrerId);
        revenue2Customer.get(referrerRevenue).remove(referrerId);
        customer2Revenue.put(referrerId, referrerRevenue + revenue);
        revenue2Customer.compute(referrerRevenue + revenue, (k, v) -> {
            if (v == null) {
                v = new HashSet<>();
            }
            v.add(referrerId);
            return v;
        });
        return id;
    }

    List<Long> getKLowestRevenue(int k, int targetRevenue) {
        NavigableMap<Integer, Set<Long>> entries = revenue2Customer.tailMap(targetRevenue, false);
        System.out.println(entries);
        List<Long> ans = new ArrayList<>();
        while (ans.size() < k && !entries.isEmpty()) {
            Map.Entry<Integer, Set<Long>> entry = entries.pollFirstEntry();
            ans.addAll(entry.getValue());
        }
        return ans;
    }

    List<Long> getReferrerChain(long id) {
        long currentId = id;
        List<Long> ans = new ArrayList<>();
        ans.add(id);
        while (referrers.containsKey(currentId)) {
            currentId = referrers.get(currentId);
            ans.add(currentId);
        }
        return ans;
    }

    public static void main(String[] args) {
        CustomerRevenue2 cr = new CustomerRevenue2();
        long c1 = cr.insert(500);
        long c2 = cr.insert(750);
        long c3 = cr.insert(850);
        long c4 = cr.insert(300, c1);
        long c5 = cr.insert(150, c4);

        // [800, 750, 850, 450, 150)

        // expected [0, 1]
        System.out.println(cr.getKLowestRevenue(2, 650));
        // expected [4, 3, 0]
        System.out.println(cr.getReferrerChain(c5));
    }
}