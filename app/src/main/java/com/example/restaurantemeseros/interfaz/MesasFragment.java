package com.example.restaurantemeseros.interfaz;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.restaurantemeseros.adaptador.VolleySingleton;
import com.example.restaurantemeseros.mundo.Mesa;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MesasFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MesasFragment extends BottomSheetDialogFragment {

    protected RequestQueue requestQueue;
    protected JsonRequest jsonRequest;
    private SearchView buscarMesa;
    private RecyclerView listaMesas, mesasDesocupadas;
    private AdaptadorListaMesa adaptadorListaMesa;

    private ArrayList<Mesa> mesasDes;
    private ArrayList<Mesa> mesasDesAux;
    private ArrayList<Mesa> mesas;
    private ArrayList<Mesa> mesasAux;
    private ProgressDialog dialog;
    private int cantMesas;
    private Activity actividad;
    private InterfazFragamen interfazFragamen;
    ImageButton agregarMesa,animacionMesas;
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
        recuperarPreferencias();
        this.agregarMesa = v.findViewById (R.id.AgregarPedido);
        this.buscarMesa = v.findViewById (R.id.searchBuscarPlato);
        this.listaMesas = v.findViewById (R.id.listaMesas);
        animacionMesas=v.findViewById (R.id.imageButton);
       // this.mesasDesocupadas = v.findViewById (R.id.listaMesasDesocupadas);
        this.requestQueue = VolleySingleton.getInstance(getContext()).getRequestQueue();
        this.mesasDes = new ArrayList<Mesa> ();
        this.mesasDesAux = new ArrayList<Mesa> ();
        this.mesas = new ArrayList<Mesa> ();
        this.mesasAux = new ArrayList<Mesa> ();
        this.adaptadorListaMesa = new AdaptadorListaMesa(getContext (), this.mesas);
        this.listaMesas.setLayoutManager (new LinearLayoutManager(getContext ()));

        this.listaMesas.setAdapter(adaptadorListaMesa);
        this.listaMesas.setLayoutManager (new GridLayoutManager(getContext (), 3));


/*
        new Timer ().scheduleAtFixedRate(new TimerTask ()
        {
            @Override
            public void run()
            {

                System.out.println ("A Kiss after 5 seconds");
            }
        },1,5000);*/
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

                replegarMesa();
                Toast.makeText(getContext(), "Has seleccionado la mesa "+mesa.getIdmesa (), Toast.LENGTH_LONG).show();
                Bundle bundleEnvio = new Bundle ();
                bundleEnvio.putSerializable ("mesa", mesa);
                getParentFragmentManager ().setFragmentResult ("key", bundleEnvio);
                getParentFragmentManager().popBackStack();

            }
        });

        agregarMesa.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v)
            {
                IntentIntegrator integrator = IntentIntegrator.forSupportFragment(MesasFragment.this);
                integrator.setOrientationLocked(false);
                integrator.setPrompt("Scan QR code");
                integrator.setBeepEnabled(false);
                integrator.initiateScan();
            }
        });
        mDialog = new Dialog (getContext ());
        return v;

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null)
        {

            if(result.getContents() == null)
            {
                Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_LONG).show();
            } else
            {
                String datos = result.getContents().trim ();
                System.out.println (datos.replace ("tel:",""));
                buscarMesa(datos.replace ("tel:",""));
            }
        }
    }
    public void buscarMesa(String idmesa)
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put ("buscarUnaMesa", idmesa);
        JSONObject parameters = new JSONObject (params);
        String url = "https://openm.co/consultas/mesas.php";
        final ProgressDialog loading = ProgressDialog.show(getContext (),"Buscando mesa...","Espere por favor...",false,false);

        jsonRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject> ()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                loading.dismiss ();
                try
                {
                    JSONArray datos = response.getJSONArray ("datos");

                    if (datos.getJSONObject (0).getString ("estado").equals ("Desocupada"))
                    {
                        JSONObject mesa = datos.getJSONObject (0);
                        String estado = mesa.getString ("estado");
                        int id = mesa.getInt ("idmesas");
                        String numero = mesa.getString ("numero");
                        String codigoQR = mesa.getString ("codigoQR");
                        Mesa m = new Mesa (id, numero, codigoQR, estado);
                        crearPedido(m);
                    }else
                    {
                        Toast.makeText(getContext(), "La mesa esta ocupada", Toast.LENGTH_LONG).show();
                    }
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

        jsonRequest.setRetryPolicy (new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add (jsonRequest);
    }

    public void buscarMesaDesocupada()
    {
        mesasDesAux.clear ();
        Map<String, String> params = new HashMap<String, String>();
        params.put ("buscarMesasDesocupadas", "Mes");
        JSONObject parameters = new JSONObject (params);
        String url = "https://openm.co/consultas/pedidos.php";

        jsonRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject> () {
            @Override
            public void onResponse(JSONObject response)
            {
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
                        mesasDes.addAll (mesasDesAux);
                        cantMesas = mesasDes.size ();
                    }


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
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonRequest.setRetryPolicy (policy);
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
    private void crearPedido(Mesa mesa)
    {
        final String[] miObservacion = {""};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext ());
        builder.setTitle("Title");

        final TextView input = new EditText(getContext ());
        input.setText ("¿Desea realizar un pedido en esta mesa?");
        builder.setView(input);
        final ProgressDialog loading = ProgressDialog.show(getContext (),"Creando pedido...","Espere por favor...",false,false);

        builder.setPositiveButton("Si", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
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
                        mesasAux.add(mesa);
                        loading.dismiss ();
                        Toast.makeText (getContext (), "Pedido creado", Toast.LENGTH_SHORT).show ();
                        replegarMesa();
                        Bundle bundleEnvio = new Bundle ();
                        bundleEnvio.putSerializable ("mesa", mesa);
                        getParentFragmentManager ().setFragmentResult ("key", bundleEnvio);
                        dialog.cancel();
                    }
                }, new Response.ErrorListener () {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace ();
                    }
                });
                int socketTimeout = 0;
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                jsonRequest.setRetryPolicy (policy);
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
    private void recuperarPreferencias()
    {
        SharedPreferences preferences= getContext ().getSharedPreferences("preferenciasLogin", Context.MODE_PRIVATE);
        boolean sesion=preferences.getBoolean("sesion",false);
        if(sesion)
        {
            this.idEmpleados=preferences.getString("idEmpleados", "No hay nada");

        }
    }
    private void replegarMesa()
    {/*
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            getActivity ().onBackPressed();
        }*/
       // getFragmentManager().beginTransaction().hide (MesasFragment.this).commit();
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
}
