package com.party.map.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> {
    private Context myContext;
    private ArrayList<AlarmeItem> myAlarmeList;
    boolean test;
    private AlarmePrefs alarmePrefs;

    /**
     * Constructeur du recyclerView
     * @param context : Contexte partagé de l'application
     */
    RecyclerAdapter(@NonNull Context context){
        myContext = context;
        myAlarmeList = new ArrayList<>();
    }
    /**
     * Constructeur du recyclerView
     * @param context : Contexte partagé de l'application
     */
    RecyclerAdapter(@NonNull Context context, ArrayList<AlarmeItem> alarmeItems){
        this.myContext = context;
        this.myAlarmeList = alarmeItems;
        this.alarmePrefs = new AlarmePrefs(context);
    }

    /**
     * Ajout d'une alarme
     * @param heure : Heure de l'alarme à mettre en place
     * @param enable : Définir l'état de l'alarme
     */
    void addAlarm(String heure, boolean enable){
        AlarmeItem item = new AlarmeItem();
        item.sethAlarm(heure);
        item.setAlarmEnabled(enable);
        myAlarmeList.add(item);
        alarmePrefs.setPrefsAlarm(item);
        this.notifyDataSetChanged();
    }

    /**
     * Suppression d'une alarme depuis sa position
     * @param position : Position de l'item à supprimer
     */
    void removeAlarm(int position){
        myAlarmeList.remove(position);
        alarmePrefs.deleteAlarm(myAlarmeList.get(position));
        this.notifyDataSetChanged();
    }

    /**
     * Restaurer une alarme dans la liste
     * @param alarmeItem : le model de l'alarme à restaurer
     * @param position : position de l'item dans la liste
     */
    void restoreAlarm(AlarmeItem alarmeItem, int position) {
        myAlarmeList.add(position, alarmeItem);
        alarmePrefs.setPrefsAlarm(alarmeItem);
        this.notifyItemInserted(position);
    }

    /**
     * Tableau d'alarme à ajouter
     * @param newAlarm : liste d'alarmes à remplacer dans la liste actuelle
     */
    void newAlarm(ArrayList<AlarmeItem> newAlarm) {
        myAlarmeList = newAlarm;
        this.notifyDataSetChanged();
    }

    /**
     * Récupère le tableau des alarmes
     * @return la liste de toutes les alarmes enregistrées
     */
    ArrayList<AlarmeItem> getMyAlarmeList() {
        return myAlarmeList;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(myContext).inflate(R.layout.ligne, parent, false);
        return new RecyclerViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        AlarmeItem item = myAlarmeList.get(position);
        holder.tvHeure.setText(item.gethAlarm());
        holder.swAlarme.setChecked(item.isAlarmEnabled());
        holder.itemView.setTag(position);
    }
    @Override
    public int getItemCount() {
        return myAlarmeList.size();
    }


    /**
     * ViewHolder
     */
    class RecyclerViewHolder extends RecyclerView.ViewHolder{
        private TextView tvHeure;
        private Switch swAlarme;
        public RelativeLayout viewBackground, viewForeground;
        private boolean isEnabled = false;
        RecyclerViewHolder(final View itemView) {
            super(itemView);
            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);
            tvHeure = itemView.findViewById(R.id.tv_alarm);
            swAlarme = itemView.findViewById(R.id.sw_alarm);
            swAlarme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    //Switch Checked
                    Toast.makeText(myContext, "Send BLE data : " + myAlarmeList.get(getAdapterPosition()).gethAlarm() + "H" + isChecked, Toast.LENGTH_SHORT).show();
                }else {
                    //Switch disabled
                    Toast.makeText(myContext, "Alarme disabled " + itemView.getTag() + " state " + isChecked, Toast.LENGTH_SHORT).show();
                }
                }
            });

//
//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    Toast.makeText(myContext, "Ajouter une nouvelle fonctionnalité", Toast.LENGTH_SHORT).show();
//                    return false;
//                }
//            });
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });
        }
    }
}