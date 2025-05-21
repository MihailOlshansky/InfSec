import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Lab6 {
    private static final String INPUT_IMAGE = "resources\\Lab6_original.png";
    private static final String OUTPUT_IMAGE = "resources\\Lab6_hidden.png";
    private static final String MESSAGE = "Сообщение секретное в картинке вообще жесть!";

    private static final String END_OF_MESSAGE = "1111111111111110";

    public static void main(String[] args) {
        try {
            encode(INPUT_IMAGE, MESSAGE, OUTPUT_IMAGE);
            String decodedMessage = decode(OUTPUT_IMAGE);
            System.out.println("\nИзвлечённое сообщение: " + decodedMessage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void encode(String imagePath, String message, String outputPath) throws IOException {
        BufferedImage img = ImageIO.read(new File(imagePath));
        int width = img.getWidth();
        int height = img.getHeight();

        StringBuilder binaryMsg = new StringBuilder();
        for (char c : message.toCharArray()) {
            StringBuilder currChar = new StringBuilder(Integer.toBinaryString(c));
            currChar.insert(0, "0".repeat(16 - currChar.length()));
            binaryMsg.append(currChar);
        }
        binaryMsg.append(END_OF_MESSAGE);

        if (binaryMsg.length() > width * height * 3) {
            throw new IllegalArgumentException("Сообщение слишком длинное для изображения!");
        }

        encodeIntoImage(img, binaryMsg);

        ImageIO.write(img, "png", new File(outputPath));
        System.out.println("Сообщение успешно скрыто в " + outputPath);
    }

    private static void encodeIntoImage(BufferedImage img, StringBuilder binaryMsg) {
        int width = img.getWidth();
        int height = img.getHeight();
        int msgIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int a = (rgb >> 24) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                if (msgIndex < binaryMsg.length()) {
                    r = (r & 0xFE) | (binaryMsg.charAt(msgIndex) - '0');
                    msgIndex++;
                }
                if (msgIndex < binaryMsg.length()) {
                    g = (g & 0xFE) | (binaryMsg.charAt(msgIndex) - '0');
                    msgIndex++;
                }
                if (msgIndex < binaryMsg.length()) {
                    b = (b & 0xFE) | (binaryMsg.charAt(msgIndex) - '0');
                    msgIndex++;
                }

                img.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);

                if (msgIndex >= binaryMsg.length()) {
                    break;
                }
            }
            if (msgIndex >= binaryMsg.length()) {
                break;
            }
        }
    }

    public static String decode(String imagePath) throws IOException {
        BufferedImage img = ImageIO.read(new File(imagePath));

        StringBuilder binaryMsg = getBinaryData(img);

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < binaryMsg.length(); i += 16) {
            if (i + 16 > binaryMsg.length()) {
                break;
            }
            String byteStr = binaryMsg.substring(i, i + 16);
            int charCode = Integer.parseInt(byteStr, 2);
            result.append((char) charCode);
        }

        return result.toString();
    }

    private static StringBuilder getBinaryData(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        StringBuilder binaryMsg = new StringBuilder();

        outer:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                binaryMsg.append(r & 1);
                if (checkIfEnd(binaryMsg)) {
                    break outer;
                }
                binaryMsg.append(g & 1);
                if (checkIfEnd(binaryMsg)) {
                    break outer;
                }
                binaryMsg.append(b & 1);

                if (checkIfEnd(binaryMsg)) {
                    break outer;
                }
            }
        }

        if (binaryMsg.length() >= 16) {
            binaryMsg.setLength(binaryMsg.length() - 16);
        }
        return binaryMsg;
    }

    private static boolean checkIfEnd(StringBuilder binaryMsg) {
        return binaryMsg.length() >= 16
                && binaryMsg.substring(binaryMsg.length() - 16).equals(END_OF_MESSAGE);
    }
}