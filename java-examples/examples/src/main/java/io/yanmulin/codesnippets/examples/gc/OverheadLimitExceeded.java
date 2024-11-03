package io.yanmulin.codesnippets.examples.gc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OverheadLimitExceeded {
    public void exceedOverheadLimit() {
       int i = 0;
       List<String> list = new ArrayList<>();

       try {
           while (true) {
               list.add(UUID.randomUUID().toString().intern());
               i ++;
           }
       } catch (Throwable t) {
           System.out.println("*********i: " + i);
           t.printStackTrace();
           throw t;
       }
    }

    public void outOfHeapMemory() {
        String str = "";
        int i = 0;
        try {
            while (true) {
                str += UUID.randomUUID().toString();
                i ++;
            }
        } catch (Throwable t) {
            System.out.println("*********i: " + i);
            t.printStackTrace();
            throw t;
        }
    }

    public static void main(String[] args) {
        new OverheadLimitExceeded().exceedOverheadLimit();
    }
}
