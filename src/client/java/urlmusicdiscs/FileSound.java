package urlmusicdiscs;

import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.floatprovider.ConstantFloatProvider;
import org.jetbrains.annotations.Nullable;

public class FileSound implements SoundInstance {
    public String fileUrl;
    public Vec3d position;

    @Override
    public Identifier getId() {
        return new Identifier(URLMusicDiscs.MOD_ID, "customsound/" + fileUrl);
    }

    @Nullable
    @Override
    public WeightedSoundSet getSoundSet(SoundManager soundManager) {
        return new WeightedSoundSet(getId(), null);
    }

    @Override
    public Sound getSound() {
        return new Sound(getId().toString(), ConstantFloatProvider.create((float) getVolume()), ConstantFloatProvider.create((float) getPitch()), 1, Sound.RegistrationType.SOUND_EVENT, true, false, 64);
    }

    @Override
    public SoundCategory getCategory() {
        return SoundCategory.RECORDS;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public boolean isRelative() {
        return false;
    }

    @Override
    public int getRepeatDelay() {
        return 0;
    }

    @Override
    public float getVolume() {
        return 1;
    }

    @Override
    public float getPitch() {
        return 1;
    }

    @Override
    public double getX() {
        return position.x;
    }

    @Override
    public double getY() {
        return position.y;
    }

    @Override
    public double getZ() {
        return position.z;
    }

    @Override
    public AttenuationType getAttenuationType() {
        return SoundInstance.AttenuationType.LINEAR;
    }
}
