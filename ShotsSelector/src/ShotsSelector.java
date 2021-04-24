import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.util.ArrayList;

/**
 * @author: billwang
 * @create: 4/23/21
 */

class Shot {
    int start;
    int end;

    public Shot(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return String.format("%d %d\n", start, end);
    }
}

public class ShotsSelector {
    File[] frames;
    int threshold = 50000;

    public ShotsSelector(String folderPath) {
        frames = VideoPlayer.readImages(folderPath);
    }

    public static void main(String[] args) {
        //Get video directory
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setDialogTitle("Choose a directory to save your file: ");
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        ShotsSelector shotsSelector;
        ArrayList<Shot> shots = new ArrayList<>();

        int returnValue = jfc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            if (jfc.getSelectedFile().isDirectory()) {
                String folderPath = jfc.getSelectedFile().getPath();
                shotsSelector = new ShotsSelector(folderPath);
                shots = shotsSelector.selectShots();
            }
        }

        //write result to this file
        try {
            FileWriter myWriter = new FileWriter("VideoShots.txt");
            for (Shot shot : shots) {
                myWriter.write(shot.toString());
            }
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //output arraylist of all shots with (start, end)
    private ArrayList<Shot> selectShots() {
        ArrayList<Shot> shots = new ArrayList<>();
        int frameNumber = this.frames.length;
        int[] prevHist = generateHist(frames[0]);
        int[] currHist;
        int shotStartFrame = 0;
        for (int i = 1; i < frameNumber; i++) {
            currHist = generateHist(frames[i]);
            if (overThreshold(prevHist, currHist, i)) {
                shots.add(new Shot(shotStartFrame, i));
                shotStartFrame = i;
            }
            prevHist = currHist;
        }
        shots.add(new Shot(shotStartFrame, frameNumber - 1));
        return shots;
    }

    //check if the difference is over the threshold
    private boolean overThreshold(int[] prevHist, int[] currHist, int index) {
        int sum = 0;
        for (int i = 0; i < prevHist.length; i++) {
            sum += Math.abs(prevHist[i] - currHist[i]);
        }
        return sum > this.threshold;
    }

    //generate the HSV histogram
    private int[] generateHist(File frame) {
        int[] hist = new int[361];
        try {
            int width = 320;
            int height = 180;
            int frameLength = width * height * 3;

            RandomAccessFile raf = new RandomAccessFile(frame, "r");
            raf.seek(0);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);

            int ind = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    byte r = bytes[ind];
                    byte g = bytes[ind + height * width];
                    byte b = bytes[ind + height * width * 2];

                    double R = Byte.toUnsignedInt(r);
                    double G = Byte.toUnsignedInt(g);
                    double B = Byte.toUnsignedInt(b);

                    int colorValue = colorValue(R, G, B);
                    hist[colorValue]++;
                    ind++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hist;
    }

    //same function from HW2 calculate HSV color degree of each pixel
    private int colorValue(double R, double G, double B) {
        double min = Math.min(Math.min(R, G), B);
        double max = Math.max(Math.max(R, G), B);
        double delta = max - min;

        double h = 0;
        if (delta == 0) {
            h = 0;
        } else {
            if (max == R) {
                h = (G - B) / delta % 6;
            } else if (max == G) {
                h = 2 + (B - R) / delta;
            } else if (max == B) {
                h = 4 + (R - G) / delta;
            }
        }

        h = h * 60;
        if (h < 0) {
            h += 360;
        }
        return (int) h;
    }
}
