package io.ybrid.app.example.android.ng;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import io.ybrid.app.example.android.ng.player.AndroidMetadataConsumer;
import io.ybrid.app.example.android.ng.player.AndroidPlayer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    @NotNull AndroidPlayer player = new AndroidPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_play).setOnClickListener(v -> player.play());
        findViewById(R.id.button_swap).setOnClickListener(v -> player.swap());
        findViewById(R.id.button_stop).setOnClickListener(v -> player.stop());

        player.setMetadataConsumer(new AndroidMetadataConsumer(player::stop, findViewById(R.id.status_display)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            player.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}