import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * @author: billwang
 * @create: 5/2/21
 */
public class CreateHighlight {
    static String inputVideoDirectory;
    static String inputAudioFile;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        JFrame frame = new JFrame("Shot Merger");
        JButton video = new JButton("Choose Frame");
        JButton audio = new JButton("Choose Audio");
        JButton create = new JButton("Create Highlight");

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
                    }
                }
            }
        });

        create.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ShotsSelectorEntropy selector = new ShotsSelectorEntropy(inputVideoDirectory);
                String[] cmd = new String[]{"/Users/billwang/.pyenv/versions/3.8.0/bin/python", "/Users/billwang/Documents/GitHub/usc576videoHighligh/AudioScoring/audio-process.py", inputAudioFile, "/Users/billwang/Desktop/VideoData/ShotsFrames.txt", "/Users/billwang/Desktop/VideoData/AudioScore.txt"};
                try {
                    Runtime.getRuntime().exec(cmd);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                File temp = new File("/Users/billwang/Desktop/VideoData/AudioScore.txt");
                while (!temp.exists()) {
                    System.out.println("waiting");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
                ShotsMerger merger = new ShotsMerger(inputVideoDirectory, inputAudioFile);
                long finish = System.currentTimeMillis();
                long millis = finish - start;
                long seconds = (millis / 1000) % 60;
                long minutes = ((millis / 1000 - seconds)) / 60;
                create.setText(String.format("Finished in %d minutes %d seconds", minutes, seconds));
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
            frame.add(create, c);


            frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setSize(550, 100);
            frame.setVisible(true);
        }
    }

}
