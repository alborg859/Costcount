package onion.costcount;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

public class CurrencyDialog extends DialogFragment {






    Spinner current;
    Spinner foreign;
    SharedPreferences sharedPreferences;



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }


    @Override
    public void onStart() {
        super.onStart();

    }


    public void getExchangeRates(String base, String foreign) {

        String url = "https://api.exchangeratesapi.io/latest?base=" + base + "&symbols=" + foreign;
        OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                String strResp = response.body().string();
                if(response.isSuccessful()) {





                    try {
                        JSONObject json = new JSONObject(strResp);
                        float m = Float.parseFloat(json.get("rates").toString().replaceAll("[^\\d.]", ""));


                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putFloat("multiplier" , m);
                        editor.apply();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }


            }
        });


    }



    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }


    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {




        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.currency_dialog_layout, null);
        current = view.findViewById(R.id.currentSpinner);
        foreign = view.findViewById(R.id.foreignSpinner);

        String[] currentArray = getResources().getStringArray(R.array.currencies);

        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);


        int curPos = 0;
        int forPos = 0;
        boolean cont = true;

        for(int i = 0; i<currentArray.length && cont; i++) {
            cont = false;
            if(sharedPreferences.getString("currentCurrency", "USD").equals(currentArray[i])) {
                curPos = i;
            } else cont = true;

            if(sharedPreferences.getString("foreignCurrency", "USD").equals(currentArray[i])) {
                forPos = i;
            } else cont = true;


        }

        current.setSelection(curPos);
        foreign.setSelection(forPos);



        builder.setView(view).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (haveNetworkConnection()) {

                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putString("currentCurrency", current.getSelectedItem().toString());
                edit.putString("foreignCurrency", foreign.getSelectedItem().toString());

                edit.apply();
                if (!current.getSelectedItem().toString().equals(foreign.getSelectedItem().toString())) {
                    getExchangeRates(current.getSelectedItem().toString(), foreign.getSelectedItem().toString());
                }


            } else {

                    Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();

            }


            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });




        return builder.create();



    }

}
