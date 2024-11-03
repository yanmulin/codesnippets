package io.yanmulin.codesnippets.examples.concurrency;

import java.util.Random;

public class DemonstrateDeadlock {

    private static final int NUM_THREADS = 20;
    private static final int NUM_ACCOUNTS = 5;
    private static final int NUM_ITERATIONS = 1000;
    private static final Random RANDOM = new Random();

    class Account {
        int balance = 100000;
    }

    class TransferThread extends Thread {

        private Account[] accounts;

        public TransferThread(Account[] accounts) {
            this.accounts = accounts;
        }

        @Override
        public void run() {
            for (int i = 0; i < NUM_ITERATIONS; i ++) {
                int from = RANDOM.nextInt(NUM_ACCOUNTS);
                int to = RANDOM.nextInt(NUM_ACCOUNTS);
                int amount = RANDOM.nextInt(1000);
                transferMoney(accounts[from], accounts[to], amount);
            }
        }
    }

    private void transferMoney(Account from, Account to, int amount) {
        synchronized (from) {
            synchronized (to) {
                if (from.balance >= amount) {
                    from.balance -= amount;
                    to.balance += amount;
                }
            }
        }
    }

    public void run() {
        final Account[] accounts = new Account[NUM_ACCOUNTS];

        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new Account();
        }

        for (int i = 0; i < NUM_THREADS; i ++) {
            new TransferThread(accounts).start();
        }
    }

    public static void main(String[] args) {
        new DemonstrateDeadlock().run();
    }
}
