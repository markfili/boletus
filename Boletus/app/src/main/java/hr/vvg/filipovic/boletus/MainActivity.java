package hr.vvg.filipovic.boletus;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.imgrec.ColorMode;
import org.neuroph.imgrec.ImageRecognitionPlugin;
import org.neuroph.imgrec.image.Dimension;
import org.neuroph.imgrec.image.ImageAndroid;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hr.vvg.filipovic.boletus.base.BaseActivity;

/**
 * Main activity for loading the neural network and showing results.
 *
 * Created by marko on 12/02/16.
 */
public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PHOTO_RESULT_CODE = 100;
    private static final int SCALE_WIDTH = 20;
    private static final int SCALE_HEIGHT = 20;
    private static final int ORIGINAL_WIDTH = 200;
    private static final int ORIGINAL_HEIGHT = 151;
    private static final int RGB_PIXELS_VALUE = 3;

    private NeuralNetwork nnet;
    private ImageRecognitionPlugin imageRecognition;

    @Bind(R.id.imageView)
    protected ImageView imageView;

    @Bind(R.id.loading)
    TextView loadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // init activity layout
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        // bind views from layout with fields in activity
        ButterKnife.bind(this);

        // initialize network loading and setup
        loadData();
    }

    /**
     * Starts loading the neural network in a separate Thread.
     */
    private void loadData() {
        showDialog();
        // start a new thread to load neural network
        new Thread(null, loadDataRunnable, "networkLoader", 256000).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap image = null;
        Uri selectedImage;
        if (resultCode == RESULT_OK) {
            if (requestCode == PHOTO_RESULT_CODE) {
                try {
                    // picking an image returns an URI with image location
                    selectedImage = data.getData();
                    // get selected image Bitmap from its location
                    image = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // show results of recognition on UI
                showResults(image, recognize(image));
                Log.i(TAG, "onActivityResult: result is " + recognize(image));
            }
        }
    }

    /**
     * Displays the selected image in an ImageView and recognition result in a TextView.
     *
     * @param bitmap selected image to be shown.
     * @param result result text from image recognition.
     */
    private void showResults(Bitmap bitmap, String result) {
        imageView.setImageBitmap(bitmap);
        loadingText.setText(getString(R.string.result_text, result));
    }

    /**
     * Downscales the selected image and returns result from image recognition.
     *
     * @param bitmap image to be downscaled and recognized.
     * @return image recognition result in String format.
     */
    private String recognize(Bitmap bitmap) {
        showDialog();

        Bitmap image = Bitmap.createScaledBitmap(bitmap, SCALE_WIDTH, SCALE_HEIGHT, false);

        imageRecognition.setInput(new ImageAndroid(image));
        imageRecognition.processInput();

        HashMap<String, Double> output = imageRecognition.getOutput();
        return getAnswer(output);
    }

    private String getAnswer(HashMap<String, Double> output) {
        double highest = 0;
        String answer = "";

        for (Map.Entry<String, Double> entry : output.entrySet()) {
            Log.i(TAG, entry.getKey() + ": " + entry.getValue());
            if (entry.getValue() > highest) {
                highest = entry.getValue();
                answer = entry.getKey();
            }
        }
        return answer;
    }

    @OnClick(R.id.fab)
    protected void onSelectPhotoClick() {
        // build intent to search for content choosing apps
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // set the mime type to image to show only images
        intent.setType("image/*");
        // show gallery
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), PHOTO_RESULT_CODE);
    }

    private Runnable loadDataRunnable = new Runnable() {
        @Override
        public void run() {
            // open neural network
            InputStream is = getResources().openRawResource(R.raw.animal_network);

            // load neural network
            nnet = NeuralNetwork.load(is);
            Log.i(TAG, "run: neural network initialized: " + (nnet == null));
            Log.i(TAG, "run: input neurons count: " + nnet.getInputNeurons().length);

            // initialize ImageRecognitionPlugin
            imageRecognition = new ImageRecognitionPlugin(new Dimension(ORIGINAL_WIDTH, ORIGINAL_HEIGHT), ColorMode.COLOR_RGB);
            // connect plugin to network
            imageRecognition.setParentNetwork(nnet);

            Log.i(TAG, "run: image pixels count: " + SCALE_HEIGHT * SCALE_WIDTH * RGB_PIXELS_VALUE);
            Log.i(TAG, "run: imageRecognitionPlugin initialized: " + (imageRecognition == null));
            hideDialog();
        }
    };

}
