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
