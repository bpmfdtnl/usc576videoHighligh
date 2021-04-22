import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class PlaySound {
    Clip clip;
    AudioFormat format;

    public void play() {
        clip.start();
    }

    public long getPosition(){
        return clip.getFramePosition();
    }

    public float getSampleRate(){
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