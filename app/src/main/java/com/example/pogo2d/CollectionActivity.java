package com.example.pogo2d;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Optional;

public class CollectionActivity extends AppCompatActivity {

    private static GridView gridView;
    private static ArrayList<String> pkmnNames = new ArrayList<>();
    private static ArrayList<Bitmap> pokeArrayList = new ArrayList();
    private StorageReference mStorageRef;
    private FirebaseFirestore db;
    private Toolbar myToolbar;

    public static GridView getGridView() {
        return gridView;
    }

    public static void addPokemonToPokedex(String nom) {
        Bitmap img = BitmapFactory
                .decodeFile(Pokemon
                        .getPokemonFromName()
                        .get(Pokemon.shapeName(nom))
                        .getFichier()
                        .getAbsolutePath());
        img = Bitmap
                .createScaledBitmap(img,
                        img.getWidth() * 3,
                        img.getHeight() * 3,
                        false);
        pkmnNames.add(Pokemon.shapeName(nom));
        pokeArrayList.add(img);

        ((CustomAdapter) gridView
                .getAdapter())
                .notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        db = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        gridView = findViewById(R.id.gridView);

        Object[] list = new Object[2];
        list[0] = pkmnNames;
        list[1] = pokeArrayList;

        gridView.setAdapter(new CustomAdapter(CollectionActivity.this, list));

        db.collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .collection("pokemons")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                final String valPokemon = (String) document.getData().get("nom");

                                if(valPokemon!=null) {
                                    Optional<Pokemon> first = Pokemon
                                            .getPokemons()
                                            .stream()
                                            .filter(p -> Pokemon.shapeName(p.getNom())
                                                    .equals(Pokemon.shapeName(valPokemon)))
                                            .findFirst();

                                    if (first.isPresent()) {
                                        Bitmap img = BitmapFactory
                                                .decodeFile(first.get().getFichier().getAbsolutePath());
                                        img = Bitmap
                                                .createScaledBitmap(img,
                                                        img.getWidth() * 3,
                                                        img.getHeight() * 3,
                                                        false);
                                        pkmnNames.add(Pokemon.shapeName(valPokemon));
                                        pokeArrayList.add(img);

                                        ((CustomAdapter) gridView
                                                .getAdapter())
                                                .notifyDataSetChanged();
                                    } else {
                                        Log.d("CollectionActivity",
                                                "Error getting pokemon: " + valPokemon + " " + Pokemon.shapeName(valPokemon) + " ",
                                                task.getException());
                                    }
                                }
                            }

                        } else {
                            Log.d("CollectionActivity", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        // Configure the search info and add any event listeners...
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            /*case R.id.pokedex_id:
                // User chose the "Settings" item, show the app settings UI...
                intent = new Intent(this, CollectionActivity.class);
                this.startActivity(intent);
                return true;*/

            case R.id.carte_id:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                intent = new Intent(this, MapActivity.class);
                this.startActivity(intent);
                return true;

            case R.id.logout_id:
                CollectionActivity.pokeArrayList = new ArrayList<>();
                CollectionActivity.pkmnNames = new ArrayList<>();

                FirebaseAuth.getInstance().signOut();
                intent = new Intent(this, MainActivity.class);
                this.startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
