package onion.costcount;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.MyViewHolder> {


    private ArrayList<CostItem> costItems;
    private LayoutInflater inflater;
    public RecyclerView mRecyclerView;
    //SharedPreferences sharedPreferences;
    Context mContext;

   public  String currentSign;
    public  String foreignSign;
    public float multiplier;

    public CartAdapter (Context context, ArrayList<CostItem> shoppingItems) {
        inflater = LayoutInflater.from(context);
        mContext = context;
        this.costItems = shoppingItems;

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }

    @NonNull
    @Override
    public CartAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.cost_item_layout, parent, false);
        CartAdapter.MyViewHolder holder = new MyViewHolder(view);
            //sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        //currentSign =  sharedPreferences.getString("currentCurrency", "USD");
        //foreignSign =  sharedPreferences.getString("foreignCurrency", "USD");

        return holder;

    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.MyViewHolder holder, int i) {

        String currentSymbol = "";
        String foreignSymbol = "";

        switch (currentSign){
            case "USD":  currentSymbol = "$"; break;
            case "EUR": currentSymbol = "€"; break;
            case "GBP": currentSymbol = "£"; break;
            case "PLN": currentSymbol = "zł"; break;
            case "CHF": currentSymbol = "fr"; break;

            default: break;

        }

        switch (foreignSign){
            case "USD":  foreignSymbol = "$"; break;
            case "EUR": foreignSymbol = "€"; break;
            case "GBP": foreignSymbol = "£"; break;
            case "PLN": foreignSymbol = "zł"; break;
            case "CHF": foreignSymbol = "fr"; break;

            default: break;

        }



        if(currentSign.equals(foreignSign)) {

            holder.foreignCurrencyTV.setText("");
            holder.foreignPriceTV.setText("");

        } else  {
            holder.foreignCurrencyTV.setText(foreignSymbol);
            holder.foreignPriceTV.setText(String.format("%.2f",costItems.get(i).getPrice()*multiplier));
        }

        holder.priceTV.setText(String.valueOf(costItems.get(i).getPrice()));
        holder.currencyTV.setText(currentSymbol);




    }


    @Override
    public int getItemCount() {
        return costItems.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView priceTV;
        TextView foreignPriceTV;

        TextView currencyTV;
        TextView foreignCurrencyTV;



        public MyViewHolder(final View itemView) {
            super(itemView);

            priceTV = itemView.findViewById(R.id.priceTV);

            currencyTV = itemView.findViewById(R.id.currentCurrencyTV);


            foreignPriceTV = itemView.findViewById(R.id.foreignPriceTV);
            foreignCurrencyTV = itemView.findViewById(R.id.foreignCurrencyTV);


        }





    }


}
