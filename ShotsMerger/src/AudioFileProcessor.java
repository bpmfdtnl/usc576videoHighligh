import java.io.*;
import java.util.Arrays;
import javax.sound.sampled.*;

class AudioFileProcessor {

    public static void copyAudioSecond(String sourceFileName, String destinationFileName, int startSecond, int secondsToCopy) {
        AudioInputStream inputStream = null;
        AudioInputStream shortenedStream = null;
        try {
            File file = new File(sourceFileName);
            AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
            AudioFormat format = fileFormat.getFormat();
            inputStream = AudioSystem.getAudioInputStream(file);
            int bytesPerSecond = format.getFrameSize() * (int) format.getFrameRate();
            inputStream.skip(startSecond * bytesPerSecond);
            long framesOfAudioToCopy = secondsToCopy * (int) format.getFrameRate();
            shortenedStream = new AudioInputStream(inputStream, format, framesOfAudioToCopy);
            File destinationFile = new File(destinationFileName);
            AudioSystem.write(shortenedStream, fileFormat.getType(), destinationFile);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        if (shortenedStream != null)
            try {
                shortenedStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static void copyAudioFrame(String sourceFileName, String destinationFileName, int startFrame, int endFrame) {
        AudioInputStream inputStream = null;
        AudioInputStream shortenedStream = null;
        int shotLength = endFrame - startFrame;
        try {
            File file = new File(sourceFileName);
            AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
            AudioFormat format = fileFormat.getFormat();
            inputStream = AudioSystem.getAudioInputStream(file);
            int bytesPerSecond = format.getFrameSize() * (int) format.getFrameRate();
            int bytesPerVideoFrame = bytesPerSecond / 30;
            inputStream.skip(startFrame * bytesPerVideoFrame);
            long framesOfAudioToCopy = shotLength * (int) format.getFrameRate() / 30;
            shortenedStream = new AudioInputStream(inputStream, format, framesOfAudioToCopy);
            File destinationFile = new File(destinationFileName);
            AudioSystem.write(shortenedStream, fileFormat.getType(), destinationFile);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        if (shortenedStream != null)
            try {
                shortenedStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public static void mergeAudioFile(String directory, String outputWav) {
        try {
            File dir = new File(directory);
            File[] waves = dir.listFiles();

            waves = Arrays.stream(waves).
                    filter(file -> file.getName().endsWith(".wav"))
                    .sorted((o1, o2) ->
                    {
                        int name1 = Integer.parseInt(o1.getName().substring(0, o1.getName().length() - 4));
                        int name2 = Integer.parseInt(o2.getName().substring(0, o2.getName().length() - 4));
                        return Integer.compare(name1, name2);
                    })
                    .toArray(File[]::new);

            AudioInputStream clip1 = AudioSystem.getAudioInputStream(waves[0]);
            AudioInputStream clip2 = AudioSystem.getAudioInputStream(waves[1]);
            long sum = clip1.getFrameLength() + clip2.getFrameLength();
            SequenceInputStream sq = new SequenceInputStream(clip1, clip2);

            for (int i = 2; i < waves.length; i++) {
                AudioInputStream clip = AudioSystem.getAudioInputStream(waves[i]);
                sq = new SequenceInputStream(sq, clip);
                sum += clip.getFrameLength();
            }

            AudioInputStream appendedFiles =
                    new AudioInputStream(
                            sq,
                            clip1.getFormat(),
                            sum);

            AudioSystem.write(appendedFiles,
                    AudioFileFormat.Type.WAVE,
                    new File(outputWav));

            deleteDirectory(dir);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}