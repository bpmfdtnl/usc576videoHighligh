import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.io.FileOutputStream;

public class PlaySound {
    Clip clip;
    AudioFormat format;

    public void play() {
        clip.start();
    }

    public long getTime(){
        return clip.getMicrosecondPosition();
    }

    public void end(){
        clip.close();
    }


    public float getSampleRate(){
        System.out.println(format.getFrameRate());
        return format.getFrameRate();
    }

    public void loadFile(String filePath) {
        try {
            File Sound = new File(filePath);
            this.clip = AudioSystem.getClip();
            this.format = clip.getFormat();
            clip.open(AudioSystem.getAudioInputStream(Sound));
        } catch (Exception ignored) {
        }

    }
}