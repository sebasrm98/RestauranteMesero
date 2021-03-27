package com.example.restaurantemeseros.adaptador;

import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restaurantemeseros.R;
import com.example.restaurantemeseros.interfaz.MesasFragment;
import com.example.restaurantemeseros.mundo.Mesa;

import java.util.ArrayList;

public class AdaptadorListaMesa extends  RecyclerView.Adapter<AdaptadorListaMesa.ViewHolder> implements View.OnClickListener
{
    private final MesasFragment fragmentEvet;
    private Mesa proyecto;
    private ArrayList<Mesa> list;
    private MesasFragment buscarMesa;
    private Context contexto;
    private static LayoutInflater  inflater = null;
    private View.OnClickListener listener;

    public AdaptadorListaMesa(Context conexto, ArrayList<Mesa> lista, MesasFragment fragmentEvet)
    {
        this.list=lista;
        this.contexto = conexto;
        inflater = (LayoutInflater ) conexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.fragmentEvet = fragmentEvet;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.fragment_lista_mesas, null);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position)
    {
        proyecto = this.list.get(position);

        holder.txtNumeroMesa.setText(String.valueOf(list.get(position).getNumero()));
        holder.item.setTag (position);
        holder.item.setOnDragListener (fragmentEvet);
        holder.item.setOnLongClickListener (new View.OnLongClickListener ()
        {
            @Override
            public boolean onLongClick(View view)
            {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(data, shadowBuilder, view, 0);
                view.setVisibility(View.INVISIBLE);
                return true;
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public void setFragment(MesasFragment buscarMesa)
    {
        this.buscarMesa=buscarMesa;
    }

    @Override
    public void onClick(View v)
    {
        if(listener!=null)
        {
            listener.onClick(v);
        }
    }

    public void setOnclickListener(View.OnClickListener listener)
    {
        this.listener = listener;
    }

    public ArrayList<Mesa> getList()
    {
        return this.list;
    }

    public class ViewHolder  extends RecyclerView.ViewHolder
    {
        ConstraintLayout item;
        TextView txtNumeroMesa;

        public ViewHolder (@NonNull View itemView)
        {
            super(itemView);
            txtNumeroMesa=(TextView) itemView.findViewById(R.id.txtNombrePlato);
            item=(ConstraintLayout) itemView.findViewById (R.id.itemMesaOcupada);
        }
    }


}
