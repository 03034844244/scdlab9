import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class WordCounter {
    private final Map<String, Integer> wordCount = new HashMap<>();
    private final Object lock = new Object();

    public void countWord(String word) {
        synchronized (lock) {
            wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
        }
    }

    public Map<String, Integer> getWordCount() {
        return wordCount;
    }
}

class FileProcessor implements Runnable {
    private final WordCounter wordCounter;
    private final String filePath;

    public FileProcessor(WordCounter wordCounter, String filePath) {
        this.wordCounter = wordCounter;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+");
                for (String word : words) {

                    word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
                    if (!word.isEmpty()) {
                        wordCounter.countWord(word);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class MultithreadingWordCounter {
    public static void main(String[] args) {
        String filePath = "sample.txt";
        int numThreads = 4;

        WordCounter wordCounter = new WordCounter();
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        try {
            for (int i = 0; i < numThreads; i++) {
                executorService.submit(new FileProcessor(wordCounter, filePath));
            }

            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            Map<String, Integer> finalWordCount = wordCounter.getWordCount();
            for (Map.Entry<String, Integer> entry : finalWordCount.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
