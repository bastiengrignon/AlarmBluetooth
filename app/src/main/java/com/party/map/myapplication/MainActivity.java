package com.party.map.myapplication;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
//import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.UUID;
import static com.party.map.myapplication.FakeData.*;

/* TODO:    Afficher notification
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, CompoundButton.OnCheckedChangeListener {
    private RelativeLayout disconnect;
    private RelativeLayout connect;
    private TextView HeureAlarme;
    private ArrayList<AlarmeItem> myAlarmList;
    //Variable d'heures et de minutes
    private int MySelectedHour;
    private int MySelectedMinute;       // memo: à supprimer quand plus de textView mais juste liste
    private static final String SMILING_FACE_WITH_HALO = "😇";
    private static final String SavedKey = "saved_key";
    private String date, temps;

    //Intent pour le Bluetooth, connexion et activation
    private static final int ACTIVATION_BLUETOOTH = 1;
    private static final int CONNEXION_BLUETOOTH = 2;
    //Indispensable pour connexion bluetooth
    private ConnectedThread connectedThread;
    private BluetoothAdapter myBluetoothAdapter = null;
    private BluetoothSocket mySocket = null;
    private boolean isConnected = false ;         //Test Bluetooth connecté ou non
    //Très important il paraît...
    UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private RecyclerAdapter myRecyclerAdapter;


    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageView imageReveil = findViewById(R.id.imageReveil);
        final Button btnConnexion = findViewById(R.id.buttonConnexion);
        final FloatingActionButton btnAjoutAlarme = findViewById(R.id.floatingActionButtonAdd);
        final Switch sendAlarm = findViewById(R.id.switch_alarm);
        final RecyclerView myRecyclerView = findViewById(R.id.rv_list);

        disconnect = findViewById(R.id.disconnnected);
        connect = findViewById(R.id.connected);
        connect.setVisibility(View.GONE);
        disconnect.setVisibility(View.VISIBLE);
        HeureAlarme = findViewById(R.id.textViewHeure);

        myAlarmList = new ArrayList<>();
        myRecyclerAdapter = new RecyclerAdapter(this, myAlarmList);
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (savedInstanceState != null) {   //Si on a déjà une instance sauvegardée on récupère les alarmes
//            ArrayList<AlarmeItem> mysRegisteredAlarms = savedInstanceState.getParcelableArrayList(SavedKey);
//            myRecyclerAdapter.newAlarm(mysRegisteredAlarms);
            AlarmePrefs alarmePrefs = new AlarmePrefs(getApplicationContext());
            alarmePrefs.getPrefsAlarm(1);
        }
        else {
            Alarm alarms[] = FakeData.getFakeAlarm();
            for (Alarm alarm : alarms) {
                myRecyclerAdapter.addAlarm(alarm.heure, alarm.enabled);
            }
        }

        //memo:  Préparation de la ligne, du séparateur et de l'animation pour la liste
        myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        myRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        myRecyclerView.setItemAnimator(new DefaultItemAnimator());
        myRecyclerView.setAdapter(myRecyclerAdapter);

        //Verification du Bluetooth présent sur le téléphone puis demande à l'utilisateur l'autorisation d'activation
        if (myBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.phone_no_bluetooth), Toast.LENGTH_SHORT).show();
            finish();
        }
        else if (!myBluetoothAdapter.isEnabled()) {
            Intent activationBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(activationBluetooth, ACTIVATION_BLUETOOTH);
        }
        btnConnexion.setOnClickListener(this);
        btnAjoutAlarme.setOnClickListener(this);
        imageReveil.setOnLongClickListener(this);
        sendAlarm.setOnCheckedChangeListener(this);
        itemSwipe(myRecyclerView);

    }//Fin du OnCreate


    /**
     * Fonction de vibration du téléphone
     * @param temps : durée de vibration
     */
    public void vibrer(int temps){
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        if (vib != null) {
            vib.vibrate(temps);
        }
    }

    /**
     * Gestion des résultats de requètre Bluetooth
     * @param requestCode : Code de requète  (ACTVIVATION_BLE, ...)
     * @param resultCode : Code de résultat (OK, CANCEL, NOK, ...)
     * @param data : Données passées en paramètres
     */
    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case ACTIVATION_BLUETOOTH :
                if(resultCode == Activity.RESULT_OK){
                    Toast.makeText(getApplicationContext(),R.string.bluetooth_enabled_message,Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(),R.string.error_bluetooth_activation,Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;

            case CONNEXION_BLUETOOTH :
                if (resultCode == Activity.RESULT_OK){
                    final String MAC = Objects.requireNonNull(data.getExtras()).getString(ListeAppareils.ADRESSE_MAC);
                    //Toast.makeText(getApplicationContext(),"MAC finale : " + MAC,Toast.LENGTH_LONG).show();
                    final BluetoothDevice myDevice = myBluetoothAdapter.getRemoteDevice(MAC);
                    try{
                        mySocket = myDevice.createRfcommSocketToServiceRecord(MY_UUID);
                        mySocket.connect();
                        isConnected = true;
                        connectedThread = new ConnectedThread(mySocket);
                        connectedThread.start();
                        Toast.makeText(getApplicationContext(),"Bluetooth connecté à : "  + myDevice.getName(),Toast.LENGTH_LONG).show();
                        connect.setVisibility(View.VISIBLE);
                        disconnect.setVisibility(View.GONE);
                    }catch (IOException error){
                        isConnected = false;
                        Toast.makeText(getApplicationContext(),R.string.error_connection_bluetooth,Toast.LENGTH_SHORT).show();
                        connect.setVisibility(View.GONE);
                        disconnect.setVisibility(View.VISIBLE);
                    }

                }else {
                    Toast.makeText(getApplicationContext(),R.string.error_mac_connection,Toast.LENGTH_SHORT).show();
                }
        }
    }

    /**
     * Gestion de l'appui court pour différents composants de l'appli
     * @param view : la vue de l'item clické
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.floatingActionButtonAdd:
                vibrer(100);
                final Calendar mcurrentTime = Calendar.getInstance();     // memo : Ouverture du popup de l'heure
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        //NbAlarme++;
                        MySelectedHour = selectedHour;
                        MySelectedMinute = selectedMinute;
                        HeureAlarme.setText(getString(R.string.text_alarm, selectedHour, selectedMinute));
                        myRecyclerAdapter.addAlarm(getString(R.string.text_alarm, selectedHour, selectedMinute), false);    //memo : ajout de l'alarme à la liste
                    }
                }, hour, minute, true);     //true = 24 hour time
                mTimePicker.setTitle(getString(R.string.time_picker_title));
                mTimePicker.show();
                break;
            case R.id.buttonConnexion:
                vibrer(100);
                if (isConnected) {
                    //Deconnexion
                    try {
                        mySocket.close();
                        isConnected = false;
                        Toast.makeText(getApplicationContext(), getString(R.string.bluetooth_disconnected), Toast.LENGTH_SHORT).show();
                    } catch (IOException error) {
                        Toast.makeText(getApplicationContext(), getString(R.string.bluetooth_error_connection), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Connexion ouverture de la liste des appareils déja appareillés
                    Intent OpenList = new Intent(MainActivity.this, ListeAppareils.class);
                    startActivityForResult(OpenList, CONNEXION_BLUETOOTH);
                }
                break;
            /*case R.id.buttonSend:
                vibrer(100);
                if (isConnected) {
                    /*connectedThread.write(String.valueOf(MySelectedHour));
                    connectedThread.write(":");
                    connectedThread.write(String.valueOf(MySelectedMinute));
                    connectedThread.write("\n");*
                    setDataToSend(String.valueOf(MySelectedHour + ":" + String.valueOf(MySelectedMinute) + "\n"), 0);
                    Toast.makeText(getApplicationContext(), "Alarme activée", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(), "Tu dois te connecter d'abord", Toast.LENGTH_SHORT).show();
                }
                break;*/
            case R.id.imageReveil:
                break;
            default:
                break;
        }
    }

    /**
     * Gestion de l'appui long pour différents composants de l'appli
     * @param view : la vue de l'item clické
     * @return true
     */
    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()){
            case R.id.imageReveil:
                vibrer(200);
                AlertDialog.Builder easterEggDialog = new AlertDialog.Builder(MainActivity.this);
                easterEggDialog.setTitle("Racourci trouvé")
                        .setMessage("Tu a trouvé le raccourci, tu peux accéder à l'appli pour voir à quoi ça ressemble mais tu ne pourras pas envoyer de données"+SMILING_FACE_WITH_HALO)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Action pour bouton Ok
                                connect.setVisibility(View.VISIBLE);
                                disconnect.setVisibility(View.GONE);
                            }
                        })
                        .setNegativeButton("Retourner à la connexion", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Action pour bouton Fermer
                                connect.setVisibility(View.GONE);
                                disconnect.setVisibility(View.VISIBLE);
                                dialogInterface.dismiss();
                            }
                        });
                final AlertDialog EasterEgg = easterEggDialog.create();
                EasterEgg.show();
            break;
            case R.id.buttonConnexion:
                if (isConnected) {
                    //Deconnexion
                    try {
                        mySocket.close();
                        isConnected = false;
                        Toast.makeText(getApplicationContext(), "Bluetooth Déconnecté", Toast.LENGTH_SHORT).show();
                    } catch (IOException error) {
                        Toast.makeText(getApplicationContext(), "Une erreur est survenue lors de la connexion", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
        return true;
    }

    /**
     * Création du menu 3 points
     * @param menu : le menu à créer
     * @return le menu créé
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Sélection du menu 3 points
     * @param item : liste des items du menu
     * @return l'item sélectionné
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_maj_hour:
                showMajAlert();
                break;
            case R.id.menu_param:
                launchSettings();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Fermeture de la connexion Bluetooth
     */
    @Override
    protected void onStop() {
        if (isConnected) {
            //Deconnexion
            try {
                mySocket.close();
                isConnected = false;
                Toast.makeText(getApplicationContext(), "Bluetooth Déconnecté", Toast.LENGTH_SHORT).show();
            } catch (IOException error) {
                Toast.makeText(getApplicationContext(), "Une erreur est survenue lors de la connexion", Toast.LENGTH_SHORT).show();
            }
        }
        super.onStop();
    }

    /**
     * ACtivation de l'alarme avec envoi de données si connecté
     * @param buttonView : model du bouton
     * @param isChecked : état du switch
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        vibrer(100);
        if (isChecked){
            HeureAlarme.setTypeface(null, Typeface.BOLD);
            if (isConnected){
                setDataToSend(String.valueOf(MySelectedHour + ":" + String.valueOf(MySelectedMinute) + "\n"), 0);
                Toast.makeText(getApplicationContext(), getString(R.string.alarm_activated), Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(),getString(R.string.error_not_connected), Toast.LENGTH_SHORT).show();
            }
        }else {
            HeureAlarme.setTypeface(null, Typeface.NORMAL);
            if (isConnected){
                setDataToSend("", 2);
            }else {
                Toast.makeText(getApplicationContext(),getString(R.string.error_not_connected), Toast.LENGTH_SHORT).show();
            }

        }
    }

    /**
     * Flux Bluetooth, entrée et sortie
     */
    private class ConnectedThread extends Thread {
        //private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        //private byte[] mmBuffer; // mmBuffer store for the stream

        @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
        ConnectedThread(BluetoothSocket socket) {
            //InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            /*try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                //  Log.e(TAG, "Error occurred when creating input stream", e);
            }*/
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                //  Log.e(TAG, "Error occurred when creating output stream", e);
            }

            //mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
/*
        public void Read() {
            byte[] buffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Permet de recevoir des données (pas utilisé pour l'instant)

            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    //Message readMsg = mHandler.obtainMessage(
                    //       MessageConstants.MESSAGE_READ, numBytes, -1,
                    //       mmBuffer);
                   // readMsg.sendToTarget();
                } catch (IOException e) {
                   // Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }
*/
        // Permet d'envoyer des données.
        void write(String Datasend) {
            byte[] msgBuffer = Datasend.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(),R.string.error_data_send,Toast.LENGTH_SHORT).show();
            }
        }
        /*
        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mySocket.close();
            } catch (IOException e) {
                //Log.e(TAG, "Could not close the connect socket", e);
            }
        }*/
    }

    /**
     * Bouton retour (back)
     * @param keyCode : Code de la touche appuyée
     * @param event : Evènement lors de l'appui
     * @return false
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            connect.setVisibility(View.GONE);
            disconnect.setVisibility(View.VISIBLE);
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SavedKey, myRecyclerAdapter.getMyAlarmeList());
    }

    /**
     * Mise à jour de l'heure
     */
    private void showMajAlert(){
        final Calendar calendar = Calendar.getInstance();
        View inflatedView = LayoutInflater.from(this).inflate(R.layout.maj_date, null);
        final TextView dateEdit = inflatedView.findViewById(R.id.tvDate);
        final TextView tempsEdit = inflatedView.findViewById(R.id.tvTemps);
        dateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dateEdit.setText(getString(R.string.maj_date, dayOfMonth, month, year));
                        date = getString(R.string.maj_date, dayOfMonth, month, year);
                    }
                },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
        tempsEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        tempsEdit.setText(getString(R.string.maj_temps, hourOfDay, minute));
                        temps = getString(R.string.maj_temps, hourOfDay, minute);
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePickerDialog.show();
            }
        });

        AlertDialog.Builder dialogMAJ = new AlertDialog.Builder(this)
                .setTitle("Date de la mise à jour")
                .setView(inflatedView)
                .setPositiveButton("Mettre à jour", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isConnected){
                            setDataToSend(date + "#" + temps + "\n",1);
                        }
                    }
                })
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        final AlertDialog dialogShow = dialogMAJ.create();
        dialogShow.show();
    }

    /**
     * Encapsulation des données à envoyer
     * @param message : chaine de caractère à envoyer
     * @param mode : Mode à envoyer
     */
    private void setDataToSend(final String message, final int mode){
        //mode 0 : envoie activation de l'heure de l'alarme
        //mode 1 : mise à jour de l'heure de l'horloge
        //mode 2 : désactivation de l'alarme
        if (mode >= 0 && mode < 3){
            if (mode == 0){
                connectedThread.write( "1m" + message);
                Toast.makeText(this, "Alarme activée : " + message, Toast.LENGTH_SHORT).show();
            }else if(mode == 1) {
                connectedThread.write( "2m" + message);
                Toast.makeText(this, "Mise à jour de l'heure" + message, Toast.LENGTH_SHORT).show();
            }else {
                connectedThread.write("3m" + message);
                Toast.makeText(this, "Désactivation de l'alarme", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, "Vous n'êtes pas dans le bon mode", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Lancement de l'activité Paramètres
     */
    private void launchSettings(){
        Intent settingsActivity = new Intent(this, SettingsActivity.class);
        startActivity(settingsActivity);
    }

    /**
     * Gestion du Swipe vers la gauche pour suppresion d'alarmes
     * @param myRecyclerView : passage du recyclerView
     */
    private void itemSwipe(RecyclerView myRecyclerView){
        ItemTouchHelper.SimpleCallback itemTouchHelper = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                if (viewHolder != null) {
                    final View forgroundView = ((RecyclerAdapter.RecyclerViewHolder) viewHolder).viewForeground;
                    getDefaultUIUtil().onSelected(forgroundView);
                }
            }
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = (int)viewHolder.itemView.getTag();
                String itemName = myAlarmList.get(viewHolder.getAdapterPosition()).gethAlarm();

                if (direction == ItemTouchHelper.LEFT) {

                    final AlarmeItem deletedItem = myAlarmList.get(viewHolder.getAdapterPosition());
                    final int deletedPosition = viewHolder.getAdapterPosition();
                    myRecyclerAdapter.removeAlarm(position);
                    Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(),getString(R.string.snackbar_restore_alarm, itemName), getResources().getInteger(R.integer.snackbar_length));
                    snackbar.setAction("Annuler", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            myRecyclerAdapter.restoreAlarm(deletedItem, deletedPosition);
                        }
                    });
                    snackbar.setActionTextColor(getColor(R.color.colorArduino));
                    snackbar.show();
                }
            }
            @Override
            public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                final View forgroundView = ((RecyclerAdapter.RecyclerViewHolder) viewHolder).viewForeground;
                getDefaultUIUtil().onDrawOver(c, recyclerView, forgroundView, dX, dY, actionState, isCurrentlyActive);
            }
            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                final View forgroundView = ((RecyclerAdapter.RecyclerViewHolder) viewHolder).viewForeground;
                getDefaultUIUtil().onDraw(c, recyclerView, forgroundView, dX, dY, actionState, isCurrentlyActive);
            }
            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                final View forgroundView = ((RecyclerAdapter.RecyclerViewHolder) viewHolder).viewForeground;
                getDefaultUIUtil().clearView(forgroundView);
            }

        };
        new ItemTouchHelper(itemTouchHelper).attachToRecyclerView(myRecyclerView);
    }
}
//todo : mise en place changement temps alarme, sonnerie, led/buzzer actif pendant alarme, voir note téléphone...

//fixme : Quand l'avant dernier élément à été supprimé puis restauré, si on supprime le dernier élément, celui d'avant (avant dernier) prend la valeur du dernier élément de la liste
