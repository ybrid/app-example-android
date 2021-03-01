package io.ybrid.app.example.android.ng.player;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import io.ybrid.player.io.audio.PCMDataBlock;
import io.ybrid.player.io.audio.output.AudioOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

/**
 * This class implements a simple {@link AudioOutput} using the Android's {@link AudioTrack}.
 */
public class AndroidAudioOutput implements AudioOutput {
    /**
     * This configures how many times the minimal buffer should be used for playback.
     */
    private static final int BUFFER_SCALE = 4;

    private @Nullable AudioTrack audioTrack;
    private @Nullable PCMDataBlock lastBlock;

    /**
     * Prepares the backend for playback.
     *
     * @param block A block of PCM data used to obtain initial audio configuration. This block must not be played.
     * @throws IOException Thrown on backend related I/O-Error.
     */
    @Override
    public void prepare(@NotNull PCMDataBlock block) throws IOException {
        final int sampleRate = block.getSampleRate();
        final int channels = block.getNumberOfChannels();
        final AudioAttributes.Builder attributesBuilder = new AudioAttributes.Builder();
        final AudioAttributes attributes;
        final AudioFormat.Builder formatBuilder = new AudioFormat.Builder();
        final AudioFormat format;
        final int channelConfig;
        int bufferSize;

        if (audioTrack != null)
            return;

        switch (channels) {
            case 1:
                channelConfig = AudioFormat.CHANNEL_OUT_MONO;
                break;
            case 2:
                channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
                break;

            default:
                throw new IllegalStateException("Unexpected number of channels: " + channels);
        }

        bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT);
        bufferSize *= BUFFER_SCALE;

        attributesBuilder.setUsage(AudioAttributes.USAGE_MEDIA);
        attributesBuilder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
        attributes = attributesBuilder.build();

        formatBuilder.setChannelMask(AudioFormat.CHANNEL_OUT_STEREO);
        formatBuilder.setEncoding(AudioFormat.ENCODING_PCM_16BIT);
        formatBuilder.setSampleRate(sampleRate);
        format = formatBuilder.build();

        audioTrack = new AudioTrack(attributes, format, bufferSize, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);
        lastBlock = block;
    }

    /**
     * Start playback mode of the backend.
     */
    @Override
    public void play() {
        Objects.requireNonNull(audioTrack).play();
    }

    /**
     * Set the backend into pause mode. Can be resumed by calling {@link #play()}.
     */
    @Override
    public void pause() {
        Objects.requireNonNull(audioTrack).pause();
    }

    /**
     * Writes an actual block of PCM data to the output buffer of the interface.
     * <p>
     * This call blocks on average for the time represented by the block,
     *
     * @param block The block to write to the backend.
     * @throws IOException Thrown on backend related I/O-Error.
     */
    @Override
    public void write(@NotNull PCMDataBlock block) throws IOException {
        final short[] buffer = block.getData();
        final int ret;

        if (audioTrack == null || lastBlock == null)
            throw new NullPointerException();

        if (block.getSampleRate() != lastBlock.getSampleRate() ||
                block.getNumberOfChannels() != lastBlock.getNumberOfChannels()) {
            close();
            prepare(block);
        }

        ret = audioTrack.write(buffer, 0, buffer.length);
        if (ret != buffer.length) {
            throw new RuntimeException("Short write");
        }

        lastBlock = block;
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        audioTrack.flush();
        audioTrack.stop();
        audioTrack.release();
        audioTrack = null;
        lastBlock = null;
    }
}
