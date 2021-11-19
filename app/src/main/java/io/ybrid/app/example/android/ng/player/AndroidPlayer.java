/*
 * Copyright (c) 2021 nacamar GmbH - Ybrid®, a Hybrid Dynamic Live Audio Technology
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.ybrid.app.example.android.ng.player;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.LocaleList;
import io.ybrid.api.MediaEndpoint;
import io.ybrid.api.SwapMode;
import io.ybrid.api.session.Session;
import io.ybrid.player.player.MediaController;
import io.ybrid.player.player.MetadataConsumer;
import io.ybrid.player.player.YbridPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * This abstracts the {@link Session}, and {@link YbridPlayer}
 * in a way suitable for Android.
 */
public final class AndroidPlayer implements Closeable {
    private static final @NotNull URI STREAM_URI = URI.create("https://democast.ybrid.io/adaptive-demo");

    private final @NotNull HandlerThread handlerThread;
    private final @NotNull Handler handler;
    private @Nullable Session session;
    private @Nullable MetadataConsumer metadataConsumer;
    private MediaController player;

    {
        // Create a Thread to run the player in. This is required
        // as Android does not allow network activity on the main thread.
        handlerThread = new HandlerThread("Android Ybrid Player Handler"); //NON-NLS
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        switchURI(STREAM_URI);
    }

    public void switchURI(@NotNull URI uri) {
        final @NotNull Session nextSession;

        // Create a MediaEndpoint, and a Session from it.
        try {
            final @NotNull MediaEndpoint mediaEndpoint = new MediaEndpoint(uri);
            final @NotNull LocaleList list = LocaleList.getAdjustedDefault();
            final @NotNull List<Locale.LanguageRange> languages = new ArrayList<>(list.size());

            for (int i = 0; i < list.size(); i++) {
                // Use a weight of 1.0 for the default language, and 0.5 for every other language.
                //noinspection MagicNumber
                languages.add(new Locale.LanguageRange(list.get(i).toLanguageTag(), i == 0 ? 1 : 0.5));
            }

            mediaEndpoint.setAcceptedLanguages(languages);

            nextSession = mediaEndpoint.createSession();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // Connect the session.
        handler.post(() -> {
            try {
                stopOnSameThread();
                session = nextSession;
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
             synchronized (AndroidPlayer.this) {
                 if (player != null)
                     return;
                 player = new YbridPlayer(Objects.requireNonNull(session));
                 if (metadataConsumer != null)
                     player.setMetadataConsumer(metadataConsumer);
                 player.play();
             }
         });
    }

    private synchronized void stopOnSameThread() {
        if (player != null)
            player.stop();
        player = null;
    }

    /**
     * Stop playback.
     */
    public void stop() {
        if (player != null) {
            handler.post(this::stopOnSameThread);
        }
    }

    /**
     * Swap current item.
     */
    public void swap() {
        handler.post(() -> player.swapItem(SwapMode.END2END));
    }

    @Override
    public void close() throws IOException {
        handlerThread.quitSafely();
    }
}
