package ma.projet.grcp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ma.projet.grpc.stubs.Compte;

public class CompteAdapter extends RecyclerView.Adapter<CompteAdapter.CompteViewHolder> {

    private List<Compte> comptes;

    public CompteAdapter(List<Compte> comptes) {
        this.comptes = comptes;
    }

    @NonNull
    @Override
    public CompteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_compte, parent, false);
        return new CompteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompteViewHolder holder, int position) {
        Compte compte = comptes.get(position);
        holder.tvCompteId.setText(String.format("ID: %d", compte.getId()));
        holder.tvSolde.setText(String.format("Solde: %.2f", compte.getSolde()));
        holder.tvDateCreation.setText(String.format("Date: %s", compte.getDateCreation()));
        holder.tvType.setText(String.format("Type: %s", compte.getType()));

        // Ajouter un gestionnaire de clic sur l'élément
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(
                    v.getContext(),
                    "Compte sélectionné: ID " + compte.getId(),
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    @Override
    public int getItemCount() {
        return comptes.size();
    }

    static class CompteViewHolder extends RecyclerView.ViewHolder {
        TextView tvCompteId, tvSolde, tvDateCreation, tvType;

        public CompteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCompteId = itemView.findViewById(R.id.tvCompteId);
            tvSolde = itemView.findViewById(R.id.tvSolde);
            tvDateCreation = itemView.findViewById(R.id.tvDateCreation);
            tvType = itemView.findViewById(R.id.tvType);
        }
    }
}
