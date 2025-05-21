import java.util.*;
import java.lang.Integer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Lab1 {
    private static final String ALPHABET = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
    private static final String INPUT_FILE = "resources\\Lab1_encrypted.txt";
    private static final String OUTPUT_FILE = "resources\\Lab1_decrypted.txt";

    private static final double[] RUSSIAN_FREQ = {
            8.01, 1.59, 4.54, 1.70, 2.98,  // А-Д
            8.45, 0.04, 0.94, 1.65, 7.35,  // Е-И
            1.21, 3.49, 4.40, 3.21, 6.70,   // Й-Н
            10.96, 2.81, 4.73, 5.47, 6.26,  // О-Т
            2.62, 0.26, 0.97, 0.48, 1.44,   // У-Ч
            0.73, 0.36, 0.04, 1.90, 1.74,   // Ш-Ъ
            0.32, 0.64, 2.01                 // Э-Я
    };
    private static final int MAX_KEY_LENGTH = 20;

    public static void main(String[] args) {
        String ciphertext = readFromFile(INPUT_FILE);

        ciphertext = ciphertext.toUpperCase();
        String ciphertextAlp = ciphertext.replaceAll("[^\\p{IsAlphabetic}]", "");

        int keyLength = findKeyLength(ciphertextAlp);
        System.out.println("Предполагаемая длина ключа: " + keyLength);

        String key = findKey(ciphertextAlp, keyLength);
        System.out.println("Найденный ключ: " + key);

        String decrypted = decrypt(ciphertext, key);
        writeToFile(OUTPUT_FILE, decrypted);
    }

    public static String readFromFile(String filePath) {
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }

        return content.toString();
    }

    public static void writeToFile(String filePath, String text) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(text);
            System.out.println("Текст успешно записан в файл.");
        } catch (IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }

    private static double countCI(String str, int keyLength) {
        String group = getGroup(str, 0, keyLength);

        Map<Integer, Integer> charsAmount = new HashMap<>();
        group.chars()
                .forEach(it -> charsAmount.put(it, charsAmount.getOrDefault(it, 0) + 1));

        return charsAmount.values().stream()
                .map(x -> Double.valueOf(x * x))
                .reduce(0.0, Double::sum) / (group.length() * group.length());

    }

    public static int findKeyLength(String ciphertext) {
        double[] cis = new double[MAX_KEY_LENGTH];
        int l = 1;
        for (int i = 0; i < MAX_KEY_LENGTH; i++) {
            cis[i] = countCI(ciphertext, i + 1);
            if (cis[i] > cis[l - 1]) {
                l = i + 1;
            }
        }

        return l;
    }


    public static String findKey(String ciphertext, int keyLength) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < keyLength; i++) {
            String group = getGroup(ciphertext, i, keyLength);
            int shift = findShift(group);
            key.append(ALPHABET.charAt(shift));
        }
        return key.toString();
    }

    private static String getGroup(String ciphertext, int start, int step) {
        StringBuilder group = new StringBuilder();
        for (int i = start; i < ciphertext.length(); i += step) {
            group.append(ciphertext.charAt(i));
        }
        return group.toString();
    }

    private static int findShift(String group) {
        double minChiSq = Double.MAX_VALUE;
        int bestShift = 0;

        for (int shift = 0; shift < ALPHABET.length(); shift++) {
            double chiSq = 0.0;
            for (int i = 0; i < group.length(); i++) {
                char c = group.charAt(i);
                int originalPos = (ALPHABET.indexOf(c) - shift + ALPHABET.length()) % ALPHABET.length();
                double expected = RUSSIAN_FREQ[originalPos] * group.length() / 100;
                double observed = countLetter(group, c);
                chiSq += Math.pow(observed - expected, 2) / expected;
            }
            if (chiSq < minChiSq) {
                minChiSq = chiSq;
                bestShift = shift;
            }
        }
        return bestShift;
    }

    private static int countLetter(String text, char letter) {
        return (int)text.chars().filter(x -> x == letter).count();
    }

    public static String decrypt(String ciphertext, String key) {
        StringBuilder plaintext = new StringBuilder();
        for (int i = 0, j = 0; i < ciphertext.length(); i++, j++) {
            while (i < ciphertext.length()
                    && !ALPHABET.contains(String.valueOf(ciphertext.charAt(i)))) {
                plaintext.append(ciphertext.charAt(i));
                i++;
            }

            if (i >= ciphertext.length()) {
                continue;
            }

            char c = ciphertext.charAt(i);
            char k = key.charAt(j % key.length());
            int cPos = ALPHABET.indexOf(c);
            int kPos = ALPHABET.indexOf(k);
            int pPos = (cPos - kPos + RUSSIAN_FREQ.length) % RUSSIAN_FREQ.length;
            plaintext.append(ALPHABET.charAt(pPos));
        }
        return plaintext.toString();
    }
}