import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class VideoPlayer {

    //get all files from directory and sort based on file name
    public static File[] readImages(String filePath) {
        File folder = new File(filePath);
        File[] frames = folder.listFiles();
        assert frames != null;

        return Arrays.stream(frames).
                filter(file -> file.getName().endsWith(".rgb"))
                .sorted((o1, o2) ->
                {
                    int name1 = Integer.parseInt(o1.getName().substring(0, o1.getName().length() - 4).substring(5));
                    int name2 = Integer.parseInt(o2.getName().substring(0, o2.getName().length() - 4).substring(5));
                    return Integer.compare(name1, name2);
                })
                .toArray(File[]::new);
    }

    //read in each image rgb file
    static void readImageRGB(File file, BufferedImage img) {
        try {
            int frameLength = 320 * 180 * 3;

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