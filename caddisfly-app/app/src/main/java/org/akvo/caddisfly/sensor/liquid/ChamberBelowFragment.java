/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.sensor.liquid;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.model.TestInfo;

public class ChamberBelowFragment extends BaseRunTest implements RunTest {

    public ChamberBelowFragment() {
        // Required empty public constructor
    }

    public static ChamberBelowFragment newInstance(TestInfo param1, Calibration calibration) {
        ChamberBelowFragment fragment = new ChamberBelowFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, param1);
        args.putParcelable(ARG_PARAM2, calibration);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        startTest();
    }

    private void startTest() {
        if (!cameraStarted) {

            setupCamera();

            cameraStarted = true;

            cameraSwitcher.start();
        }
    }

    @Override
    protected void initializeTest() {
        super.initializeTest();
        binding.imageIllustration.setVisibility(View.GONE);
        binding.circleView.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding.cameraView.setOnClickListener(v -> takePicture());

        return binding.getRoot();
    }

    protected void takePicture() {
        binding.cameraView.setOnClickListener(null);
        sound.playShortResource(R.raw.beep);
        super.takePicture();
    }
}
