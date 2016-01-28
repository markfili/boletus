package hr.vvg.filipovic.boletus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.neuroph.contrib.imgrec.ImageRecognitionPlugin;
import org.neuroph.contrib.imgrec.image.Image;
import org.neuroph.contrib.imgrec.image.ImageFactory;
import org.neuroph.core.NeuralNetwork;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private static final int PHOTO_RESULT_CODE = 100;

    private NeuralNetwork nnet;
    private ImageRecognitionPlugin imageRecognition;

    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        loadData();
    }

    private void loadData() {
        // TODO show dialog
        new Thread(null, loadDataRunnable, "networkLoader", 32000).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PHOTO_RESULT_CODE) {
                Uri selectedImage = data.getData();
                // image filepath
                imagePath = selectedImage.getPath();
                Image image = ImageFactory.getImage(imagePath);

                Log.i(TAG, "onActivityResult: result is " + recognize(image));
            }
        }
    }

    private String recognize(Image image) {
        // todo show dialog
        HashMap<String, Double> output = imageRecognition.recognizeImage(image);
        return getAnswer(output);
    }

    private String getAnswer(HashMap<String, Double> output) {
        double highest = 0;
        String answer = "";

        for (Map.Entry<String, Double> entry : output.entrySet()) {
            if (entry.getValue() > highest) {
                highest = entry.getValue();
                answer = entry.getKey();
            }
        }
        return answer;
    }

    @OnClick(R.id.fab)
    protected void onSelectPhotoClick() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        // show gallery
        startActivityForResult(intent, PHOTO_RESULT_CODE);
    }

    private Runnable loadDataRunnable = new Runnable() {
        @Override
        public void run() {
            // open neural network
            InputStream is = getResources().openRawResource(R.raw.neural_network);
            // load neural network
            nnet = NeuralNetwork.load(is);
            imageRecognition = (ImageRecognitionPlugin) nnet.getPlugin(ImageRecognitionPlugin.class);

            // TODO show dialog
        }
    };

}
