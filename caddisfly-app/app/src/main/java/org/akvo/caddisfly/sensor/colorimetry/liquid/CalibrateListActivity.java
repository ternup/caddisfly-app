/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.ui.BaseActivity;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.ColorUtil;
import org.akvo.caddisfly.util.FileUtil;
import org.akvo.caddisfly.util.PreferencesUtil;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * An activity representing a list of Calibrate items.
 * <p/>
 * This activity also implements the required
 * {@link CalibrateListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class CalibrateListActivity extends BaseActivity
        implements CalibrateListFragment.Callbacks,
        SaveCalibrationDialogFragment.CalibrationDetailsSavedListener {

    private final int REQUEST_CALIBRATE = 100;
    private FloatingActionButton fabEditCalibration;
    private TextView textSubtitle;
    private TextView textSubtitle1;
    private TextView textSubtitle2;
    private int mPosition;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (AppPreferences.isDiagnosticMode()) {
            getMenuInflater().inflate(R.menu.menu_calibrate_dev, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionSwatches:
                final Intent intent = new Intent(this, DiagnosticSwatchActivity.class);
                startActivity(intent);
                return true;
            case R.id.menuLoad:
                Handler.Callback callback = new Handler.Callback() {
                    public boolean handleMessage(Message msg) {
                        CalibrateListFragment fragment = (CalibrateListFragment)
                                getSupportFragmentManager()
                                        .findFragmentById(R.id.fragmentCalibrateList);
                        fragment.setAdapter();
                        return true;
                    }
                };
                loadCalibration(callback);
                return true;
            case R.id.menuSave:
                showEditCalibrationDetailsDialog();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate_list);

        textSubtitle = (TextView) findViewById(R.id.textSubtitle);
        textSubtitle1 = (TextView) findViewById(R.id.textSubtitle1);
        textSubtitle2 = (TextView) findViewById(R.id.textSubtitle2);

        ((TextView) findViewById(R.id.textTitle)).setText(CaddisflyApp.getApp().
                getCurrentTestInfo().getName(getResources().getConfiguration().locale.getLanguage()));

        fabEditCalibration =
                (FloatingActionButton) findViewById(R.id.fabEditCalibration);

        if (AppPreferences.isDiagnosticMode()) {
            fabEditCalibration.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.cyan)));
        }

        fabEditCalibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditCalibrationDetailsDialog();
            }
        });

        loadDetails();
    }

    private void showEditCalibrationDetailsDialog() {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SaveCalibrationDialogFragment saveCalibrationDialogFragment = SaveCalibrationDialogFragment.newInstance();
        saveCalibrationDialogFragment.show(ft, "saveCalibrationDialog");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(R.string.calibrate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fabEditCalibration.setEnabled(true);
    }

    private void loadDetails() {

        String testCode = CaddisflyApp.getApp().getCurrentTestInfo().getCode();

        long calibrationDate = PreferencesUtil.getLong(this, testCode, R.string.calibrationDateKey);

        if (calibrationDate >= 0) {
            textSubtitle1.setText(new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.US).format(new Date(calibrationDate)));
        }

        Long expiryDate = PreferencesUtil.getLong(this, testCode, R.string.calibrationExpiryDateKey);

        if (expiryDate >= 0) {
            textSubtitle2.setText(String.format("%s: %s", getString(R.string.expires),
                    new SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(new Date(expiryDate))));
        }

        textSubtitle.setText(PreferencesUtil.getString(this, testCode, R.string.batchNumberKey, ""));

        CalibrateListFragment calibrateListFragment = (CalibrateListFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragmentCalibrateList);

        if (calibrateListFragment != null) {
            calibrateListFragment.refresh();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_back_out, R.anim.slide_back_in);
    }

    /**
     * Callback method from {@link CalibrateListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(int id) {

        TestInfo currentTestInfo = CaddisflyApp.getApp().getCurrentTestInfo();

//        String key = String.format("%s_%s", currentTestInfo.getCode(), getString(R.string.calibrationDateKey));
//        long calibrationDate = PreferencesUtil.getLong(this, key);
        //Show calibration details dialog if this is an incomplete calibration from more than an hour ago
//        Calendar currentDate = Calendar.getInstance();
//        if (currentDate.getTimeInMillis() - calibrationDate > 1000 * 60 * 1) {
//            key = String.format("%s_%s", currentTestInfo.getCode(), getString(R.string.batchNumberKey));
//            PreferencesUtil.removeKey(this, key);
//            key = String.format("%s_%s", currentTestInfo.getCode(), getString(R.string.calibrationExpiryDateKey));
//            PreferencesUtil.removeKey(this, key);
//            showEditCalibrationDetailsDialog();
//            return;
//        }

        fabEditCalibration.setEnabled(false);
        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                fabEditCalibration.setEnabled(true);
            }
        }, 500);

        //Show edit calibration details dialog if required
        Long expiryDate = PreferencesUtil.getLong(this, currentTestInfo.getCode(), R.string.calibrationExpiryDateKey);
        if (expiryDate < Calendar.getInstance().getTimeInMillis()) {
            showEditCalibrationDetailsDialog();
            return;
        }

        mPosition = id;
        Swatch swatch = currentTestInfo.getSwatch(mPosition);

        if (!ApiUtil.isCameraInUse(this, null)) {
            final Intent intent = new Intent(getIntent());
            intent.setClass(getBaseContext(), AlignmentActivity.class);
            intent.putExtra("isCalibration", true);
            intent.putExtra("swatchValue", swatch.getValue());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivityForResult(intent, REQUEST_CALIBRATE);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CALIBRATE:

                Swatch swatch = CaddisflyApp.getApp().getCurrentTestInfo().getSwatch(mPosition);

                long calibratedDate = PreferencesUtil.getLong(this,
                        CaddisflyApp.getApp().getCurrentTestInfo().getCode(),
                        R.string.calibrationDateKey);

                if (resultCode == Activity.RESULT_OK) {
                    saveCalibratedData(swatch, data.getIntExtra("color", 0));

                    //Save date if this is the first swatch calibrated
                    if (calibratedDate < 0 || SwatchHelper.getCalibratedSwatchCount(
                            CaddisflyApp.getApp().getCurrentTestInfo().getSwatches()) == 1) {
                        PreferencesUtil.setLong(this, CaddisflyApp.getApp().getCurrentTestInfo().getCode(),
                                R.string.calibrationDateKey, Calendar.getInstance().getTimeInMillis());
                        loadDetails();
                    }

                    ((CalibrateListFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.fragmentCalibrateList)).setAdapter();
                }

                break;
        }
    }

    /**
     * Save a single calibrated color
     *
     * @param swatch      The swatch object
     * @param resultColor The color value
     */
    private void saveCalibratedData(Swatch swatch, final int resultColor) {
        String colorKey = String.format(Locale.US, "%s-%.2f",
                CaddisflyApp.getApp().getCurrentTestInfo().getCode(), swatch.getValue());

        if (resultColor == 0) {
            PreferencesUtil.removeKey(getApplicationContext(), colorKey);
        } else {
            swatch.setColor(resultColor);
            PreferencesUtil.setInt(getApplicationContext(), colorKey, resultColor);
        }
    }

    /**
     * Convert a string number into a double value
     *
     * @param text the text to be converted to number
     * @return the double value
     */
    private double stringToDouble(String text) {

        text = text.replaceAll(",", ".");
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        try {
            return nf.parse(text).doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * Save a list of calibrated colors
     *
     * @param swatches List of swatch colors to be saved
     */
    private void saveCalibratedSwatches(ArrayList<Swatch> swatches) {

        TestInfo currentTestInfo = CaddisflyApp.getApp().getCurrentTestInfo();
        for (Swatch swatch : swatches) {
            String key = String.format(Locale.US, "%s-%.2f",
                    currentTestInfo.getCode(), swatch.getValue());

            PreferencesUtil.setInt(this, key, swatch.getColor());
        }

        CaddisflyApp.getApp().loadCalibratedSwatches(currentTestInfo);
    }

    /**
     * Load the calibrated swatches from the calibration text file
     *
     * @param callback callback to be initiated once the loading is complete
     */
    private void loadCalibration(final Handler.Callback callback) {
        final Context context = this;
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.loadCalibration);

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context,
                    R.layout.row_text);

            final File path = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION,
                    CaddisflyApp.getApp().getCurrentTestInfo().getCode());

            File[] listFilesTemp = null;
            if (path.exists() && path.isDirectory()) {
                listFilesTemp = path.listFiles();
            }

            final File[] listFiles = listFilesTemp;
            if (listFiles != null && listFiles.length > 0) {
                Arrays.sort(listFiles);

                for (File listFile : listFiles) {
                    arrayAdapter.add(listFile.getName());
                }

                builder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }
                );

                builder.setAdapter(arrayAdapter,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String fileName = listFiles[which].getName();
                                final ArrayList<Swatch> swatchList = new ArrayList<>();

                                ArrayList<String> calibrationDetails;
                                try {
                                    calibrationDetails = FileUtil.loadFromFile(path, fileName);

                                    if (calibrationDetails != null) {

                                        for (int i = calibrationDetails.size() - 1; i >= 0; i--) {
                                            if (!calibrationDetails.get(i).contains("=")) {
                                                calibrationDetails.remove(i);
                                            }
                                        }

                                        for (String rgb : calibrationDetails) {
                                            String[] values = rgb.split("=");
                                            Swatch swatch = new Swatch(stringToDouble(values[0]),
                                                    ColorUtil.getColorFromRgb(values[1]), Color.TRANSPARENT);
                                            swatchList.add(swatch);
                                        }

                                        if (swatchList.size() > 0) {
                                            Toast.makeText(getBaseContext(),
                                                    String.format(getString(R.string.calibrationLoaded), fileName),
                                                    Toast.LENGTH_SHORT).show();

                                            (new AsyncTask<Void, Void, Void>() {
                                                @Override
                                                protected Void doInBackground(Void... params) {
                                                    saveCalibratedSwatches(swatchList);
                                                    return null;
                                                }

                                                @Override
                                                protected void onPostExecute(Void result) {
                                                    super.onPostExecute(result);
                                                    callback.handleMessage(null);
                                                }
                                            }).execute();
                                        } else {
                                            throw new Exception();
                                        }
                                    }
                                } catch (Exception ex) {
                                    AlertUtil.showError(context, R.string.error, getString(R.string.errorLoadingFile),
                                            null, R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }, null, null);

                                }
                            }
                        }
                );

                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        final ListView listView = alertDialog.getListView();
                        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                                final int position = i;

                                AlertUtil.askQuestion(context, R.string.delete,
                                        R.string.deleteConfirm, R.string.delete, R.string.cancel, true,
                                        new DialogInterface.OnClickListener() {
                                            @SuppressWarnings("unchecked")
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String fileName = listFiles[position].getName();
                                                FileUtil.deleteFile(path, fileName);
                                                ArrayAdapter listAdapter = (ArrayAdapter) listView.getAdapter();
                                                listAdapter.remove(listAdapter.getItem(position));
                                                alertDialog.dismiss();
                                                Toast.makeText(getBaseContext(), R.string.deleted, Toast.LENGTH_SHORT).show();
                                            }
                                        }, null);
                                return true;
                            }
                        });

                    }
                });
                alertDialog.show();
            } else {
                AlertUtil.showMessage(context, R.string.notFound, R.string.loadFilesNotAvailable);
            }
        } catch (ActivityNotFoundException ignored) {
        }

        callback.handleMessage(null);
    }

    @Override
    public void onCalibrationDetailsSaved() {
        loadDetails();
    }
}
