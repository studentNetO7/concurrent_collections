import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Analyser {
    private static final BlockingQueue<String> queueWithLetterA = new ArrayBlockingQueue<>(100);
    private static final BlockingQueue<String> queueWithLetterB = new ArrayBlockingQueue<>(100);
    private static final BlockingQueue<String> queueWithLetterC = new ArrayBlockingQueue<>(100);

    private static final AtomicInteger counterA = new AtomicInteger(0);
    private static final AtomicInteger counterB = new AtomicInteger(0);
    private static final AtomicInteger counterC = new AtomicInteger(0);


    public static void main(String[] args) throws InterruptedException {
        // Поток для заполнения очередей
        Thread generatorThread = new Thread(() -> {
            for (int i = 0; i < 10_000; i++) {
                String text = generateText("abc", 100_000);
                try {
                    queueWithLetterA.put(text);
                    queueWithLetterB.put(text);
                    queueWithLetterC.put(text);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        // Поток для подсчета буквы 'a'
        Thread processingLetterAThread = new Thread(() -> {
            while (true) {
                try {
                    letterCounter('a', queueWithLetterA.take(), counterA);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });

        // Поток для подсчета буквы 'b'
        Thread processingLetterBThread = new Thread(() -> {
            while (true) {
                try {
                    letterCounter('b', queueWithLetterB.take(), counterB);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        // Поток для подсчета буквы 'c'
        Thread processingLetterCThread = new Thread(() -> {
            while (true) {
                try {
                    letterCounter('c', queueWithLetterC.take(), counterC);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        // Запускаем все потоки
        generatorThread.start();
        processingLetterAThread.start();
        processingLetterBThread.start();
        processingLetterCThread.start();
        // Ожидаем завершения потока-генератора
        generatorThread.join();
        // Завершаем все потоки
        processingLetterAThread.interrupt();
        processingLetterBThread.interrupt();
        processingLetterCThread.interrupt();

        // Выводим на печать результаты подсчетов
        System.out.println("Максимальное количество 'a': " + counterA.get());
        System.out.println("Максимальное количество 'b': " + counterB.get());
        System.out.println("Максимальное количество 'c': " + counterC.get());
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    private static void letterCounter(char letter, String text, AtomicInteger counter) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (c == letter) {
                count++;
            }
        }
        int currentCount = count;
        counter.updateAndGet(previousCount -> Math.max(previousCount, currentCount));
    }
}
