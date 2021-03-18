package com.example.restaurantemeseros.interfaz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.example.restaurantemeseros.R;
import com.example.restaurantemeseros.mundo.Usuario;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {


    protected RequestQueue requestQueue;
    protected JsonRequest jsonRequest;
    EditText textUsuario,textContraseña;
    Button botonLogin;

    // variables de sesión de usuario
    String usuario, contrasena;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textUsuario=findViewById(R.id.textUsuario);
        textContraseña=findViewById(R.id.textContraseña);
        botonLogin=findViewById(R.id.botonInciarSesion);

        recuperarPreferencias();
        this.requestQueue= Volley.newRequestQueue( this);
        botonLogin.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                usuario=textUsuario.getText().toString();
                contrasena=textContraseña.getText().toString();
                if(!usuario.isEmpty() && !contrasena.isEmpty())
                {
                    Usuario miUsuario=null;
                    Map<String,String> parametros= new HashMap<String, String>();
                    parametros.put("usuario",usuario);
                    parametros.put("contrasena",contrasena);
                    JSONObject parameters = new JSONObject(parametros);

                    String url="https://openm.co/consultas/buscarUsuario.php";
                    jsonRequest=new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject> ()
                    {
                        @Override
                        public void onResponse(JSONObject response)
                        {
                            try
                            {
                                JSONObject datos = response.optJSONArray ("datos").getJSONObject (0);
                                guardarPreferencias(datos);
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            } catch (JSONException e) {
                                e.printStackTrace ();
                            }
                        }
                    }, new Response.ErrorListener ()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            Toast.makeText(LoginActivity.this, "El usuario no esta registrado o contraseña incorrecta", Toast.LENGTH_SHORT).show();
                        }
                    });
                    requestQueue.add(jsonRequest);

                }else
                {
                    Toast.makeText(LoginActivity.this,"No se permiten campos vacios", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    private void guardarPreferencias(JSONObject datos) throws JSONException
    {
        SharedPreferences preferences= getSharedPreferences("preferenciasLogin", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putString("idEmpleados",datos.getInt("idempleados")+"");
        editor.putString("identificacion",datos.getString("identificacion"));
        editor.putString("nombres", datos.getString("nombres"));
        editor.putString("apellidos",datos.getString("apellidos"));
        editor.putString("telefono",datos.getString("telefono"));
        editor.putString("contrasena",datos.getString("contrasena"));
        editor.putString("rol",datos.getString("rol"));
        editor.putString("cargo",datos.getString("cargo"));
        editor.putString("nick",datos.getString("nick"));
        editor.putBoolean("sesion",true);
        editor.commit();
    }

    private void recuperarPreferencias() {
        SharedPreferences preferences= getSharedPreferences("preferenciasLogin", Context.MODE_PRIVATE);
        textUsuario.setText(preferences.getString("nick",""));
        textContraseña.setText(preferences.getString("contrasena",""));
    }
}