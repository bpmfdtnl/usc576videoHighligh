import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

class playVideo {
    static int count = 0;
    static int size = 200;
    static Timer tm;
    static String filePath;
    static BufferedImage[] images = new BufferedImage[size];



    //create JFrame and set timer to display video
    public static void main(String[] args) {
        JFrame frame = new JFrame("Media Player");
        JButton video=new JButton("Choose Frame Directory");
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
                        filePath = jfc.getSelectedFile().getPath();
                        readImages(images, filePath);
                        frame.remove(video);
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
                        filePath = jfc.getSelectedFile().getPath();
                        System.out.println("--------dot wav selected---------");
                        frame.remove(audio);
                        frame.setSize(320, 180);
                    }
                }
            }
        });


        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tm.start();
                frame.remove(start);
            }
        });



        video.setBounds(50,100,95,30);
        audio.setBounds(50,200,95,30);
        start.setBounds(50,200,95,30);
        frame.add(video);
        frame.add(audio);
        frame.add(start);

        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);
        JLabel currFrame = new JLabel();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 1;
        frame.setLocationRelativeTo(null);

        tm = new Timer(33, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currFrame.setIcon(new ImageIcon(images[count++]));
                if (count >= size){
                    tm.stop();
                }
            }
        });



        frame.getContentPane().add(currFrame, c);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    //read all rgb files from filePath directory
    private static void readImages(BufferedImage[] images, String filePath) {
        for (int i = 0; i < size; i++) {
            BufferedImage imgOne = new BufferedImage(320, 180, BufferedImage.TYPE_INT_RGB);
            String filename = String.format("/frame%d.rgb", i);
            readImageRGB(filePath+filename, imgOne);
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