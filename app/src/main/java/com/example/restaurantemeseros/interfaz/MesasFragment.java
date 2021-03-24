package com.example.restaurantemeseros.interfaz;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.example.restaurantemeseros.Abtract.InterfazFragamen;
import com.example.restaurantemeseros.R;
import com.example.restaurantemeseros.adaptador.AdaptadorListaMesa;
import com.example.restaurantemeseros.adaptador.AdaptadorListaMesaDesocupada;
import com.example.restaurantemeseros.adaptador.AdaptadorListaPedidos;
import com.example.restaurantemeseros.adaptador.AdaptadorListaPlatos;
import com.example.restaurantemeseros.mundo.Mesa;
import com.example.restaurantemeseros.mundo.Pedido;
import com.example.restaurantemeseros.mundo.Plato;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.

 * create an instance of this fragment.
 */
public class MesasFragment extends Fragment implements View.OnDragListener
{
    protected RequestQueue requestQueue;
    protected JsonRequest jsonRequest;
    private SearchView buscarMesa;
    private RecyclerView listaMesas, mesasDesocupadas;
    private AdaptadorListaMesa adaptadorListaMesa;
    private AdaptadorListaMesaDesocupada adaptadorListaMesaDesocupada;
    private ArrayList<Mesa> mesasDes;
    private ArrayList<Mesa> mesasDesAux;
    private ArrayList<Mesa> mesas;
    private ArrayList<Mesa> mesasAux;
    private ProgressDialog dialog;
    private int cantMesas;
    private Activity actividad;
    private InterfazFragamen interfazFragamen;
    ImageButton agregarMesa;
    Dialog mDialog;
    TextView numeroMesa;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private String idEmpleados;

    public MesasFragment()
    {
        // Required empty public constructor
    }

    public static MesasFragment newInstance(String param1, String param2)
    {
        MesasFragment fragment = new MesasFragment ();
        Bundle args = new Bundle ();
        args.putString (ARG_PARAM1, param1);
        args.putString (ARG_PARAM2, param2);
        fragment.setArguments (args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate (savedInstanceState);
        if (getArguments () != null)
        {
            mParam1 = getArguments ().getString (ARG_PARAM1);
            mParam2 = getArguments ().getString (ARG_PARAM2);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate (R.layout.fragment_mesas, container, false);

        this.agregarMesa = v.findViewById (R.id.AgregarPedido);
        this.buscarMesa = v.findViewById (R.id.searchBuscarPlato);
        this.listaMesas = v.findViewById (R.id.listaMesas);
        this.mesasDesocupadas = v.findViewById (R.id.listaMesasDesocupadas);
        this.requestQueue = Volley.newRequestQueue (getContext ());
        this.mesasDes = new ArrayList<Mesa> ();
        this.mesasDesAux = new ArrayList<Mesa> ();
        this.mesas = new ArrayList<Mesa> ();
        this.mesasAux = new ArrayList<Mesa> ();
        this.adaptadorListaMesa = new AdaptadorListaMesa (getContext (), this.mesas);
        this.listaMesas.setLayoutManager (new LinearLayoutManager (getContext ()));
        this.mesasDesocupadas.setLayoutManager (new GridLayoutManager (getContext (), 3));
        this.adaptadorListaMesaDesocupada = new AdaptadorListaMesaDesocupada (getContext (), this.mesasDes);
        this.mesasDesocupadas.setAdapter (adaptadorListaMesaDesocupada);
        this.listaMesas.setAdapter(adaptadorListaMesa);
        this.listaMesas.setLayoutManager (new GridLayoutManager (getContext (), 3));
        recuperarPreferencias();




/*new Timer ().scheduleAtFixedRate(new TimerTask ()

        {
            @Override
            public void run()
            {

                System.out.println ("A Kiss after 5 seconds");
            }
        },1,6000);*/
        buscarlista ();
        buscarMesaDesocupada ();
        this.buscarMesa.setOnQueryTextListener (new SearchView.OnQueryTextListener ()
        {
            @Override
            public boolean onQueryTextSubmit(String query) {return false; }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                mesas.clear ();
                listaMesas.setAdapter (adaptadorListaMesa);

                if (!newText.isEmpty ())
                {
                    for (Mesa m : mesasAux)
                    {
                        if (m.getNumero ().contains (newText))
                        {
                            mesas.add (m);
                        }
                    }
                } else
                {
                    mesas.addAll (mesasAux);
                }
                return false;
            }
        });
        this.adaptadorListaMesa.setOnclickListener (new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                Mesa mesa = mesas.get (listaMesas.getChildAdapterPosition (v));
                Bundle bundleEnvio = new Bundle ();
                bundleEnvio.putSerializable ("mesa", mesa);
                getParentFragmentManager ().setFragmentResult ("key", bundleEnvio);
               getFragmentManager ().popBackStack ();
            }
        });

        this. mesasDesocupadas.setOnDragListener(this);
        this.listaMesas.setOnDragListener(this);
        return v;

    }

    public void buscarMesaDesocupada()
    {
        mesasDesAux.clear ();
        Map<String, String> params = new HashMap<String, String> ();
        params.put ("buscarMesasDesocupadas", "Mes");
        JSONObject parameters = new JSONObject (params);
        String url = "https://openm.co/consultas/pedidos.php";

        jsonRequest = new JsonObjectRequest (Request.Method.POST, url, parameters, new Response.Listener<JSONObject> () {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONArray datos = response.getJSONArray ("datos");
                    for (int i = 0; i < datos.length (); i++) {
                        JSONObject mesa = datos.getJSONObject (i);
                        int id = mesa.getInt ("idmesas");
                        String numero = mesa.getString ("numero");
                        String codigoQR = mesa.getString ("codigoQR");
                        String estado = mesa.getString ("estado");
                        Mesa m = new Mesa (id, numero, codigoQR, estado);
                        mesasDesAux.add (m);
                    }

                    if (mesasDesAux.size () != cantMesas) {
                        mesasDes.clear ();
                        String numero = mesasDesAux.get (mesasDesAux.size () - 1).getNumero ();
                        mesasDes.addAll (mesasDesAux);
                        cantMesas = mesasDes.size ();
                    }
                    adaptadorListaMesaDesocupada.notifyDataSetChanged ();

                } catch (JSONException e) {
                    e.printStackTrace ();
                }
            }
        }, new Response.ErrorListener () {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace ();
            }
        });

        requestQueue.add (jsonRequest);
    }

    public void buscarlista()
    {
        mesasAux.clear();
        Map<String,String> params= new HashMap<String, String> ();
        params.put("buscarMesas","Mes");
        JSONObject parameters = new JSONObject(params);
        String url="https://openm.co/consultas/pedidos.php";
        jsonRequest=new JsonObjectRequest (Request.Method.POST, url, parameters, new Response.Listener<JSONObject> ()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                try
                {
                    if (response!=null)
                    {
                        JSONArray datos = response.getJSONArray ("datos");
                        for (int i = 0; i < datos.length(); i++)
                        {
                            JSONObject mesa = datos.getJSONObject(i);
                            int id=mesa.getInt ("idmesas");
                            String numero=mesa.getString("numero");
                            String codigoQR=mesa.getString("codigoQR");
                            String estado=mesa.getString("estado");
                            Mesa m=new Mesa( id,  numero,  codigoQR, estado);
                            mesasAux.add(m);
                        }
                        if (mesasAux.size()!=cantMesas)
                        {
                            mesas.clear();
                            String numero = mesasAux.get (mesasAux.size () - 1).getNumero ();
                            //  Toast.makeText(getContext (),"Mesa "+numero+" ha sido ocupada", Toast.LENGTH_SHORT).show();
                            mesas.addAll(mesasAux);
                            cantMesas=mesas.size ();
                        }
                        adaptadorListaMesa.notifyDataSetChanged ();
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace ();
                }
            }
        }, new Response.ErrorListener ()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                error.printStackTrace ();
            }
        });
        requestQueue.add(jsonRequest);
    }



    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof Activity)
        {
            this.actividad = (Activity) context;
            // this.interfazFragamen = (InterfazFragamen) this.actividad;
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    @Override
    public boolean onDrag(View v, DragEvent event)
    {
        int action = event.getAction();
        switch (action)
        {
            case DragEvent.ACTION_DRAG_STARTED:
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                //    v.setBackgroundColor(Color.LTGRAY);
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                v.setBackgroundColor(Color.YELLOW);
                break;
            case DragEvent.ACTION_DROP:

                int positionFuente = -1;
                View viewSource = (View) event.getLocalState ();
                RecyclerView RecyclerView = (RecyclerView) viewSource.getParent ();
                positionFuente = (int) viewSource.getTag ();

                if (RecyclerView.getAdapter () instanceof AdaptadorListaMesa)
                {
                    final AdaptadorListaMesa adaptadorListaMesa = (AdaptadorListaMesa) RecyclerView.getAdapter ();
                    final Mesa mesa = adaptadorListaMesa.getList ().get (positionFuente);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext ());
                    builder.setTitle("Title");
                    final TextView input = new TextView(getContext ());
                    input.setText ("¿Desea eliminar  el pedido de la mesa "+mesa.getIdmesa ()+"?");
                    builder.setView(input);

                    final int finalPositionFuente = positionFuente;
                    final int finalPositionFuente1 = positionFuente;
                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(final DialogInterface dialog, int which)
                        {
                            final ProgressDialog loading = ProgressDialog.show(getContext (),"Creando pedido...","Espere por favor...",false,false);
                            Map<String, String> params = new HashMap<String, String>();
                            params.put ("eliminarUnPedido", "true");
                            params.put ("idmesa", mesa.getIdmesa ()+"");
                            JSONObject parameters = new JSONObject (params);
                            String url = "https://openm.co/consultas/pedidos.php";
                            jsonRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject> ()
                            {
                                @Override
                                public void onResponse(JSONObject response)
                                {
                                    loading.dismiss ();
                                    Toast.makeText (getContext (), "Pedido elminado de la "+ mesa.getIdmesa (), Toast.LENGTH_SHORT).show ();
                                    adaptadorListaMesaDesocupada.getList ().add (mesa);
                                    adaptadorListaMesa.getList ().remove (finalPositionFuente1);
                                    adaptadorListaMesaDesocupada.notifyDataSetChanged ();
                                    adaptadorListaMesa.notifyDataSetChanged ();

                                    dialog.cancel();
                                }
                            }, new Response.ErrorListener ()
                            {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    error.printStackTrace ();
                                    loading.dismiss ();
                                }
                            });
                            int socketTimeout = 0;
                            requestQueue.add (jsonRequest);
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    });
                    builder.show ();

                } else if ((RecyclerView.getAdapter () instanceof AdaptadorListaMesaDesocupada))
                {
                    final AdaptadorListaMesaDesocupada adaptadorListaMesaDesocupada = (AdaptadorListaMesaDesocupada) RecyclerView.getAdapter ();
                    final Mesa  mesa = adaptadorListaMesaDesocupada.getList ().get (positionFuente);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext ());
                    builder.setTitle("Title");
                    final TextView input = new TextView(getContext ());
                    input.setText ("¿Desea realizar un pedido en esta mesa?");
                    builder.setView(input);

                    final int finalPositionFuente = positionFuente;
                    builder.setPositiveButton("Si", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(final DialogInterface dialog, int which)
                        {
                            final ProgressDialog loading = ProgressDialog.show(getContext (),"Creando pedido...","Espere por favor...",false,false);
                            Map<String, String> params = new HashMap<String, String>();
                            params.put ("crearPedido", "true");
                            params.put ("idmesa", mesa.getIdmesa ()+"");
                            params.put ("idempleado", idEmpleados+"");
                            JSONObject parameters = new JSONObject (params);
                            String url = "https://openm.co/consultas/pedidos.php";
                            jsonRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject> ()
                            {
                                @Override
                                public void onResponse(JSONObject response)
                                {
                                    loading.dismiss ();
                                    Toast.makeText (getContext (), "Pedido creado en la mesa "+ mesa.getIdmesa (), Toast.LENGTH_SHORT).show ();
                                    adaptadorListaMesa.getList ().add (mesa);
                                    adaptadorListaMesaDesocupada.getList ().remove (finalPositionFuente);
                                    adaptadorListaMesaDesocupada.notifyDataSetChanged ();
                                    adaptadorListaMesa.notifyDataSetChanged ();
                                    dialog.cancel();
                                }
                            }, new Response.ErrorListener ()
                            {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    error.printStackTrace ();
                                    loading.dismiss ();
                                }
                            });
                            int socketTimeout = 0;
                            requestQueue.add (jsonRequest);
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.cancel();
                        }
                    });
                    builder.show ();
                }
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                v.setBackgroundColor(0);
                break;
            default:
                break;
        }
        View vw = (View) event.getLocalState();
        vw.setVisibility(View.VISIBLE);
        return true;
    }

    private void recuperarPreferencias()
    {
        SharedPreferences preferences= getContext ().getSharedPreferences("preferenciasLogin", Context.MODE_PRIVATE);
        boolean sesion=preferences.getBoolean("sesion",false);
        if(sesion)
        {
            this.idEmpleados=preferences.getString("idEmpleados", "No hay nada");

        }
    }
}