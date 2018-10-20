package com.party.map.myapplication;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

/**
 * Created by basti on 24/06/2017.
 * Création d'un popup d'une listes des appareils déjà associés
 */

public class ListeAppareils extends ListActivity {

    protected static String ADRESSE_MAC = null;

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ArrayAdapter<String> ArrayBluetooth = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        final BluetoothAdapter myBluetoothAdapter2 = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> PairedDevices = myBluetoothAdapter2.getBondedDevices();

        if (PairedDevices.size() > 0){
            for (BluetoothDevice Devices : PairedDevices){
                final String nameBT = Devices.getName();
                final String macBT = Devices.getAddress();
                ArrayBluetooth.add(nameBT + "\n" + macBT);
            }
        }
        setListAdapter(ArrayBluetooth);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        final String GeneralInformation = ((TextView) v).getText().toString();
        //Affiche les infos de l'appareil
        // Toast.makeText(getApplicationContext(),"Infos : " + GeneralInformation, Toast.LENGTH_LONG).show();

        final String AdresseMAC = GeneralInformation.substring(GeneralInformation.length() - 17);
        //Affiche l'adresse MAC
        // Toast.makeText(getApplicationContext(),"MAC : " + AdresseMAC, Toast.LENGTH_LONG).show();

        Intent returnMAC = new Intent();
        returnMAC.putExtra(ADRESSE_MAC,AdresseMAC);
        setResult(RESULT_OK,returnMAC);
        finish();
    }
}
