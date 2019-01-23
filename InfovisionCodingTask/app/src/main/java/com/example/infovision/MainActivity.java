package com.example.infovision;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.List;

import adapter.CustomAdapter;
import model.RetroPhoto;
import network.RetroInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements RecyclerViewClickListener{

    private static final String TAG = "MainActivity";


    // Remote Config keys
    private static final String LOADING_PHRASE_CONFIG_KEY = "loading_phrase";
    private static final String WELCOME_MESSAGE_KEY = "welcome_message";
    private static final String WELCOME_MESSAGE_CAPS_KEY = "welcome_message_caps";


    private FirebaseRemoteConfig mFirebaseRemoteConfig;


    private CustomAdapter adapter;
    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setupRemoteConfig();

    }


    private void setupRemoteConfig(){
        // Get Remote Config instance.
        // [START get_remote_config_instance]
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance ( );
        // [END get_remote_config_instance]


        // Create Remote Config Setting to enable developer mode.
        // Fetching configs from the server is normally limited to 5 requests per hour.
        // Enabling developer mode allows many more requests to be made per hour, so developers
        // can test different config values during development.
        // [START enable_dev_mode]
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder( )
                .setDeveloperModeEnabled ( BuildConfig.DEBUG )
                .build();
        mFirebaseRemoteConfig.setConfigSettings ( configSettings );
        // [END enable_dev_mode]


        // Set default Remote Config values. In general you should have in app defaults for all
        // values that you may configure using Remote Config later on. The idea is that you
        // use the in app defaults and when you need to adjust those defaults, you set an updated
        // value in the App Manager console. Then the next time you application fetches from the
        // server, the updated value will be used. You can set defaults via an xml file like done
        // here or you can set defaults inline by using one of the other setDefaults methods.S
        // [START set_default_values]
        mFirebaseRemoteConfig.setDefaults ( R.xml.remote_config );
        // [END set_default_values]

        fetchRemoteConfigData();

    }


    private void fetchRemoteConfigData(){
        long cacheExpiration = 3600; // 1 hour in seconds.
        // If in developer mode cacheExpiration is set to 0 so each fetch will retrieve values from
        // the server.
        if (mFirebaseRemoteConfig.getInfo( ).getConfigSettings( ).isDeveloperModeEnabled( )) {
            cacheExpiration = 0;
        }


        // [START fetch_config_with_callback]
        // cacheExpirationSeconds is set to cacheExpiration here, indicating that any previously
        // fetched and cached config would be considered expired because it would have been fetched
        // more than cacheExpiration seconds ago. Thus the next fetch would go to the server unless
        // throttling is in progress. The default expiration duration is 43200 (12 hours).
        mFirebaseRemoteConfig.fetch ( cacheExpiration )
                .addOnCompleteListener ( this, new OnCompleteListener <Void>( ) {
                    @Override
                    public void onComplete(@NonNull Task <Void> task ) {
                        if ( task.isSuccessful ( )) {
                            Toast.makeText ( MainActivity.this, "Fetch Succeeded"+task.toString(),
                                    Toast.LENGTH_SHORT ).show( );

                            // Once the config is successfully fetched it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched ( );
                        } else {
                            Toast.makeText ( MainActivity.this, "Fetch Failed"+task.toString(),
                                    Toast.LENGTH_SHORT).show( );
                        }

                    }
                } );
        // [END fetch_config_with_callback]

        fetchPicturesFromRemoteConfig();
    }

    private void fetchPicturesFromRemoteConfig() {
        String url = mFirebaseRemoteConfig.getString("new_image");
        System.out.println("*** "+url);

        /*Create handle for the RetrofitInstance interface*/
        GetDataService service = RetroInterface.getRetrofitInstance().create(GetDataService.class);
        Call<List<RetroPhoto>> call = service.getAllPhotos();
        call.enqueue(new Callback<List<RetroPhoto>>() {
            @Override
            public void onResponse(Call<List<RetroPhoto>> call, Response<List<RetroPhoto>> response) {
                generateDataInList(response.body());
            }

            @Override
            public void onFailure(Call<List<RetroPhoto>> call, Throwable t) {
            //    progressDoalog.dismiss();
                Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });


    }


    private void generateDataInList(List<RetroPhoto> photoList){

        recyclerView = findViewById(R.id.customRecycler);


        adapter = new CustomAdapter(this,photoList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        adapter.setClickListener(this);
    }


    @Override
    public void onClick(View view, int position) {
        System.out.println("*** posiiton: "+position
        );
    }
}
