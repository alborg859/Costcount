package onion.costcount;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CartDialog extends DialogFragment {






    ArrayList<CostItem> costItems;
    CartAdapter cartAdapter;
    RecyclerView recyclerView;
    DatabaseHelper databaseHelper;
    TextView empty;

    SharedPreferences sharedPreferences;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }

    AlertDialog d;
    @Override
    public void onStart() {
        super.onStart();

    }


    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {




        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_recycler_view, null);
        empty = view.findViewById(R.id.emptyTV);
        recyclerView = view.findViewById(R.id.recyclerView);
        databaseHelper = new DatabaseHelper(getContext());
        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        setRecyclerView();



        empty.setVisibility(View.INVISIBLE);
        if(cartAdapter.getItemCount() == 0) empty.setVisibility(View.VISIBLE);



        builder.setView(view).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setNeutralButton("CLEAR ALL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                databaseHelper.clearAll();
                if(costItems.size() == 0) {
                    empty.setVisibility(View.VISIBLE);
                } else {
                    empty.setVisibility(View.INVISIBLE);
                }
            }
        });




        return builder.create();



    }

    public void setRecyclerView() {

        costItems = databaseHelper.getAllData();
        cartAdapter = new CartAdapter(getContext(), costItems);
        cartAdapter.currentSign = sharedPreferences.getString("currentCurrency", "USD");
        cartAdapter.foreignSign = sharedPreferences.getString("foreignCurrency", "USD");
        cartAdapter.multiplier = sharedPreferences.getFloat("multiplier", 1);


        recyclerView.setAdapter(cartAdapter);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            float price = costItems.get(viewHolder.getAdapterPosition()).getPrice();
            int id = costItems.get(viewHolder.getAdapterPosition()).getId();
            costItems.remove(viewHolder.getAdapterPosition());

            databaseHelper.deleteItem(id, price);
            cartAdapter.notifyDataSetChanged();

            if(costItems.size() == 0) {
                empty.setVisibility(View.VISIBLE);
            } else {
                empty.setVisibility(View.INVISIBLE);
            }

        }
    };


}
