import java.util.Random;
import java.util.concurrent.Semaphore;

public class DiningPhilosophersWaiter {

    static class Philosopher extends Thread {
        private final int id;
        private final Semaphore[] forks;   // her çatal için 1 permit
        private final Semaphore waiter;    // N-1 permit (butler)
        private final Random rnd = new Random();

        Philosopher(int id, Semaphore[] forks, Semaphore waiter) {
            this.id = id;
            this.forks = forks;
            this.waiter = waiter;
            setName("Philosopher-" + id);
        }

        private void think() throws InterruptedException {
            Thread.sleep(200 + rnd.nextInt(400));
        }

        private void eat() throws InterruptedException {
            Thread.sleep(200 + rnd.nextInt(400));
        }

        @Override
        public void run() {
            int left = id;
            int right = (id + 1) % forks.length;

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    think();

                    // Garson: aynı anda en fazla N-1 filozof masaya "giriş" yapıp çatal almaya çalışabilir
                    waiter.acquire();

                    // Çatalları al
                    forks[left].acquire();
                    forks[right].acquire();

                    System.out.println(getName() + " eating with forks " + left + " & " + right);
                    eat();

                    // Çatalları bırak
                    forks[right].release();
                    forks[left].release();

                    // Garson iznini bırak
                    waiter.release();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final int N = 5;

        // fair=true: bekleyenler sırayla alır -> starvation riskini azaltır
        Semaphore[] forks = new Semaphore[N];
        for (int i = 0; i < N; i++) forks[i] = new Semaphore(1, true);

        // N-1 permit: deadlock'u kıran kritik nokta
        Semaphore waiter = new Semaphore(N - 1, true);

        Philosopher[] philosophers = new Philosopher[N];
        for (int i = 0; i < N; i++) {
            philosophers[i] = new Philosopher(i, forks, waiter);
            philosophers[i].start();
        }

        // Demo: 10 saniye çalıştır
        Thread.sleep(10_000);
        for (Philosopher p : philosophers) p.interrupt();
        for (Philosopher p : philosophers) p.join();

        System.out.println("Finished.");
    }
}
