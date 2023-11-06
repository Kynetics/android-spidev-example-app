/*
 * Copyright (C)  2018-2023 Kynetics, LLC
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.kynetics.libspidevapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import com.kynetics.libspidev.SpidevException;
import com.kynetics.libspidev.SpidevFactory;
import com.kynetics.libspidev.SpidevInterface;

public class MainActivity extends AppCompatActivity {
    final String TAG = "MainActivity";
    private SpidevInterface dev;
    private int bus;
    private int chipselect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_title);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        showDeviceInitializationDialog();
    }

    private void showDeviceInitializationDialog() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog, null);
        final EditText busTxt = dialogView.findViewById(R.id.dialogBusInput);
        final EditText csTxt = dialogView.findViewById(R.id.dialogCsInput);

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("SPI device initialization")
                .setMessage("Specify SPI bus and chip select")
                .setCancelable(false)
                .setNeutralButton(R.string.menu_about, (dialog, which) -> {
                    openAboutActivity();
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /* do nothing, method overridden later */
                    }
                });

        final AlertDialog dialog = dialogBuilder.create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                int busNum;
                int csNum;
                try {
                    busNum = Integer.parseInt(busTxt.getText().toString());
                    csNum = Integer.parseInt(csTxt.getText().toString());
                } catch (NumberFormatException e) {
                    return;
                }

                bus = busNum;
                chipselect = csNum;

                try {
                    initSpiDevice();
                    dialog.dismiss();
                } catch (SpidevException e) {
                    Toast.makeText(getApplication().getBaseContext(),
                            String.format("Error opening /dev/spidev%d.%d", bus, chipselect),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openAboutActivity() {
        startActivity(new Intent(MainActivity.this, AboutActivity.class));
    }

    private int initSpiDevice() throws SpidevException {

        dev = SpidevFactory.newInstance(bus, chipselect);
        Log.d(TAG, String.format("*** Dumpstat: /dev/spidev%d.%d***", dev.getBus(), dev.getChipsel()));

        TextView tvDev = findViewById(R.id.devTxt);
        tvDev.setText(String.format("SPI device: spidev%d.%d", dev.getBus(), dev.getChipsel()));

        updateModeView();

        updateSpeedView();

        updateBpwView();

        updateBitJustView();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        return 0;
    }

    private void updateModeView() {
        SpidevInterface.spiMode ret;
        RadioButton chkButton = null;

        //Show current mode
        try {
            ret = dev.getMode();

            switch (ret) {
                case MODE_0:
                    chkButton = findViewById(R.id.modeButton0);
                    break;
                case MODE_1:
                    chkButton = findViewById(R.id.modeButton1);
                    break;
                case MODE_2:
                    chkButton = findViewById(R.id.modeButton2);
                    break;
                case MODE_3:
                    chkButton = findViewById(R.id.modeButton3);
                    break;
            }

            chkButton.setChecked(true);

        } catch (SpidevException e) {
            Log.e(TAG, "Error reading SPI mode");
        }

        RadioGroup modeGrp = findViewById(R.id.modeGroup);

        //Select user mode
        modeGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.modeButton0:
                        // Mode 0
                        Log.d(TAG, "User choice: mode 0");
                        try {
                            dev.setMode(SpidevInterface.spiMode.MODE_0);
                        } catch (SpidevException e) {
                            Log.e(TAG, "Error setting SPI mode");
                            Toast.makeText(getApplication().getBaseContext(),
                                    "ERROR", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.modeButton1:
                        // Mode 1
                        Log.d(TAG, "User choice: mode 1");
                        try {
                            dev.setMode(SpidevInterface.spiMode.MODE_1);
                        } catch (SpidevException e) {
                            Log.e(TAG, "Error setting SPI mode");
                            Toast.makeText(getApplication().getBaseContext(),
                                    "ERROR", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.modeButton2:
                        // Mode 2
                        Log.d(TAG, "User choice: mode 2");
                        try {
                            dev.setMode(SpidevInterface.spiMode.MODE_2);
                        } catch (SpidevException e) {
                            Log.e(TAG, "Error setting SPI mode");
                            Toast.makeText(getApplication().getBaseContext(),
                                    "ERROR", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.modeButton3:
                        // Mode 3
                        Log.d(TAG, "User choice: mode 3");
                        try {
                            dev.setMode(SpidevInterface.spiMode.MODE_3);
                        } catch (SpidevException e) {
                            Log.e(TAG, "Error setting SPI mode");
                            Toast.makeText(getApplication().getBaseContext(),
                                    "ERROR", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                Toast.makeText(getApplication().getBaseContext(),
                        "Mode set", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSpeedView() {
        int ret;
        final EditText speedEdit = findViewById(R.id.speedInput);
        final Button speedBtn = findViewById(R.id.speedBtn);

        //Show current speed
        try {
            ret = dev.getSpeedHz();
            speedEdit.setText(String.format("%d", ret));

        } catch (SpidevException e) {
            Log.e(TAG, "Error reading SPI speed");
        }

        //Set user speed
        speedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userSpeed = speedEdit.getText().toString();
                try {
                    dev.setSpeedHz(Integer.parseInt(userSpeed));
                    Toast.makeText(getApplication().getBaseContext(),
                            "Speed set", Toast.LENGTH_SHORT).show();
                } catch (SpidevException e) {
                    Log.e(TAG, "Error setting SPI speed");
                    Toast.makeText(getApplication().getBaseContext(),
                            "ERROR", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void updateBpwView() {
        int ret;
        final EditText bpwEdit = findViewById(R.id.bpwInput);
        final Button bpwBtn = findViewById(R.id.bpwBtn);

        //Show current BPW
        try {
            ret = dev.getBpw();
            bpwEdit.setText(String.format("%d", ret));
        } catch (SpidevException e) {
            Log.e(TAG, "Error reading SPI bpw");
        }

        //Set user BPW
        bpwBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userBpw = bpwEdit.getText().toString();
                try {
                    dev.setBpw(Integer.valueOf(userBpw));
                    Toast.makeText(getApplication().getBaseContext(),
                            "BPW set", Toast.LENGTH_SHORT).show();
                } catch (SpidevException e) {
                    Log.e(TAG, "Error setting BPW");
                    Toast.makeText(getApplication().getBaseContext(),
                            "ERROR", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateBitJustView() {
        SpidevInterface.spiBitJust ret;
        final RadioButton chkButton;
        final RadioGroup lsbGrp = findViewById(R.id.lsbGroup);

        //Show current bit justification
        try {
            ret = dev.getBitJustification();

            if (ret == SpidevInterface.spiBitJust.MSB_FIRST)
                chkButton = findViewById(R.id.msbFirst);
            else
                chkButton = findViewById(R.id.lsbFirst);

            chkButton.setChecked(true);

        } catch (SpidevException e) {
            Log.e(TAG, "Error reading SPI mode");
        }

        //Set user bit justification
        lsbGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                SpidevInterface.spiBitJust bitJust;

                if (checkedId == R.id.msbFirst)
                    bitJust = SpidevInterface.spiBitJust.MSB_FIRST;
                else
                    bitJust = SpidevInterface.spiBitJust.LSB_FIRST;

                try {
                    dev.setBitJustification(bitJust);
                    Toast.makeText(getApplication().getBaseContext(),
                            "Bit justification set", Toast.LENGTH_SHORT).show();
                } catch (SpidevException e) {
                    Log.e(TAG, "Error setting bit justification");
                    Toast.makeText(getApplication().getBaseContext(),
                            "ERROR", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_recreate) {
            recreate();
            return true;
        } else if (item.getItemId() == R.id.menu_about) {
            openAboutActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void onDestroy() {
        super.onDestroy();
        dev.close();
    }
}