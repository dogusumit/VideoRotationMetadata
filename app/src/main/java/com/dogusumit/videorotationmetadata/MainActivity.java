package com.dogusumit.videorotationmetadata;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    final Context context = this;

    static final int REQUEST_KODUM = 1123;

    ImageView imgv1;
    TextView tv1;
    Spinner spn1;
    Button btn1, btn2;
    FFmpeg ffmpeg;
    String param, name;
    String girdiYolu, ciktiDosya, ciktiKlasor;
    int angle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        spn1 = findViewById(R.id.spinner1);
        btn1 = findViewById(R.id.button1);
        btn2 = findViewById(R.id.button2);
        imgv1 = findViewById(R.id.imageview1);
        tv1 = findViewById(R.id.textview1);
        ffmpeg = FFmpeg.getInstance(context);
        param = "";
        ciktiDosya = "";
        girdiYolu = "";
        ciktiKlasor = "";
        angle = 0;


        isStoragePermissionGranted();

        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                }

                @Override
                public void onFailure() {
                }

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegNotSupportedException e) {
            toastla(e.getMessage());
        }

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, REQUEST_KODUM);
                } catch (Exception e) {
                    toastla(e.getMessage());
                }
            }
        });

        spn1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinnerSelected(0);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (ffmpeg.isFFmpegCommandRunning()) {
                        if (ffmpeg.killRunningProcesses()) {
                            btn2.setText(R.string.str2);
                            tv1.setText(R.string.str7);
                        }
                    } else {
                        File a = new File(girdiYolu);
                        String s1 = a.getAbsoluteFile().getName();
                        ciktiKlasor = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getString(R.string.dir);
                        String s2 = s1.substring(s1.lastIndexOf("."));
                        String s3 = s1.substring(0, s1.lastIndexOf("."));
                        ciktiDosya = s3 + "_" + name + s2;

                        File folder = new File(ciktiKlasor);
                        if (folder.exists() || folder.mkdir()) {

                            //tv1.setText(ciktiDosya);

                            ArrayList<String> cmdArray = new ArrayList<>();
                            cmdArray.add("-y");
                            cmdArray.add("-i");
                            cmdArray.add(girdiYolu);
                            cmdArray.add("-c");
                            cmdArray.add("copy");
                            cmdArray.add("-metadata:s:v:0");
                            cmdArray.add(param);
                            cmdArray.add(ciktiKlasor + "/" + ciktiDosya);

                            //tv1.setText(cmdArray.toString());

                            ffmpegCalistir(cmdArray.toArray(new String[cmdArray.size()]));
                        } else {
                            toastla(getString(R.string.str10));
                        }
                    }
                } catch (Exception e) {
                    toastla(e.getLocalizedMessage());
                }

            }
        });

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_KODUM) {
                String selectedVideoPath;
                selectedVideoPath = uritoPath(data.getData());
                try {
                    if (selectedVideoPath == null) {
                        toastla(getString(R.string.str9));
                    } else {
                        girdiYolu = selectedVideoPath;
                        videoYukle(selectedVideoPath);
                        //tv1.setText(girdiYolu);
                        param = "";
                        imgv1.setRotation(0);
                        imgv1.setScaleX(1f);
                        imgv1.setScaleY(1f);
                        name = "";
                    }
                } catch (Exception e) {
                    toastla(e.getMessage());
                }
            }
        }
    }

    public void spinnerSelected(int position) {
        try {
            switch (position) {
                case 0:
                    imgv1.setRotation(0 - angle);
                    imgv1.setScaleX(1f);
                    imgv1.setScaleY(1f);
                    name = "0";
                    param = "rotate=0";
                    break;
                case 1:
                    imgv1.setRotation(90 - angle);
                    imgv1.setScaleX(1f);
                    imgv1.setScaleY(1f);
                    name = "90";
                    param = "rotate=90";
                    break;
                case 2:
                    imgv1.setRotation(180 - angle);
                    imgv1.setScaleX(1f);
                    imgv1.setScaleY(1f);
                    name = "180";
                    param = "rotate=180";
                    break;
                case 3:
                    imgv1.setRotation(270 - angle);
                    imgv1.setScaleX(1f);
                    imgv1.setScaleY(1f);
                    name = "270";
                    param = "rotate=270";
                    break;
                default:
                    imgv1.setRotation(0 - angle);
                    imgv1.setScaleX(1f);
                    imgv1.setScaleY(1f);
                    name = "0";
                    param = "rotate=0";
                    break;
            }
        } catch (Exception e) {
            toastla(e.getLocalizedMessage());
        }
    }

    public void isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    public String uritoPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }

    void toastla(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    void videoYukle(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            File file = new File(path);
            if (!file.exists()) {
                toastla(getString(R.string.str10));
                return;
            }
            retriever.setDataSource(file.getAbsolutePath());

            imgv1.setImageBitmap(retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST));

            String s1 = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            String s2 = getString(R.string.str11) + " " + s1 + "\n\n" + path;
            tv1.setText(s2);

            if (s1 != null)
                angle = Integer.parseInt(s1);
            spinnerSelected(angle / 90);
            spn1.setSelection(angle / 90);

        } catch (Exception e) {
            toastla(e.getMessage());
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                toastla(ex.getMessage());
            }
        }
    }

    void ffmpegCalistir(String[] cmd) {
        try {
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    tv1.setText(getString(R.string.str8));
                    btn2.setText(getString(R.string.str4));
                }

                @Override
                public void onProgress(String message) {
                    String s = getString(R.string.str8) + "\n" + message;
                    tv1.setText(s);
                }

                @Override
                public void onFailure(String message) {
                    String s = getString(R.string.str6) + "\n" + message;
                    tv1.setText(s);
                }

                @Override
                public void onSuccess(String message) {
                    try {
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(new File(ciktiKlasor + "/" + ciktiDosya));
                        mediaScanIntent.setData(contentUri);
                        context.sendBroadcast(mediaScanIntent);
                        tv1.setText(R.string.str5);

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(getString(R.string.str5));
                        builder.setMessage(getString(R.string.str12));
                        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("content://media/internal/images/media"));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(intent);
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } catch (Exception e) {
                        toastla(e.getLocalizedMessage());
                    }
                }

                @Override
                public void onFinish() {
                    btn2.setText(R.string.str2);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            toastla(e.getMessage());
        }
    }

    private void uygulamayiOyla() {
        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
            } catch (Exception ane) {
                toastla(e.getMessage());
            }
        }
    }

    private void marketiAc() {
        try {
            Uri uri = Uri.parse("market://developer?id=dogusumit");
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/developer?id=dogusumit")));
            } catch (Exception ane) {
                toastla(e.getMessage());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.oyla:
                uygulamayiOyla();
                return true;
            case R.id.market:
                marketiAc();
                return true;
            case R.id.cikis:
                System.exit(0);
                android.os.Process.killProcess(android.os.Process.myPid());
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}