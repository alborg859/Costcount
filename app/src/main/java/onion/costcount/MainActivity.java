package onion.costcount;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
//import android.support.design.widget.NavigationView;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.view.GravityCompat;
//import android.support.v4.widget.DrawerLayout;
//import android.support.v7.app.AppCompatActivity;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.elevation.ElevationOverlayProvider;
import com.google.android.material.navigation.NavigationView;


import java.io.IOException;
import java.util.Random;
import java.util.zip.Inflater;

//import android.support.annotation.NonNull;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    SurfaceView camView;
    TextView priceTXT;
    TextView CostTV;
    CameraSource cameraSource;
    final int RequestCameraPermissionID = 1001;
    Button captureBTN;
    ScaleAnimation scaleAnimation;
    ScaleAnimation unscaleAnimation;
    ScaleAnimation costUpdate;
    public BoxDetector boxDetector;
    ImageButton addButton;
    DatabaseHelper databaseHelper;

    ImageButton menuButton;
    DrawerLayout drawerLayout;
    ConstraintLayout constraintLayout;
    NavigationView navigationView;
    boolean isOpen = false;
    CartDialog cartDialog;
    CurrencyDialog currencyDialog;

    TextView currentCurrencyTV;
    TextView foreignCurrencyTV;
    TextView foreignCostTV;

    //inflating
    ImageButton keyboardButton;
    LayoutInflater inflater;
    EditText priceHandInput;
    View inputFieldView;

    ImageButton handAdd;
    EditText handInput;
    ImageView refocuser;

    AlphaAnimation inputAppear, inputDisappear;

    SharedPreferences sharedPreferences;
String adId = "";

    private InterstitialAd interstitialAd;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    try {
                        cameraSource.start(camView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public boolean isNumber(String priceString) {

        if (priceString == null) return false;
        try {
            float f = Float.parseFloat(priceString);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        String curCur = sharedPreferences.getString("currentCurrency", "USD");

        constraintLayout = findViewById(R.id.CL);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-9541882283543234/8927939853");
        interstitialAd.loadAd(new AdRequest.Builder().build());


        keyboardButton = findViewById(R.id.keyboardButton);
        keyboardButton.setOnClickListener(new View.OnClickListener() {



            @Override
            public void onClick(View v) {
            captureBTN.setEnabled(false);
            menuButton.setEnabled(false);
                priceTXT.setText("");
                inputAppear = new AlphaAnimation(0f, 1f);
                inputAppear.setDuration(200);
                inputAppear.setFillAfter(true);


                inputDisappear = new AlphaAnimation(1f, 0f);
                inputDisappear.setDuration(200);
                inputDisappear.setFillAfter(true);
                inputDisappear.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {


                        inputFieldView.clearFocus();
                        constraintLayout.removeView(inputFieldView);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });


                inflater = LayoutInflater.from(getApplicationContext());
                inputFieldView = inflater.inflate(R.layout.inflated_input, drawerLayout, false);
                constraintLayout.addView(inputFieldView);
                inputFieldView.setAnimation(inputAppear);

                handAdd = inputFieldView.findViewById(R.id.inputButton);
                handInput = inputFieldView.findViewById(R.id.inputText);
                refocuser = inputFieldView.findViewById(R.id.refocuser);

                handAdd.setEnabled(false);

                handInput.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (handInput.getText().toString().length() > 0) {
                            handAdd.setEnabled(true);
                        } else {
                            handAdd.setEnabled(false);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                handAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        float price = Float.parseFloat(handInput.getText().toString());
                        double p = Math.round(price * 100.0) / 100.0;
                        price = (float) p;

                        databaseHelper.addData(price);
                        handInput.setText("");

                        if(price > 0) {

                        float cost = databaseHelper.getCost();
                        CostTV.setText((String.format("%.2f", cost)));
                        float multiplier = sharedPreferences.getFloat("multiplier", 1);
                        foreignCostTV.setText((String.format("%.2f", cost * multiplier))); } else {
                            Toast.makeText(getApplicationContext(),"Invalid input", Toast.LENGTH_SHORT).show();
                        }


                    }
                });

                refocuser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        inputFieldView.setBackgroundColor(Color.TRANSPARENT);
                        inputFieldView.setAnimation(inputDisappear);
                        priceTXT.setText("");
                        captureBTN.setEnabled(true);
                        menuButton.setEnabled(true);
                    }
                });


            }
        });


        camView = findViewById(R.id.surface_view);
        priceTXT = findViewById(R.id.priceTxt);
        captureBTN = findViewById(R.id.captureBTN);
        addButton = findViewById(R.id.addButton);
        addButton.setVisibility(View.INVISIBLE);
        CostTV = findViewById(R.id.costTV);
        menuButton = findViewById(R.id.menuBTN);
        navigationView = findViewById(R.id.navigationView);
        drawerLayout = findViewById(R.id.drawerLayout);
        cartDialog = new CartDialog();
        currencyDialog = new CurrencyDialog();

        foreignCostTV = findViewById(R.id.foreignCostTV);
        foreignCurrencyTV = findViewById(R.id.foreignCurrencyTV);
        currentCurrencyTV = findViewById(R.id.currencyTV);

        navigationView.setNavigationItemSelectedListener(this);
        float cost = databaseHelper.getCost();
        CostTV.setText((String.format("%.2f", cost)));

        CostTV.setShadowLayer(1,0,0,Color.parseColor("#000000"));
        currentCurrencyTV.setShadowLayer(1,0,0,Color.parseColor("#000000"));
        foreignCostTV.setShadowLayer(1,0,0,Color.parseColor("#000000"));
        foreignCurrencyTV.setShadowLayer(1,0,0,Color.parseColor("#000000"));


        switch (curCur) {
            case "USD":
                currentCurrencyTV.setText("$");
                break;
            case "EUR":
                currentCurrencyTV.setText("€");
                break;
            case "GBP":
                currentCurrencyTV.setText("£");
                break;
            case "PLN":
                currentCurrencyTV.setText("zł");
                break;
            case "CHF":
                currentCurrencyTV.setText("fr");
                break;

            default:
                break;

        }


        String forCur = sharedPreferences.getString("foreignCurrency", "USD");

        switch (forCur) {
            case "USD":
                foreignCurrencyTV.setText("$");
                break;
            case "EUR":
                foreignCurrencyTV.setText("€");
                break;
            case "GBP":
                foreignCurrencyTV.setText("£");
                break;
            case "PLN":
                foreignCurrencyTV.setText("zł");
                break;
            case "CHF":
                foreignCurrencyTV.setText("fr");
                break;

            default:
                break;

        }

        if (sharedPreferences.getString("currentCurrency", "USD").equals(sharedPreferences.getString("foreignCurrency", "USD"))) {
            foreignCurrencyTV.setVisibility(View.INVISIBLE);
            foreignCostTV.setVisibility(View.INVISIBLE);
        } else {
            foreignCurrencyTV.setVisibility(View.VISIBLE);
            foreignCostTV.setVisibility(View.VISIBLE);
            float multiplier = sharedPreferences.getFloat("multiplier", 1);

            foreignCostTV.setText((String.format("%.2f", cost * multiplier)));

        }


        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (isNumber(priceTXT.getText().toString()) && Float.parseFloat(priceTXT.getText().toString()) > 0) {
                    databaseHelper.addData(Float.parseFloat(priceTXT.getText().toString()));
                    float cost = databaseHelper.getCost();

                    CostTV.setText((String.format("%.2f", cost)));
                    String cs = CostTV.getText().toString();


                    float multiplier = sharedPreferences.getFloat("multiplier", 1);
                    foreignCostTV.setText((String.format("%.2f", Float.parseFloat(cs)*multiplier )));

                    costUpdate = new ScaleAnimation(1f, 1.3f, 1f, 1.3f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    costUpdate.setDuration(100);
                    costUpdate.setFillAfter(false);
                    CostTV.setAnimation(costUpdate);

                } else {
                    Toast.makeText(getApplicationContext(), "Invalid number", Toast.LENGTH_SHORT).show();
                }


            }
        });

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        boxDetector = new BoxDetector(textRecognizer, 480, 240);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

                //TODO: maybe performance can be improved with this trick
                //cameraSource.stop();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

                Random r = new Random();
                int i = r.nextInt(100);

                if(interstitialAd.isLoaded() && i < 19 ) {
                    interstitialAd.show(); }


/*
                try {

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.CAMERA}, RequestCameraPermissionID);

                        return;
                    }
                    cameraSource.start(camView.getHolder());





                } catch (IOException e) {
                    e.printStackTrace();
                }
//TODO: maybe performance can be improved with this trick

*/


                float cost = databaseHelper.getCost();
        CostTV.setText((String.format("%.2f", cost)));

        if(sharedPreferences.getString("currentCurrency","USD").equals(sharedPreferences.getString("foreignCurrency","USD"))) {
            foreignCurrencyTV.setVisibility(View.INVISIBLE);
            foreignCostTV.setVisibility(View.INVISIBLE);
        } else {
            foreignCurrencyTV.setVisibility(View.VISIBLE);
            foreignCostTV.setVisibility(View.VISIBLE);

            float multiplier = sharedPreferences.getFloat("multiplier", 1);
            foreignCostTV.setText((String.format("%.2f", cost*multiplier)));

        }

        String curCur = sharedPreferences.getString("currentCurrency", "USD");

        switch (curCur){
            case "USD":  currentCurrencyTV.setText("$"); break;
            case "EUR": currentCurrencyTV.setText("€"); break;
            case "GBP": currentCurrencyTV.setText("£"); break;
            case "PLN": currentCurrencyTV.setText("zł"); break;
            case "CHF": currentCurrencyTV.setText("fr"); break;

            default: break;

        }

        String forCur = sharedPreferences.getString("foreignCurrency", "USD");

        switch (forCur){
            case "USD":  foreignCurrencyTV.setText("$"); break;
            case "EUR": foreignCurrencyTV.setText("€"); break;
            case "GBP": foreignCurrencyTV.setText("£"); break;
            case "PLN": foreignCurrencyTV.setText("zł"); break;
            case "CHF": foreignCurrencyTV.setText("fr"); break;

            default: break;

        }



        //foreign currency


    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }
});

        if (!boxDetector.isOperational()) {

        } else {



            cameraSource = new CameraSource.Builder(getApplicationContext(), boxDetector)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(height, width)

                    .setRequestedFps(30.0f)
                    .setAutoFocusEnabled(true)
                    .build();


            camView.getHolder().addCallback(new SurfaceHolder.Callback() {


                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA}, RequestCameraPermissionID);

                            return;
                        }
                        cameraSource.start(camView.getHolder());





                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
                }


            });

        boxDetector.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {

            }




            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {
                final SparseArray<TextBlock> items = detections.getDetectedItems();



                captureBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        if(items.size() != 0) {
                            priceTXT.post(new Runnable() {
                                @Override
                                public void run() {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    // for (int i = 0; i<items.size(); i++) {
                                    TextBlock item = items.valueAt(items.size()-1);

                                    stringBuilder.append(item.getValue().replaceAll("[^\\d,.]", "").replaceAll(",","."));
                                    stringBuilder.append("\n");
                                    //}

                                    scaleAnimation = new ScaleAnimation(0.1f,1f,0.1f,1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                    scaleAnimation.setDuration(100);
                                    scaleAnimation.setFillAfter(false);

                                    unscaleAnimation = new ScaleAnimation(0.1f,1f,0.1f,1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                    unscaleAnimation.setDuration(100);
                                    unscaleAnimation.setFillAfter(false);
                                    unscaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                                        @Override
                                        public void onAnimationStart(Animation animation) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            addButton.setVisibility(View.INVISIBLE);
                                        }

                                        @Override
                                        public void onAnimationRepeat(Animation animation) {

                                        }
                                    });

                                    int col = boxDetector.getDominantColor();
                                    if(col * (-1) > 5000000) {
                                    priceTXT.setTextColor(Color.parseColor("#FFFFFF")); } else {
                                        priceTXT.setTextColor(Color.parseColor("#000000"));
                                    }




                                    if (isNumber(stringBuilder.toString())) {
                                        priceTXT.setAnimation(scaleAnimation);

                                        priceTXT.setText(String.format("%.2f",Float.parseFloat(stringBuilder.toString())));
                                        addButton.setVisibility(View.VISIBLE);
                                        addButton.setAnimation(scaleAnimation);
                                    } else  {
                                        addButton.setAnimation(unscaleAnimation);
                                        priceTXT.setText("");
                                    }







                                }



                            });
                        } else {
                            scaleAnimation = new ScaleAnimation(0.1f,1f,0.1f,1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            scaleAnimation.setDuration(100);
                            scaleAnimation.setFillAfter(false);

                            unscaleAnimation = new ScaleAnimation(0.1f,1f,0.1f,1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            unscaleAnimation.setDuration(100);
                            unscaleAnimation.setFillAfter(false);
                            unscaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    addButton.setVisibility(View.INVISIBLE);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });


                            priceTXT.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);
                            priceTXT.setAnimation(scaleAnimation);
                            priceTXT.setText("No prices detected!");

                            priceTXT.setTextColor(Color.parseColor("#eb4034"));
                            if(addButton.getVisibility() == View.VISIBLE) {
                            addButton.setAnimation(unscaleAnimation); }

                        }
                    }
                });



            }
        });


            }


        }

    private long lastClickTime = 0;
    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {



        if (SystemClock.elapsedRealtime() - lastClickTime > 1000) { if (item.getItemId() == R.id.cart) {
            cartDialog.show(getSupportFragmentManager(), "cart");
            //item.setEnabled(false);

        }


            if (item.getItemId() == R.id.currency) {
                currencyDialog.show(getSupportFragmentManager(), "currency");



            }
        }

        lastClickTime = SystemClock.elapsedRealtime();

        return false;
    }
}



