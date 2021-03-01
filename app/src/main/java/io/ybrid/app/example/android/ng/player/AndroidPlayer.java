package io.ybrid.app.example.android.ng.player;

import android.os.Handler;
import android.os.HandlerThread;
import io.ybrid.api.*;
import io.ybrid.player.player.SessionClient;
import io.ybrid.player.player.YbridPlayer;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;

/**
 * This abstracts the {@link Session}, and {@link YbridPlayer}
 * in a way suitable for Android.
 */
public final class AndroidPlayer implements Closeable {
    private static final @NotNull String STREAM_URI = "https://stagecast.ybrid.io/adaptive-demo";

    private final @NotNull HandlerThread handlerThread;
    private final @NotNull Handler handler;
    private final @NotNull Session session;
    private SessionClient player;

    {
        // Create a Thread to run the player in. This is required
        // as Android does not allow network activity on the main thread.
        handlerThread = new HandlerThread("Android Ybrid Player Handler"); //NON-NLS
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        // Create a MediaEndpoint, and a Session from it.
        try {
            final @NotNull MediaEndpoint mediaEndpoint = new MediaEndpoint(URI.create(STREAM_URI));

            mediaEndpoint.forceApiVersion(ApiVersion.YBRID_V1);
            mediaEndpoint.getWorkarounds().disable(Workaround.WORKAROUND_SKIP_SILENCE);

            session = mediaEndpoint.createSession();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // Connect the session.
        handler.post(() -> {
            try {
                session.connect();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Start Playback.
     */
    public void play() {
         handler.post(() -> {
            try {
                synchronized (AndroidPlayer.this) {
                    if (player != null)
                        return;
                    player = new YbridPlayer(session, null, AndroidAudioOutput::new);
                    player.prepare();
                    player.play();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Stop playback.
     */
    public void stop() {
        if (player != null) {
            handler.post(() -> {
                try {
                    synchronized (AndroidPlayer.this) {
                        player.stop();
                        player = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * Swap current item.
     */
    public void swap() {
        handler.post(() -> {
            try {
                player.swapItem(SwapMode.END2END);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void close() throws IOException {
        handlerThread.quitSafely();
    }
}
