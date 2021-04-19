package io.ybrid.app.example.android.ng;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import io.ybrid.app.example.android.ng.player.AndroidMetadataConsumer;
import io.ybrid.app.example.android.ng.player.AndroidPlayer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;

public class MainActivity extends AppCompatActivity {
    private final @NotNull AndroidPlayer player = new AndroidPlayer();

    private void askURI() {
        final @NotNull AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final @NotNull EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        builder.setView(et);
        builder.setTitle(R.string.title_change_URI);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> player.switchURI(URI.create(et.getText().toString())));
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_play).setOnClickListener(v -> player.play());
        findViewById(R.id.button_swap).setOnClickListener(v -> player.swap());
        findViewById(R.id.button_stop).setOnClickListener(v -> player.stop());

        findViewById(R.id.button_change_uri).setOnClickListener(v -> askURI());

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