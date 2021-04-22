import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class VideoPlayer {

    //read all rgb files from filePath directory
    public static void readImages(BufferedImage[] images, String filePath, int size) {
        for (int i = 0; i < size; i++) {
            BufferedImage imgOne = new BufferedImage(320, 180, BufferedImage.TYPE_INT_RGB);
            String filename = String.format("/frame%d.rgb", i);
            readImageRGB(filePath + filename, imgOne);
            images[i] = imgOne;
        }
    }

    //read in each image rgb file
    static void readImageRGB(String imgPath, BufferedImage img) {
        try {
            int frameLength = 320 * 180 * 3;

            File file = new File(imgPath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(0);

            byte[] bytes = new byte[(int) (long) frameLength];

            raf.read(bytes);

            int ind = 0;
            for (int y = 0; y < 180; y++) {
                for (int x = 0; x < 320; x++) {
                    byte r = bytes[ind];
                    byte g = bytes[ind + 180 * 320];
                    byte b = bytes[ind + 180 * 320 * 2];

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    img.setRGB(x, y, pix);
                    ind++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}