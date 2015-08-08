package org.akvo.akvoqr;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ResultStripTestActivity extends AppCompatActivity {

    public static List<TestResult> testResults = new ArrayList<>();
    private int numSuccess = 0;
    private int numPatchesExpected = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_strip_test);

        LinearLayout layout = (LinearLayout) findViewById(R.id.resultStripTestLinearLayout);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        TextView textView = new TextView(this);
        layout.addView(textView, params);

        for(int i=0;i<testResults.size();i++)
        {
            LinearLayout innerlayout = new LinearLayout(this);
            innerlayout.setOrientation(LinearLayout.HORIZONTAL);

            ImageView imageView = new ImageView(this);
            if(testResults.get(i).numPatchesFound == numPatchesExpected)
            {
                imageView.setBackgroundColor(Color.GREEN);
                numSuccess ++;
            }
            else
            {
                imageView.setBackgroundColor(Color.RED);
            }

            imageView.setImageBitmap(testResults.get(i).calibrated);
            imageView.setPadding(5, 5, 5, 5);
            innerlayout.addView(imageView, params);

            TextView minChromaView = new TextView(this);
            minChromaView.setBackgroundColor(testResults.get(i).minChromaColor);
            minChromaView.setText("min chroma");
            innerlayout.addView(minChromaView, params);

            layout.addView(innerlayout, params);
        }

        textView.setText("Results succesfull: " + numSuccess + " out of " + CameraActivity.MAX_ITER );

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_result_strip_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
