package com.example.restaurantemeseros.interfaz;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.restaurantemeseros.adaptador.Servidor;
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
public class MesasFragment extends BottomSheetDialogFragment  implements View.OnDragListener {

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
    private Mesa mesa;
    private Activity actividad;
    private InterfazFragamen interfazFragamen;
    ImageButton agregarMesa,animacionMesas;
    Dialog mDialog;
    TextView numeroMesa;
    private Timer timer;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private String idEmpleados;

    public MesasFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new TimerTask()

        {
            @Override
            public void run()
            {
                buscarlista ();
                //  buscarMesaDesocupada ();
                System.out.println ("A Kiss after 5 seconds");
            }
        },1,4000);
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
        this.adaptadorListaMesa = new AdaptadorListaMesa (getContext (), this.mesas,this);
        this.listaMesas.setLayoutManager (new LinearLayoutManager (getContext ()));
        this.mesasDesocupadas.setLayoutManager (new GridLayoutManager (getContext (), 1));
        this.adaptadorListaMesaDesocupada = new AdaptadorListaMesaDesocupada (getContext (), this.mesasDes,this);
        this.mesasDesocupadas.setAdapter (adaptadorListaMesaDesocupada);
        this.listaMesas.setAdapter(adaptadorListaMesa);
        this.listaMesas.setLayoutManager (new GridLayoutManager (getContext (), 1));
        recuperarPreferencias();



/*
        new Timer ().scheduleAtFixedRate(new TimerTask ()
        {
            @Override
            public void run()
            {

                System.out.println ("A Kiss after 5 seconds");
            }
        },1,5000);*/


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
                mesa = mesas.get (listaMesas.getChildAdapterPosition (v));

                Toast.makeText(getContext(), "Has seleccionado la mesa "+mesa.getIdmesa (), Toast.LENGTH_LONG).show();
                Bundle bundleEnvio = new Bundle ();
                bundleEnvio.putSerializable ("mesa", mesa);
                getParentFragmentManager ().setFragmentResult ("key", bundleEnvio);
              //  getParentFragmentManager().popBackStack();

            }
        });

        this. mesasDesocupadas.setOnDragListener(this);
        this.listaMesas.setOnDragListener(this);


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
        String url = Servidor.HOST+"/consultas/mesas.php";
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

    /*public void buscarMesaDesocupada()
    {
        mesasDesAux.clear ();
        Map<String, String> params = new HashMap<String, String> ();
        params.put ("buscarMesasDesocupadas", "Mes");
        JSONObject parameters = new JSONObject (params);
        String url = Servidor.HOST+"/consultas/pedidos.php";

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

                    if (mesasDesAux.size () != cantMesas && !mesasDesAux.isEmpty ()) {
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
    }*/

    public void buscarlista()
    {

        mesasDesAux.clear ();
        mesasAux.clear();
        Map<String,String> params= new HashMap<String, String> ();
        params.put("buscarMesas","Mes");
        JSONObject parameters = new JSONObject(params);
        String url=Servidor.HOST +"/consultas/pedidos.php";
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
                        if ( datos.length()-mesasDesAux.size ()!= cantMesas)
                        {
                            for (int i = 0; i < datos.length(); i++)
                            {
                                JSONObject mesa = datos.getJSONObject(i);
                                int id=mesa.getInt ("idmesas");
                                String numero=mesa.getString("numero");
                                String codigoQR=mesa.getString("codigoQR");
                                String estado=mesa.getString("estado");
                                Mesa m=new Mesa( id,  numero,  codigoQR, estado);
                                if(estado.equals("Ocupada")){
                                    mesasAux.add(m);
                                }else{
                                    mesasDesAux.add (m);
                                }
                            }
                            mesasDes.clear ();
                            mesas.clear();
                            mesas.addAll(mesasAux);
                            mesasDes.addAll (mesasDesAux);
                            adaptadorListaMesaDesocupada.notifyDataSetChanged ();
                            adaptadorListaMesa.notifyDataSetChanged ();
                            cantMesas = mesas.size ();
                        }

                      if (mesa instanceof  Mesa){
                            Bundle bundleEnvio = new Bundle ();
                            bundleEnvio.putSerializable ("mesa", mesa);
                            getParentFragmentManager ().setFragmentResult ("key", bundleEnvio);

                        }  else
                        {
                            Bundle bundleEnvio = new Bundle ();
                            bundleEnvio.putSerializable ("mesa", null);
                            getParentFragmentManager ().setFragmentResult ("key", bundleEnvio);
                        }
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
        LayoutInflater inflater= getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_agregar_mesa,null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show ();



        final TextView input = view.findViewById(R.id.txtAgregarMesa);
        input.setText ("¿Desea realizar un pedido en la"+mesa.getNumero()+"?");
        Button botonSiAgregar =view.findViewById(R.id.botonSiAgregar);
        Button botonNoAgregar =view.findViewById(R.id.botonNoAgregar);

       // final int finalPositionFuente = positionFuente;
        botonSiAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog loading = ProgressDialog.show(getContext (),"Creando pedido...","Espere por favor...",false,false);
                Map<String, String> params = new HashMap<String, String>();
                params.put ("crearPedido", "true");
                params.put ("idmesa", mesa.getIdmesa ()+"");
                params.put ("idempleado", idEmpleados+"");
                JSONObject parameters = new JSONObject (params);
                String url = Servidor.HOST+"/consultas/pedidos.php";
                jsonRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject> ()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        loading.dismiss ();
                        Toast.makeText (getContext (), "Pedido creado en la mesa "+ mesa.getIdmesa (), Toast.LENGTH_SHORT).show ();
                        adaptadorListaMesa.getList ().add (mesa);
                       // adaptadorListaMesaDesocupada.getList ().remove (finalPositionFuente);
                        adaptadorListaMesaDesocupada.notifyDataSetChanged ();
                        adaptadorListaMesa.notifyDataSetChanged ();
                        dialog.cancel();



                     //   getContext().getParentFragmentManager().popBackStack();
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


        botonNoAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });


        dialog.show ();
    }


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
                // v.setBackgroundColor(Color.YELLOW);
                break;
            case DragEvent.ACTION_DROP:
                int positionFuente = -1, posicionDestion=-1;
                View viewSource = (View) event.getLocalState();
                RecyclerView recyclerView= (RecyclerView) viewSource.getParent ();
                positionFuente = (int) viewSource.getTag ();
                if((v instanceof  RecyclerView)
                        && (recyclerView instanceof  RecyclerView)
                        &&  v.getId() == R.id.listaMesasDesocupadas
                        && recyclerView.getId ()== R.id.listaMesas)
                {

                    positionFuente = (int) viewSource.getTag ();

                    //  positionFuente = (int) viewSource.getTag ();

                    final AdaptadorListaMesa adaptadorListaMesa= (AdaptadorListaMesa) recyclerView.getAdapter();
                    final Mesa mesa = adaptadorListaMesa.getList().get(positionFuente);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    LayoutInflater inflater = getLayoutInflater();
                    View view = inflater.inflate(R.layout.dialog_eliminar_mesa, null);
                    builder.setView(view);
                    final AlertDialog dialog = builder.create();
                    dialog.show();

                    final TextView input = view.findViewById(R.id.txtEliminarMesa);
                    Button botonSi = view.findViewById(R.id.btnSiEliminar);
                    Button botonNo = view.findViewById(R.id.btnEliminarNo);
                    input.setText("¿Desea eliminar  el pedido de la mesa " + mesa.getIdmesa() + "?");
                    builder.setView(input);


                    botonSi.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final ProgressDialog loading = ProgressDialog.show(getContext(), "Eliminando pedido...", "Espere por favor...", false, false);
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("eliminarUnPedido", "true");
                            params.put("idmesa", mesa.getIdmesa() + "");
                            JSONObject parameters = new JSONObject(params);
                            String url = Servidor.HOST +"/consultas/pedidos.php";
                            jsonRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response)
                                {
                                    loading.dismiss();
                                    Toast.makeText(getContext(), "Pedido elminado de la " + mesa.getIdmesa(), Toast.LENGTH_SHORT).show();
                                    adaptadorListaMesa.notifyDataSetChanged ();
                                    Bundle bundleEnvio = new Bundle ();
                                    bundleEnvio.putSerializable ("mesa", null);
                                    getParentFragmentManager ().setFragmentResult ("key", bundleEnvio);
                                    dialog.cancel();
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    error.printStackTrace();
                                    loading.dismiss();
                                }
                            });
                            int socketTimeout = 0;
                            requestQueue.add(jsonRequest);
                        }




                    });

                    botonNo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();

                }/*else if((RecyclerView.getAdapter () instanceof AdaptadorListaMesaDesocupada)&&v.getId ()== R.id.itemMesaOcupada)
                {
                    final Mesa mesaOrigen = adaptadorListaMesa.getList ().get (positionFuente);
                    //final Mesa mesaDestino = adaptadorListaMesaDesocupada.getList ().get (posicionDestion);
                   // final ProgressDialog loading = ProgressDialog.show(getContext (),"Creando pedido...","Espere por favor...",false,false);
                    Map<String, String> params = new HashMap<String, String>();
                    params.put ("cambiarMesaPedio", "true");
                    Toast.makeText (getContext (), "Pedido elminado de la ", Toast.LENGTH_SHORT).show ();
                    params.put ("idmesaOrigen", mesaOrigen.getIdmesa ()+"");
                  //  params.put ("idmesaDestion", mesaDestino.getIdmesa ()+"");
                    JSONObject parameters = new JSONObject (params);
                    String url = "http://192.168.1.27/consultas/pedidos.php";
                    jsonRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject> ()
                    {
                        @Override
                        public void onResponse(JSONObject response)
                        {
                            /*loading.dismiss ();
                            Toast.makeText (getContext (), "Pedido elminado de la "+ mesa.getIdmesa (), Toast.LENGTH_SHORT).show ();
                            adaptadorListaMesaDesocupada.getList ().add (mesa);
                            adaptadorListaMesa.getList ().remove (finalPositionFuente1);
                            adaptadorListaMesaDesocupada.notifyDataSetChanged ();
                            adaptadorListaMesa.notifyDataSetChanged ();
                            Bundle bundleEnvio = new Bundle ();

                            bundleEnvio.putSerializable ("mesa", null);
                            getParentFragmentManager ().setFragmentResult ("key", bundleEnvio);

                            dialog.cancel();
                        }
                    }, new Response.ErrorListener ()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace ();
                            //loading.dismiss ();
                        }
                    });
                    int socketTimeout = 0;
                    //requestQueue.add (jsonRequest);

                }*/ else if((v instanceof  RecyclerView)
                        && (recyclerView instanceof  RecyclerView)
                        &&  v.getId() == R.id.listaMesas
                        && recyclerView.getId ()== R.id.listaMesasDesocupadas)

                {

                    positionFuente = (int) viewSource.getTag ();

                    // positionFuente = (int) viewSource.getTag ();

                    final AdaptadorListaMesaDesocupada adaptadorListaMesaDesocupada= (AdaptadorListaMesaDesocupada) recyclerView.getAdapter();
                    mesa =  adaptadorListaMesaDesocupada.getList().get(positionFuente);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext ());
                    LayoutInflater inflater= getLayoutInflater();
                    View view = inflater.inflate(R.layout.dialog_agregar_mesa,null);
                    builder.setView(view);
                    final AlertDialog dialog = builder.create();
                    dialog.show ();


                    final TextView input = view.findViewById(R.id.txtAgregarMesa);
                    input.setText ("¿Desea realizar un pedido en la "+mesa.getNumero()+"?");
                    Button botonSiAgregar =view.findViewById(R.id.botonSiAgregar);
                    Button botonNoAgregar =view.findViewById(R.id.botonNoAgregar);

                    botonSiAgregar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final ProgressDialog loading = ProgressDialog.show(getContext (),"Creando pedido...","Espere por favor...",false,false);
                            Map<String, String> params = new HashMap<String, String>();
                            params.put ("crearPedido", "true");
                            params.put ("idmesa", mesa.getIdmesa ()+"");
                            params.put ("idempleado", idEmpleados+"");
                            JSONObject parameters = new JSONObject (params);
                            String url = Servidor.HOST +"/consultas/pedidos.php";
                            jsonRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject> ()
                            {
                                @Override
                                public void onResponse(JSONObject response)
                                {
                                    loading.dismiss ();
                                    Bundle bundleEnvio = new Bundle ();
                                    bundleEnvio.putSerializable ("mesa", mesa);
                                    getParentFragmentManager ().setFragmentResult ("key", bundleEnvio);
                                    adaptadorListaMesaDesocupada.notifyDataSetChanged ();
                                    Toast.makeText (getContext (), "Pedido creado en la mesa "+ mesa.getIdmesa (), Toast.LENGTH_SHORT).show ();
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
                            requestQueue.add (jsonRequest);
                        }
                    });
                    botonNoAgregar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });


                    dialog.show ();

                }
                onStart();
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(this.timer != null){
            this.timer.cancel();
        }
    }
}
