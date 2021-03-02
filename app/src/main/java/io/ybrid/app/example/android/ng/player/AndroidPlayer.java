package io.ybrid.app.example.android.ng.player;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.LocaleList;
import io.ybrid.api.MediaEndpoint;
import io.ybrid.api.Session;
import io.ybrid.api.SwapMode;
import io.ybrid.player.player.MetadataConsumer;
import io.ybrid.player.player.SessionClient;
import io.ybrid.player.player.YbridPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This abstracts the {@link Session}, and {@link YbridPlayer}
 * in a way suitable for Android.
 */
public final class AndroidPlayer implements Closeable {
    private static final @NotNull String STREAM_URI = "https://stagecast.ybrid.io/adaptive-demo";

    private final @NotNull HandlerThread handlerThread;
    private final @NotNull Handler handler;
    private final @NotNull Session session;
    private @Nullable MetadataConsumer metadataConsumer;
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
            final @NotNull LocaleList list = LocaleList.getAdjustedDefault();
            final @NotNull List<Locale.LanguageRange> languages = new ArrayList<>(list.size());

            for (int i = 0; i < list.size(); i++) {
                // Use a weight of 1.0 for the default language, and 0.5 for every other language.
                //noinspection MagicNumber
                languages.add(new Locale.LanguageRange(list.get(i).toLanguageTag(), i == 0 ? 1 : 0.5));
            }

            mediaEndpoint.setAcceptedLanguages(languages);

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

    public synchronized void setMetadataConsumer(@NotNull MetadataConsumer metadataConsumer) {
        this.metadataConsumer = metadataConsumer;
        if (player != null)
            player.setMetadataConsumer(metadataConsumer);
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
                    if (metadataConsumer != null)
                        player.setMetadataConsumer(metadataConsumer);
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
