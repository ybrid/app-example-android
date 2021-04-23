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

import android.widget.TextView;
import io.ybrid.api.metadata.Metadata;
import io.ybrid.player.player.MetadataConsumer;
import io.ybrid.player.player.MetadataProvider;
import io.ybrid.player.player.PlayerState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * This class implements a simple {@link MetadataConsumer} that sets the current status
 * on a {@link TextView}.
 */
public class AndroidMetadataConsumer implements MetadataConsumer {
    private final @NotNull Runnable stopper;
    private final @NotNull TextView statusView;

    /**
     * Construct a new instance.
     *
     * @param stopper Function called when we want to stop playback.
     * @param statusView The {@link TextView} to update.
     */
    @Contract(pure = true)
    public AndroidMetadataConsumer(@NotNull Runnable stopper, @NotNull TextView statusView) {
        this.stopper = stopper;
        this.statusView = statusView;
    }

    /**
     * This function is called by the {@link MetadataProvider} when there is new {@link Metadata}
     * to be consumed.
     *
     * @param metadata The {@link Metadata} to consume.
     */
    @Override
    public void onMetadataChange(@NotNull Metadata metadata) {
        statusView.post(() -> statusView.setText(metadata.getCurrentItem().getDisplayTitle()));
    }

    /**
     * This function is called by the {@link MetadataProvider} when the state of the player changed.
     *
     * @param playerState The new state of the player.
     */
    @Override
    public void onPlayerStateChange(@NotNull PlayerState playerState) {
        statusView.post(() -> statusView.setText(playerState.toString()));

        // We stop the player here so that the user does not have to press stop
        // before he can press play again.
        if (playerState == PlayerState.ERROR)
            stopper.run();
    }
}
