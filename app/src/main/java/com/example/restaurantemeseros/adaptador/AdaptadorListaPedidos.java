package com.example.restaurantemeseros.adaptador;

import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.restaurantemeseros.R;
import com.example.restaurantemeseros.interfaz.MenuPlatosFragment;
import com.example.restaurantemeseros.mundo.Factura;
import com.example.restaurantemeseros.mundo.Pedido;
import com.example.restaurantemeseros.mundo.Plato;

import java.util.ArrayList;

public class AdaptadorListaPedidos extends  RecyclerView.Adapter<AdaptadorListaPedidos.ViewHolder>  implements View.OnClickListener
{
    private Plato plato;
    private ArrayList<Pedido> list;
    private MenuPlatosFragment buscarPlato;
    private Context contexto;
    private static LayoutInflater  inflater = null;
    private View.OnClickListener listener;

    public AdaptadorListaPedidos(Context conexto, ArrayList<Pedido> lista)
    {
        this.list=lista;
        inflater = (LayoutInflater ) conexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = inflater.inflate(R.layout.fragment_lista_pedidos_platos, null);
        view.setOnClickListener(this);
        return new AdaptadorListaPedidos.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        plato = this.list.get(position);

        holder.txtNombre.setText(list.get(position).getNombre ());
       // holder.txtPrecio.setText(list.get(position).getPrecio ()+"");
        holder.txtCantidadPlato.setText(list.get(position).getCantidad ()+"");
        Glide.with(inflater.getContext ())
                .load(list.get(position).getImage ())
                .into(holder.imgPlatos);
        holder.item.setTag (position);
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

        if (!list.get(position).getObsevacion ().isEmpty ())
        {
            holder.imgInfo.setVisibility (View.VISIBLE);
        }else
        {
            holder.imgInfo.setVisibility (View.GONE);
        }

    }

    @Override
    public int getItemCount() {
            return list.size();
    }
    public void setFragment(MenuPlatosFragment buscarPlato)
    {
        this.buscarPlato=buscarPlato;
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

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        ConstraintLayout item;
        TextView txtNombre,txtPrecio,txtCategoria,txtObservacion,txtCantidadPlato;

        ImageView imgPlatos,imgInfo;
        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            txtNombre=(TextView) itemView.findViewById(R.id.txtNombrePlato);
           // txtPrecio=(TextView) itemView.findViewById(R.id.txtPrecio);
            txtCantidadPlato=(TextView) itemView.findViewById(R.id.txtCantidadPlato);
            imgPlatos=(ImageView) itemView.findViewById(R.id.imgPlatos);
            item=(ConstraintLayout) itemView.findViewById (R.id.itemPlatoPedido);
            imgInfo=(ImageView) itemView.findViewById(R.id.imgInfo);
        }
    }



    public ArrayList<Pedido> getList()
    {
        return this.list;
    }


}
