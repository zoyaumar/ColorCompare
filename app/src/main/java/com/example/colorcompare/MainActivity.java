package com.example.colorcompare;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.colorcompare.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MainActivity extends AppCompatActivity {

    Button add_button;
    Button upload_button;
    Button color1_button;
    Button color2_button;
    Button color3_button;
    EditText input;
    TextView color1_text;
    TextView color2_text;
    TextView color3_text;
    ImageView logo;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        add_button = findViewById(R.id.button4);
        upload_button = findViewById(R.id.button6);
        color1_button = findViewById(R.id.button2);
        color2_button = findViewById(R.id.button3);
        color3_button = findViewById(R.id.button);
        input = findViewById(R.id.editTextText);
        color1_text = findViewById(R.id.textView4);
        color2_text = findViewById(R.id.textView5);
        color3_text = findViewById(R.id.textView6);
        logo = findViewById(R.id.imageView2);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        String imageUrl = "https://wallup.net/wp-content/uploads/2016/01/315311-landscape.jpg"; // Replace with your image URL

        Glide.with(this)
                .load(imageUrl)
                .into(logo);

        new Thread(new Runnable() {
            @Override
            public void run() {
                //fetchImageTags();
            }
        }).start();

        //
        //  {"result": {"colors": {"background_colors": [{"b": 79, "closest_palette_color": "blueberry", "closest_palette_color_html_code": "#2d3359", "closest_palette_color_parent": "navy blue", "closest_palette_distance": 7.23020076751709, "g": 29, "html_code": "#1d1d4f", "percent": 43.374095916748, "r": 29}, {"b": 243, "closest_palette_color": "ice blue", "closest_palette_color_html_code": "#9bb7d4", "closest_palette_color_parent": "light blue", "closest_palette_distance": 6.8101224899292, "g": 184, "html_code": "#a4b8f3", "percent": 32.1508140563965, "r": 164}, {"b": 150, "closest_palette_color": "electric blue", "closest_palette_color_html_code": "#363b7c", "closest_palette_color_parent": "blue", "closest_palette_distance": 4.46446228027344, "g": 63, "html_code": "#3c3f96", "percent": 24.4750938415527, "r": 60}], "color_percent_threshold": 1.75, "color_variance": 49, "foreground_colors": [{"b": 210, "closest_palette_color": "royal blue", "closest_palette_color_html_code": "#00539c", "closest_palette_color_parent": "blue", "closest_palette_distance": 10.9151659011841, "g": 95, "html_code": "#465fd2", "percent": 50.4572639465332, "r": 70}, {"b": 241, "closest_palette_color": "periwinkle", "closest_palette_color_html_code": "#81a0d4", "closest_palette_color_parent": "light blue", "closest_palette_distance": 5.57353496551514, "g": 145, "html_code": "#7891f1", "percent": 47.5064010620117, "r": 120}, {"b": 40, "closest_palette_color": "fiesta", "closest_palette_color_html_code": "#be5141", "closest_palette_color_parent": "red", "closest_palette_distance": 8.91189098358154, "g": 40, "html_code": "#ee2828", "percent": 2.03633689880371, "r": 238}], "image_colors": [{"b": 113, "closest_palette_color": "electric blue", "closest_palette_color_html_code": "#363b7c", "closest_palette_color_parent": "blue", "closest_palette_distance": 4.57987833023071, "g": 44, "html_code": "#2a2c71", "percent": 55.8667793273926, "r": 42}, {"b": 240, "closest_palette_color": "periwinkle", "closest_palette_color_html_code": "#81a0d4", "closest_palette_color_parent": "light blue", "closest_palette_distance": 4.82036256790161, "g": 167, "html_code": "#94a7f0", "percent": 39.8112754821777, "r": 148}, {"b": 41, "closest_palette_color": "fiesta", "closest_palette_color_html_code": "#be5141", "closest_palette_color_parent": "red", "closest_palette_distance": 6.29721069335938, "g": 54, "html_code": "#b83629", "percent": 4.22439861297607, "r": 184}], "object_percentage": 15.9937887191772}}, "status": {"text": "", "type": "success"}}

    }

    private void fetchImageTags() {
        String credentialsToEncode = "acc_4b98ddb60e3ded4" + ":" + "1e8a345cc9296bb0ad349432ee24d01a";
        String basicAuth = Base64.getEncoder().encodeToString(credentialsToEncode.getBytes(StandardCharsets.UTF_8));

        String endpointUrl = "https://api.imagga.com/v2/colors";
        String imageUrl = "https://th.bing.com/th/id/R.b54866c50ec8db76df03fd1c78ee6691?rik=yJN8Yc2W%2f1yvEQ&pid=ImgRaw&r=0";

        HttpURLConnection connection = null;
        try {
            String url = endpointUrl + "?image_url=" + imageUrl;
            URL urlObject = new URL(url);
            connection = (HttpURLConnection) urlObject.openConnection();

            connection.setRequestProperty("Authorization", "Basic " + basicAuth);

            int responseCode = connection.getResponseCode();
            Log.d("ImaggaApi", "Sending 'GET' request to URL : " + url);
            Log.d("ImaggaApi", "Response Code : " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader connectionInput = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder jsonResponse = new StringBuilder();
                String line;
                while ((line = connectionInput.readLine()) != null) {
                    jsonResponse.append(line);
                }
                connectionInput.close();
                Log.d("json", jsonResponse.toString());
            } else {
                Log.e("ImaggaApi", "Error: " + responseCode);
            }

        } catch (IOException e) {
            Log.e("ImaggaApi", "IOException: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


}