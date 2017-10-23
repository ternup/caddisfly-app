package org.akvo.caddisfly.viewmodel;


import android.content.Context;
import android.content.res.AssetManager;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.sensor.cbt.RowView;
import org.akvo.caddisfly.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestInfoViewModel {

    public static final String BRAND_IMAGE_PATH = "images/brand/";
    private static final String ILLUSTRATION_PATH = "images/instructions/";
    private static TestInfo testInfo;
    public final ObservableField<TestInfo> test = new ObservableField<>();

    @BindingAdapter("content")
    public static void setContent(LinearLayout linearLayout, JSONObject instruction) {

        Context context = linearLayout.getContext();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        Point size = new Point();
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getRealSize(size);
        }

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        try {
            JSONArray jsonArray = instruction.getJSONArray("section");
            for (int i = 0; i < jsonArray.length(); i++) {
                String text = jsonArray.getString(i);
                if (text.contains("include:incubation_table")) {

                    LayoutInflater inflater = (LayoutInflater) linearLayout.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view;
                    if (inflater != null) {
                        view = inflater.inflate(R.layout.incubation_table, linearLayout, false);
                        linearLayout.addView(view);
                    }

                } else if (text.contains("image:")) {


                    String imageName = text.substring(text.indexOf(":") + 1, text.length());

                    int resourceId = context.getResources().getIdentifier("drawable/in_" + imageName,
                            "id", "org.akvo.caddisfly");

                    if (resourceId > 0) {

                        double divisor = 3;
                        if (displayMetrics.densityDpi > 250) {
                            divisor = 2.4;
                        }

                        if (size.y > displayMetrics.heightPixels) {
                            divisor += 0.3;
                        }

                        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                (int) (displayMetrics.heightPixels / divisor));

                        llp.setMargins(0, 0, 0, 20);

                        final AppCompatImageView imageView = new AppCompatImageView(context);
                        imageView.setImageResource(resourceId);
                        imageView.setLayoutParams(llp);
                        imageView.setContentDescription(imageName);

                        linearLayout.addView(imageView);

                    } else {

                        String image = ILLUSTRATION_PATH + imageName + ".webp";

                        InputStream ims = null;
                        try {
                            ims = context.getAssets().open(image);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (ims != null) {

                            ImageView imageView = new ImageView(linearLayout.getContext());
                            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                            imageView.setImageDrawable(Drawable.createFromStream(ims, null));

                            double divisor = 3.1;
                            if (displayMetrics.densityDpi > 250) {
                                divisor = 2.7;
                            }

                            if (size.y > displayMetrics.heightPixels) {
                                divisor += 0.3;
                            }

                            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    (int) (displayMetrics.heightPixels / divisor));

                            llp.setMargins(0, 0, 0, 20);
                            imageView.setLayoutParams(llp);

                            imageView.setContentDescription(imageName);

                            // set an id for the view to be able to find it for unit testing
                            imageView.setId(i);

                            linearLayout.addView(imageView);
                        }
                    }


                } else {

                    RowView rowView = new RowView(context);

                    TextView textView = new TextView(linearLayout.getContext());

                    if (displayMetrics.densityDpi > 250) {
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                    } else {
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    }

                    Matcher m1 = Pattern.compile("^(\\d+?\\.\\s*)(.*)").matcher(text);
                    if (m1.find()) {
                        rowView.setNumber(m1.group(1).trim());
                        text = m1.group(2).trim();
                    }

                    String[] sentences = (text + ". ").split("\\.\\s+");

                    for (int j = 0; j < sentences.length; j++) {
                        if (j > 0) {
                            rowView.append(new SpannableString(" "));
                        }
                        rowView.append(StringUtil.toInstruction((AppCompatActivity) context, testInfo, sentences[j].trim()));
                    }

                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);

                    llp.setMargins(0, 0, 0, 20);
                    textView.setLayoutParams(llp);

                    // set an id for the view to be able to find it for unit testing
                    rowView.setId(i);

                    linearLayout.addView(rowView);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter("imageScale")
    public static void setImageScale(ImageView imageView, String scaleType) {
        if (scaleType != null) {
            imageView.setScaleType(scaleType.equals("centerCrop")
                    ? ImageView.ScaleType.CENTER_CROP : ImageView.ScaleType.FIT_CENTER);
        }
    }

    @BindingAdapter("imageUrl")
    public static void setImageUrl(ImageView imageView, String name) {
        setImage(imageView, BRAND_IMAGE_PATH + name + ".webp");
    }

    private static void setImage(ImageView imageView, String name) {
        if (name != null) {
            Context context = imageView.getContext();
            try {
                name = name.replace(" ", "-");
                if (name.contains(".xml")) {
                    //final XmlResourceParser parser = context.getAssets().openXmlResourceParser(name);
                    //Drawable drawable = VectorDrawableCompat.createFromXml(context.getResources(), parser);

                    AssetManager assManager = context.getAssets();
                    InputStream is = assManager.open(name);
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();
                    xpp.setInput(is, null);
                    Drawable drawable = VectorDrawableCompat.createFromXml(context.getResources(), xpp);

                    imageView.setImageDrawable(drawable);
                } else {
                    InputStream ims = context.getAssets().open(name);
                    imageView.setImageDrawable(Drawable.createFromStream(ims, null));
                    //imageView.setBackground(Drawable.createFromStream(ims, null));
                }

            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
        }
    }

    public void setTest(TestInfo testInfo) {
        this.test.set(testInfo);
        TestInfoViewModel.testInfo = testInfo;
    }

}
