import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author: billwang
 * @create: 4/21/21
 */
public class MediaPlayer {
    int frameCount = 0;
    int videoFrames;
    String videoPath;
    String audioPath;
    PlaySound audioPlayer = new PlaySound();
    JLabel currFrame = new JLabel();
    Thread videoThread;
    boolean isPlaying = false;
    boolean isPaused = false;
    BufferedImage imgOne = new BufferedImage(320, 180, BufferedImage.TYPE_INT_RGB);
    File[] toRead;

    public MediaPlayer() {
        JFrame frame = new JFrame("Media Player");
        JButton video = new JButton("Choose Frame");
        JButton audio = new JButton("Choose Audio");
        JButton start = new JButton("Play");
        JButton pause = new JButton(" Pause");

        video.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                jfc.setDialogTitle("Choose a directory to save your file: ");
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int returnValue = jfc.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    if (jfc.getSelectedFile().isDirectory()) {
                        videoPath = jfc.getSelectedFile().getPath();
                        toRead = VideoPlayer.readImages(videoPath);
//                        videoFrames = toRead.length;
                        videoFrames = 150;
                    }
                }
            }
        });

        audio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                jfc.setDialogTitle("Choose a dot wav file: ");
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int returnValue = jfc.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    if (jfc.getSelectedFile().isFile()) {
                        audioPath = jfc.getSelectedFile().getPath();
                        audioPlayer.loadFile(audioPath);
                    }
                }
            }
        });

        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPlaying) {
                    start.setText("Stop");
                    isPlaying = true;

                    Runnable video = new Runnable() {
                        @Override
                        public void run() {
                            frame.add(pause);
                            while (frameCount * 33333 <= audioPlayer.getTime()) {
                                VideoPlayer.readImageRGB(toRead[frameCount++], imgOne);
                                currFrame.setIcon(new ImageIcon(imgOne));
                            }
                            while (frameCount * 33333 > audioPlayer.getTime()) {
                            }
                            while (frameCount < videoFrames) {
                                if (frameCount * 33333 <= audioPlayer.getTime()) {
                                    VideoPlayer.readImageRGB(toRead[frameCount++], imgOne);
                                    currFrame.setIcon(new ImageIcon(imgOne));
                                }
                            }
                            audioPlayer.reset();
                        }
                    };

                    videoThread = new Thread(video);
                    videoThread.start();
                    audioPlayer.play();
                } else {
                    System.exit(0);
                }

            }
        });

        pause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPaused) {
                    audioPlayer.stop();
                    pause.setText("Resume");
                } else {
                    audioPlayer.play();
                    pause.setText("Pause");
                }
                isPaused = !isPaused;
            }
        });

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
            frame.add(start, c);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.ipady = 40;      //make this component tall
            c.weightx = 0.0;
            c.gridwidth = 3;
            c.gridx = 0;
            c.gridy = 1;
            frame.add(currFrame, c);

            frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setSize(450, 300);
            frame.setVisible(true);
        }

    }

    public static void main(String[] args) {
        MediaPlayer mp = new MediaPlayer();
    }
}
