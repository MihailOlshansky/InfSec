import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class Lab7 {
    private static final String INPUT_AUDIO = "resources\\Lab7_original.wav";
    private static final String OUTPUT_AUDIO = "resources\\Lab7_hidden.wav";
    private static final String MESSAGE = "Сообщение секретное в аудио вообще жесть!";

    private static final String END_OF_MESSAGE = "1111111111111110";

    public static void main(String[] args) {
        try {
            encode(INPUT_AUDIO, MESSAGE, OUTPUT_AUDIO);
            String decodedMessage = decode(OUTPUT_AUDIO);
            System.out.println("Извлечённое сообщение: " + decodedMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void encode(String audioPath, String message, String outputPath) throws IOException, UnsupportedAudioFileException {
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(audioPath));
        AudioFormat format = audioStream.getFormat();

        byte[] audioBytes = getBytes(audioStream, format);

        encodeIntoAudio(message, audioBytes);

        ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
        AudioInputStream newAudioStream = new AudioInputStream(bais, format, audioBytes.length / format.getFrameSize());
        AudioSystem.write(newAudioStream, AudioFileFormat.Type.WAVE, new File(outputPath));

        System.out.println("Сообщение успешно скрыто в " + outputPath);
    }

    private static void encodeIntoAudio(String message, byte[] audioBytes) {
        StringBuilder binaryMsg = new StringBuilder();
        for (char c : message.toCharArray()) {
            StringBuilder currChar = new StringBuilder(Integer.toBinaryString(c));
            currChar.insert(0, "0".repeat(16 - currChar.length()));
            binaryMsg.append(currChar);
        }
        binaryMsg.append(END_OF_MESSAGE);

        int availableBits = audioBytes.length * 8;
        if (binaryMsg.length() > availableBits) {
            throw new IllegalArgumentException("Сообщение слишком длинное для аудиофайла");
        }

        for (int i = 0; i < binaryMsg.length(); i++) {
            int bitValue = binaryMsg.charAt(i) == '1' ? 1 : 0;
            audioBytes[i] = (byte) ((audioBytes[i] & 0xFE) | bitValue);
        }
    }

    public static String decode(String audioPath) throws IOException, UnsupportedAudioFileException {
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(audioPath));
        AudioFormat format = audioStream.getFormat();

        byte[] audioBytes = getBytes(audioStream, format);

        StringBuilder result = decodeFromAudio(audioBytes);

        return result.toString();
    }

    private static StringBuilder decodeFromAudio(byte[] audioBytes) {
        StringBuilder binaryMsg = new StringBuilder();
        StringBuilder result = new StringBuilder();

        for (byte audioByte : audioBytes) {
            int lsb = audioByte & 1;
            binaryMsg.append(lsb);

            if (binaryMsg.length() >= 16) {
                String lastByte = binaryMsg.substring(binaryMsg.length() - 16);
                if (lastByte.equals(END_OF_MESSAGE)) {
                    break;
                }
            }
        }

        if (binaryMsg.length() >= 16) {
            binaryMsg.setLength(binaryMsg.length() - 16);
        }

        for (int i = 0; i < binaryMsg.length(); i += 16) {
            if (i + 16 > binaryMsg.length()) {
                break;
            }
            String byteStr = binaryMsg.substring(i, i + 16);
            int charCode = Integer.parseInt(byteStr, 2);
            result.append((char) charCode);
        }
        return result;
    }

    private static byte[] getBytes(AudioInputStream audioStream, AudioFormat format) throws IOException, UnsupportedAudioFileException {
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            throw new UnsupportedAudioFileException("Поддерживаются только PCM-аудиофайлы");
        }

        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = audioStream.read(buffer)) != -1) {
            byteOutputStream.write(buffer, 0, bytesRead);
        }
        byte[] audioBytes = byteOutputStream.toByteArray();
        return audioBytes;
    }

}