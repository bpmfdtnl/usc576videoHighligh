import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    File soundScoreFile;
    ArrayList<Shot> shots = new ArrayList<>();
    ArrayList<Double> soundScore = new ArrayList<>();
    String inputVideoDirectory;
    String inputAudioFile;
    //Change these to save files to different folders
    String outputVideoDirectory = "/Users/billwang/Desktop/output";
    String outputAudio = "/Users/billwang/Desktop/output.wav";
    String outputAudioDirectory = "/Users/billwang/Desktop/temp";

    //contains to be displayed shots
    ArrayList<Shot> selectedShots = new ArrayList<>();


    public ShotsMerger() {
        JFrame frame = new JFrame("Shot Merger");
        JButton video = new JButton("Choose Frame");
        JButton audio = new JButton("Choose Audio");
        JButton soundScore = new JButton("Choose Sound Score File");
        JButton merge = new JButton("Merge");

        video.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                jfc.setDialogTitle("Choose a directory to save your file: ");
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int returnValue = jfc.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    if (jfc.getSelectedFile().isDirectory()) {
                        inputVideoDirectory = jfc.getSelectedFile().getPath();
                        videoFrames = VideoPlayer.readImages(inputVideoDirectory);
                    }
                }
            }
        });

        //open port to play audio
        audio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                jfc.setDialogTitle("Choose a dot wav file: ");
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int returnValue = jfc.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    if (jfc.getSelectedFile().isFile()) {
                        inputAudioFile = jfc.getSelectedFile().getPath();
                        audioPlayer.loadFile(inputAudioFile);
                    }
                }
            }
        });

        soundScore.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                jfc.setDialogTitle("Choose a directory to save your file: ");
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

                int returnValue = jfc.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    if (jfc.getSelectedFile().isDirectory()) {
                        videoFrames = VideoPlayer.readImages(jfc.getSelectedFile().getPath());
                    }
                    soundScoreFile = jfc.getSelectedFile();
                }
            }
        });

        merge.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readScore();
                selectFrames();
                copyToFolder();
                modifyWav();
                merge.setText("Finished!!!");
            }
        });

        //GUI layout
        {
            frame.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            //natural height, maximum width
            c.fill = GridBagConstraints.HORIZONTAL;

            c.weightx = 0.5;
            c.gridx = 0;
            c.gridy = 0;
            frame.add(video, c);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.5;
            c.gridx = 1;
            c.gridy = 0;
            frame.add(audio, c);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.5;
            c.gridx = 2;
            c.gridy = 0;
            frame.add(soundScore, c);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.5;
            c.gridx = 3;
            c.gridy = 0;
            frame.add(merge, c);

            frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setSize(450, 300);
            frame.setVisible(true);
        }
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
            reader = new BufferedReader(new FileReader(soundScoreFile));
            String line = reader.readLine();
            while (line != null) {
                String[] oneLine = line.split(" ");
                Shot shot = new Shot(Integer.parseInt(oneLine[0]), Integer.parseInt(oneLine[1]));
                shots.add(shot);
                soundScore.add(Double.parseDouble(oneLine[2]));
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
            shotNScores.add(new shotNScore(shots.get(i), soundScore.get(i)));
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

    public static void main(String[] args) {
        ShotsMerger shotsMerger = new ShotsMerger();
    }
}
