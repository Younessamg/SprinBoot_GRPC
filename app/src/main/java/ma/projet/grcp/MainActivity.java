package ma.projet.grcp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import ma.projet.grpc.stubs.Compte;
import ma.projet.grpc.stubs.CompteServiceGrpc;
import ma.projet.grpc.stubs.GetAllComptesRequest;
import ma.projet.grpc.stubs.GetAllComptesResponse;
import ma.projet.grpc.stubs.SaveCompteRequest;
import ma.projet.grpc.stubs.SaveCompteResponse;
import ma.projet.grpc.stubs.CompteRequest;
import ma.projet.grpc.stubs.TypeCompte;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private CompteAdapter compteAdapter;
    private List<Compte> compteList = new ArrayList<>();
    private FloatingActionButton fabAddCompte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewComptes);
        fabAddCompte = findViewById(R.id.fabAddCompte);

        compteAdapter = new CompteAdapter(compteList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(compteAdapter);

        // Charger les comptes depuis le serveur
        new Thread(this::communicateWithServer).start();

        // Ajouter un nouveau compte
        fabAddCompte.setOnClickListener(v -> addNewCompte());
    }

    private void communicateWithServer() {
        try (ManagedChannel channel = ManagedChannelBuilder
                .forAddress("10.0.2.2", 9090)
                .usePlaintext()
                .build()) {

            CompteServiceGrpc.CompteServiceBlockingStub stub = CompteServiceGrpc.newBlockingStub(channel);
            GetAllComptesRequest request = GetAllComptesRequest.newBuilder().build();
            GetAllComptesResponse response = stub.allComptes(request);

            runOnUiThread(() -> {
                compteList.clear();
                compteList.addAll(response.getComptesList());
                compteAdapter.notifyDataSetChanged();
            });
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la communication avec le serveur", e);
            runOnUiThread(() -> Toast.makeText(this, "Erreur de communication avec le serveur", Toast.LENGTH_SHORT).show());
        }
    }

    private void addNewCompte() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_compte, null);

        EditText editTextSolde = dialogView.findViewById(R.id.editTextSolde);
        Spinner spinnerTypeCompte = dialogView.findViewById(R.id.spinnerTypeCompte);
        Button btnSaveCompte = dialogView.findViewById(R.id.btnSaveCompte);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"COURANT", "EPARGNE"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTypeCompte.setAdapter(spinnerAdapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Ajouter un compte")
                .setView(dialogView)
                .create();

        btnSaveCompte.setOnClickListener(v -> {
            String soldeStr = editTextSolde.getText().toString().trim();
            String typeCompte = spinnerTypeCompte.getSelectedItem().toString();

            if (soldeStr.isEmpty()) {
                Toast.makeText(this, "Veuillez saisir un solde valide", Toast.LENGTH_SHORT).show();
                return;
            }

            double solde;
            try {
                solde = Double.parseDouble(soldeStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Le solde doit être un nombre valide", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                try (ManagedChannel channel = ManagedChannelBuilder
                        .forAddress("10.0.2.2", 9090)
                        .usePlaintext()
                        .build()) {

                    CompteServiceGrpc.CompteServiceBlockingStub stub = CompteServiceGrpc.newBlockingStub(channel);

                    SaveCompteRequest request = SaveCompteRequest.newBuilder()
                            .setCompte(
                                    CompteRequest.newBuilder()
                                            .setSolde((float) solde)
                                            .setDateCreation(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                                            .setType(TypeCompte.valueOf(typeCompte))
                                            .build()
                            )
                            .build();

                    stub.saveCompte(request);

                    // Récupérer la liste mise à jour après ajout
                    GetAllComptesRequest getAllRequest = GetAllComptesRequest.newBuilder().build();
                    GetAllComptesResponse getAllResponse = stub.allComptes(getAllRequest);

                    runOnUiThread(() -> {
                        compteList.clear();
                        compteList.addAll(getAllResponse.getComptesList());
                        compteAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Erreur lors de l'ajout du compte", e);
                    runOnUiThread(() -> Toast.makeText(this, "Erreur lors de l'ajout du compte", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        dialog.show();
    }
}
