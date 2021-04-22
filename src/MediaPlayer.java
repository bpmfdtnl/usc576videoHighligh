import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * @author: billwang
 * @create: 4/21/21
 */
public class MediaPlayer {
    int frameCount = 0;
    int videoSeconds = 90;
    int videoFrames = videoSeconds * 30;
    Timer tm;
    String videoPath;
    String audioPath;
    BufferedImage[] images = new BufferedImage[videoFrames];
    PlaySound audioPlayer = new PlaySound();
    JLabel currFrame = new JLabel();


    public MediaPlayer() {
        JFrame frame = new JFrame("Media Player");
        JButton video = new JButton("Choose Frame Directory");
        JButton audio = new JButton("Choose Audio File");
        JButton start = new JButton("Play");

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
                        VideoPlayer.readImages(images, videoPath, videoFrames);
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
                Runnable video = new Runnable() {
                    @Override
                    public void run() {
                        float spf = audioPlayer.getSampleRate() / 30;
                        while (frameCount > Math.round(audioPlayer.getPosition()/spf)){
                        }
                        while (frameCount < Math.round(audioPlayer.getPosition()/spf)){
                            currFrame.setIcon(new ImageIcon(images[frameCount++]));
                        }

                        while (frameCount < videoFrames){
                            while (frameCount > Math.round(audioPlayer.getPosition()/spf)){ }
                            while (frameCount < Math.round(audioPlayer.getPosition()/spf)){
                                currFrame.setIcon(new ImageIcon(images[frameCount++]));
                            }
                            currFrame.setIcon(new ImageIcon(images[frameCount++]));
                        }
                    }
                };

                Runnable audio = new Runnable() {
                    @Override
                    public void run() {
                        audioPlayer.play();
                    }
                };
                Thread videoThread = new Thread(video);
                Thread audioThread = new Thread(audio);
                audioThread.start();
                videoThread.start();

            }
        });

        //set up GUI layout
        {
            video.setBounds(50, 100, 95, 30);
            audio.setBounds(50, 200, 95, 30);
            start.setBounds(50, 200, 95, 30);
            frame.add(video);
            frame.add(audio);
            frame.add(start);

            GridBagLayout gLayout = new GridBagLayout();
            frame.getContentPane().setLayout(gLayout);
            currFrame = new JLabel();
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.CENTER;
            c.weightx = 0.5;
            c.gridx = 0;
            c.gridy = 1;
            frame.setLocationRelativeTo(null);
            frame.getContentPane().add(currFrame, c);
            frame.pack();
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setSize(450, 180);
            frame.setVisible(true);
        }
    }

    public static void main(String[] args) {
        MediaPlayer mp = new MediaPlayer();
    }
}
