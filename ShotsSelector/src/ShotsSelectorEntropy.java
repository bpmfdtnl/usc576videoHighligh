import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.util.ArrayList;

/**
 * @author: harryzhao
 * Modified bill's ShotsSelector file
 * @create: 4/27/21
 */

public class ShotsSelectorEntropy {
    File[] frames;
    double entropyThreshold = 1.8;
    final int alpha = 9;
    final int beta = 3;
    final int gamma = 1;
    ArrayList<Double> entropyScore = new ArrayList<>();


    public ShotsSelectorEntropy(String folderPath) {
        frames = VideoPlayer.readImages(folderPath);
        ArrayList<Shot> shots =  this.selectShots();
        try {
            FileWriter myWriter = new FileWriter("/Users/billwang/Desktop/VideoData/ShotsFrames.txt");
            for (Shot shot : shots) {
                myWriter.write(shot.toString());
            }
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileWriter myWriter = new FileWriter("/Users/billwang/Desktop/VideoData/EntropyScore.txt");
            for (double score : this.entropyScore) {
                myWriter.write(String.valueOf(score));
                myWriter.write('\n');
            }
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //output arraylist of all shots with (start, end)
    public ArrayList<Shot> selectShots() {
        double entropySum = 0;
        ArrayList<Shot> shots = new ArrayList<>();
        int frameNumber = this.frames.length;
        double prevEntropy = generateEntropy(frames[0]);
        entropySum += prevEntropy;
        double currEntropy;
        int shotStartFrame = 0;
        for (int i = 1; i < frameNumber; i++) {
            currEntropy = generateEntropy(frames[i]);
            if (overThreshold(prevEntropy, currEntropy, i)) {
                shots.add(new Shot(shotStartFrame, i));
                entropyScore.add(entropySum / (i - shotStartFrame));
                shotStartFrame = i;
                entropySum = 0;
            }
            entropySum += currEntropy;
            prevEntropy = currEntropy;
        }
        entropyScore.add(entropySum / (frameNumber - 1 - shotStartFrame));
        shots.add(new Shot(shotStartFrame, frameNumber - 1));
        return shots;
    }

    //check if the difference is over the threshold
    private boolean overThreshold(double prevEntropy, double currEntropy, int index) {
        return Math.abs(currEntropy - prevEntropy) > entropyThreshold;
    }

    //generate the entropy for the current frame
    private double generateEntropy(File frame) {
        double entropy = 0.0;
        int[] hueBins = new int[8];
        int[] saturationBins = new int[3];
        int[] valueBins = new int[3];

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
                    int r = Byte.toUnsignedInt(bytes[ind]);
                    int g = Byte.toUnsignedInt(bytes[ind + height * width]);
                    int b = Byte.toUnsignedInt(bytes[ind + height * width * 2]);

                    double r1 = r / 255.0;
                    double g1 = g / 255.0;
                    double b1 = b / 255.0;

                    double Cmax = Math.max(r1, Math.max(g1, b1));
                    double Cmin = Math.min(r1, Math.min(g1, b1));
                    double delta = Cmax - Cmin;

                    double hue = calculateHue(r1, g1, b1, Cmax, delta);
                    double saturation = calculateSaturation(Cmax, delta);
                    double value = Cmax;

                    hueBins[findHueBin(hue)]++;
                    saturationBins[findSaturationBin(saturation)]++;
                    valueBins[findValueBin(value)]++;

                    ind++;
                }
            }
            entropy = getEntropy(hueBins,saturationBins,valueBins);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return entropy;
    }

    //From HW2, calculate hue, saturation
    private double calculateHue(double r1, double g1, double b1, double Cmax, double delta) {
        double hue = 0.0;
        if (delta == 0.0) {
            hue = 0.0;
        } else if (Cmax == r1) {
            hue = 60 * (((g1 - b1) / delta) % 6);
            if (hue < 0)
            {
                hue = hue + 360;
            }
        } else if (Cmax == g1) {
            hue = 60 * ((b1 - r1) / delta + 2);
        } else if (Cmax == b1) {
            hue = 60 * ((r1 - g1) / delta + 4);
        }
        return hue;
    }

    private double calculateSaturation(double Cmax, double delta) {
        double saturation;
        if (Cmax == 0) {
            saturation = 0;
        } else {
            saturation = delta / Cmax;
        }
        return saturation;
    }

    // find bins for hue, saturation, and value
    private int findHueBin(double hue){
        int res = 0;
        if (hue <= 45) res = 0;
        else if (hue > 45 && hue <= 90) res = 1;
        else if (hue > 90 && hue <= 135) res = 2;
        else if (hue > 135 && hue <= 180) res = 3;
        else if (hue > 180 && hue <= 225) res = 4;
        else if (hue > 225 && hue <= 270) res = 5;
        else if (hue > 270 && hue <= 315) res = 6;
        else if (hue > 315 && hue <= 360) res = 7;
        return res;
    }

    private int findSaturationBin(double saturation){
        int res = 0;
        if (saturation <= 1/3) res = 0;
        else if (saturation > 1/3 && saturation <= 2/3) res = 1;
        else res = 2;
        return res;
    }

    private int findValueBin(double value){
        int res = 0;
        if (value <= 1/3) res = 0;
        else if (value > 1/3 && value <= 2/3) res = 1;
        else res = 2;
        return res;
    }

    //Calculate the entropy of current frame using HSV bins
    private double getEntropy(int[] hueBin, int[] saturationBin, int[] valueBin){
        double entropy = 0.0;

        double[] PHue = getProbability(hueBin);
        double[] PSaturation = getProbability(saturationBin);
        double[] PValue = getProbability(valueBin);

        double hueEntropy = calculateEntropy(PHue);
        double satEntropy = calculateEntropy(PSaturation);
        double valEntropy = calculateEntropy(PValue);

        entropy = alpha * hueEntropy + beta * satEntropy + gamma * valEntropy;

        return entropy;

    }

    private double[] getProbability(int[] bin){
        double[] res = new double[bin.length];
        int sum = 0;
        for (int i = 0; i < bin.length; i++){
            sum += bin[i];
        }
        for (int i = 0; i < bin.length; i++){
            res[i] = bin[i] * 1.0 / sum * 1.0;
        }
        return res;
    }

    private double calculateEntropy(double[] prob){
        double entropy = 0.0;
        for (int i = 0; i < prob.length; i++){
            if (prob[i] == 0) {
                continue;
            }
            entropy = entropy + prob[i] * (Math.log(prob[i]) / Math.log(2));
        }
        if (entropy == 0) {
            return 0.0;
        }
        else{
            return -entropy;
        }
    }


}
