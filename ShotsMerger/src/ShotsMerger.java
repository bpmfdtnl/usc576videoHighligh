import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author: billwang
 * @create: 4/24/21
 */
class shotNScore implements Comparable<shotNScore> {
    Shot shot;
    double score;

    public shotNScore(Shot shot, double score) {
        this.shot = shot;
        this.score = score;
    }

    @Override
    public int compareTo(shotNScore o) {
        return Double.compare(o.score, this.score);
    }
}

public class ShotsMerger {
    File[] videoFrames;
    PlaySound audioPlayer = new PlaySound();
    File soundScoreFile = new File("/Users/billwang/Desktop/VideoData/AudioScore.txt");
    File entropyScoreFile = new File("/Users/billwang/Desktop/VideoData/EntropyScore.txt");
    File shotsIndexFile = new File("/Users/billwang/Desktop/VideoData/ShotsFrames.txt");
    File motionScoreFile = new File("/Users/billwang/Desktop/VideoData/MotionScore.txt");

    String inputVideoDirectory;
    String inputAudioFile;
    String outputVideoDirectory = "/Users/billwang/Desktop/VideoData/output";
    String outputAudio = "/Users/billwang/Desktop/VideoData/output.wav";
    String outputAudioDirectory = "/Users/billwang/Desktop/temp";

    //contains to be displayed shots
    ArrayList<Shot> selectedShots = new ArrayList<>();
    ArrayList<Shot> shots = new ArrayList<>();
    ArrayList<Double> soundScore = new ArrayList<>();
    ArrayList<Double> entropyScore = new ArrayList<>();
    ArrayList<Double> motionScore = new ArrayList<>();



    public ShotsMerger(String videoFramesString, String audioFileString){
        inputVideoDirectory = videoFramesString;
        inputAudioFile = audioFileString;
        videoFrames = VideoPlayer.readImages(inputVideoDirectory);
        audioPlayer.loadFile(inputAudioFile);
        readScore();
        selectFrames();
        copyToFolder();
        modifyWav();
    }

    //edit .wav file
    public void modifyWav() {
        File dic = new File(outputAudioDirectory);
        dic.mkdirs();
        for (int i = 0; i < selectedShots.size(); i++) {
            Shot shot = selectedShots.get(i);
            AudioFileProcessor.copyAudioFrame(inputAudioFile, outputAudioDirectory + "/" + i + ".wav", shot.start, shot.end);
        }
        AudioFileProcessor.mergeAudioFile(outputAudioDirectory, outputAudio);
    }

    //copy selected shots frames to new folder
    public void copyToFolder() {
        File dic = new File(outputVideoDirectory);
        dic.mkdirs();
        for (Shot shot : selectedShots) {
            for (int i = shot.start; i < shot.end; i++) {
                File source = new File(inputVideoDirectory + String.format("/frame%d.rgb", i));
                File dest = new File(outputVideoDirectory + String.format("/frame%d.rgb", i));
                copyFileUsingStream(source, dest);
            }
        }
    }

    public void readScore() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(shotsIndexFile));
            String line = reader.readLine();
            while (line != null) {
                String[] oneLine = line.split(" ");
                Shot shot = new Shot(Integer.parseInt(oneLine[0]), Integer.parseInt(oneLine[1]));
                shots.add(shot);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            reader = new BufferedReader(new FileReader(soundScoreFile));
            String line = reader.readLine();
            while (line != null) {
                soundScore.add(Double.parseDouble(line));
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            reader = new BufferedReader(new FileReader(entropyScoreFile));
            String line = reader.readLine();
            while (line != null) {
                entropyScore.add(Double.parseDouble(line));
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            reader = new BufferedReader(new FileReader(motionScoreFile));
            String line = reader.readLine();
            while (line != null) {
                motionScore.add(Double.parseDouble(line));
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //edit this to combine different scores
    public void selectFrames() {
        ArrayList<shotNScore> shotNScores = new ArrayList<>();
        for (int i = 0; i < soundScore.size(); i++) {
            double score = soundScore.get(i) * 100 + entropyScore.get(i) + motionScore.get(i);
            shotNScores.add(new shotNScore(shots.get(i), score));
        }

        Collections.sort(shotNScores);
        int frameCount = 0;
        int frameTotal = 90 * 30;
        int index = 0;
        while (frameCount < frameTotal) {
            shotNScore curr = shotNScores.get(index);
            selectedShots.add(curr.shot);
            int currShotFrame = curr.shot.end - curr.shot.start;
            frameCount += currShotFrame;
            index++;
        }
        Collections.sort(selectedShots);
    }

    private static void copyFileUsingStream(File source, File dest) {
        try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
