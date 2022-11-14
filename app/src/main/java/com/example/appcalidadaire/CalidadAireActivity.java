package com.example.appcalidadaire;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CalidadAireActivity extends AppCompatActivity {

    EditText etLongitud, etLatitud;
    Button btnConsultar;
    ImageButton btnColorResultado;
    TextView tvResultado, tvDescripcionResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calidad_aire);
        conectar();
        escribirDatosPorDefecto();
        btnConsultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double longitud = Double.parseDouble(etLongitud.getText().toString());
                double latitud =  Double.parseDouble(etLatitud.getText().toString());
                ArrayList<Estacion> estaciones = leerEstaciones();
                double[] matrizb = crearMatrizb(estaciones);
                double[][] matrizA = calcularMatrizInterpolacion(estaciones);
                double[] lambdas = metodoGauss(matrizA, matrizb);
                double resultado = interpolacion(longitud, latitud, lambdas, estaciones);
                DecimalFormat df = new DecimalFormat("#.0");
                tvResultado.setText(df.format(resultado)+"");
                cambiarColorNube(resultado);
            }
        });
    }

    private void cambiarColorNube(double resultado){
        if(resultado >= 0D && resultado <= 12D){
            btnColorResultado.setColorFilter(getApplicationContext().getResources().getColor(R.color.verde_siata));
            tvDescripcionResultado.setText("Buena");
        }else if(resultado >= 13D && resultado <= 37D){
            btnColorResultado.setColorFilter(getApplicationContext().getResources().getColor(R.color.amarillo_siata));
            tvDescripcionResultado.setText("Moderada");
        }else if(resultado >= 38D && resultado <= 55D){
            btnColorResultado.setColorFilter(getApplicationContext().getResources().getColor(R.color.naranja_siata));
            tvDescripcionResultado.setText("Dañina a la salud grupos sensibles");
        }else if(resultado >= 56D && resultado <= 150D){
            btnColorResultado.setColorFilter(getApplicationContext().getResources().getColor(R.color.rojo_siata));
            tvDescripcionResultado.setText("Dañina a la salud");
        }else{
            btnColorResultado.setColorFilter(getApplicationContext().getResources().getColor(R.color.morado_siata));
            tvDescripcionResultado.setText("Muy dañina a la salud");
        }
    }

    private double[] crearMatrizb(ArrayList<Estacion> estaciones){
        double[] pm = new double[estaciones.size()];
        for (int i = 0; i<pm.length; i++) {
            pm[i] = estaciones.get(i).getPm();
        }
        return pm;
    }

    private double interpolacion(double longitud, double latitud, double[] lambdas,
                               ArrayList<Estacion> estaciones){

        int tamanio = estaciones.size();
        double calidadCalculada = 0;
        for (int i = 0; i < tamanio; i++) {
            Estacion estacionFinal = estaciones.get(i);
            Double longitudFinal = estacionFinal.getLongitud();
            Double latitudFinal = estacionFinal.getLatitud();
            double distancia = distanciaEntreDosPuntos(longitud, latitud, longitudFinal, latitudFinal);
            double valorPhi = Math.sqrt((1 + Math.pow(distancia, 2)));
            double valorFinal = lambdas[i]*valorPhi;
            calidadCalculada += valorFinal;
        }
        return calidadCalculada;
    }
    
    private double[][] calcularMatrizInterpolacion(ArrayList<Estacion> estaciones){
        int tamanio = estaciones.size();
        double[][] matrizInterpolacion = new double[tamanio][tamanio];

        for (int i = 0; i < tamanio; i++) {

            Estacion estacionInicio = estaciones.get(i);
            Double longitud1 = estacionInicio.getLongitud();
            Double latitud1 = estacionInicio.getLatitud();

            for (int j = 0; j < tamanio; j++) {

                Estacion estacionFin = estaciones.get(j);
                Double longitud2 = estacionFin.getLongitud();
                Double latitud2 = estacionFin.getLatitud();

                double distancia = distanciaEntreDosPuntos(longitud1, latitud1, longitud2, latitud2);
                double valorPhi = Math.sqrt((1 + Math.pow(distancia, 2)));
                matrizInterpolacion[i][j] = valorPhi;
            }
        }
        return matrizInterpolacion;
    }

    private double distanciaEntreDosPuntos(double x1, double y1, double x2, double y2){
        double distanciaX = Math.pow((x1 - x2), 2);
        double distanciaY = Math.pow((y1 - y2), 2);
        return Math.sqrt((distanciaX + distanciaY));
    }

    private ArrayList<Estacion> leerEstaciones(){
        ArrayList<Estacion> estaciones = new ArrayList<>();
        try {
            DBHelper helper = new DBHelper(this, "CalidadAire", null, 1);
            SQLiteDatabase db = helper.getWritableDatabase();
            String SQL = "select longitud, latitud, pm from Estaciones";

            Cursor c = db.rawQuery(SQL, null);
            if (c.moveToFirst()){
                do {
                    Estacion nuevaEstacion = new Estacion(c.getDouble(0), c.getDouble(1),
                            c.getDouble(2));
                    estaciones.add(nuevaEstacion);
                }while (c.moveToNext());
            }
            db.close();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Ocurrió un error: "+e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        return estaciones;
    }

    private void insertarEstacion(String nombreEstacion, String ciudad, Double longitud,
                                  Double latitud, Double pm){
        try {
            DBHelper helper = new DBHelper(this, "CalidadAire", null, 1);
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("nombre_estacion", nombreEstacion);
            cv.put("ciudad", ciudad);
            cv.put("longitud", longitud);
            cv.put("latitud", latitud);
            cv.put("pm", pm);
            db.insert("Estaciones", null, cv);
            db.close();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Ocurrió un error: "+e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private double[] metodoGauss(double[][] A, double[] b){
        double[][] matrizReducida = reducirMatriz(A,b);
        double[] lambdas = resolverMatriz(matrizReducida);
        return lambdas;
    }

    private double[] resolverMatriz(@NonNull double[][] matrizReducida){
        ArrayList<Double> lambdas = new ArrayList<>();
        lambdas.add(matrizReducida[matrizReducida.length - 1][matrizReducida[0].length - 1]);
        for(int i = matrizReducida.length-2; i > -1; i--){
            double ladoDerechoEcuacion = 0D;
            Integer contadorLambdas = 0;
            for(int j = matrizReducida[0].length-1; j > -1; j--){
                if(j == matrizReducida[0].length-1){
                    ladoDerechoEcuacion = matrizReducida[i][j];
                    continue;
                }
                if(!(matrizReducida[i][j] == 1.0)){
                    ladoDerechoEcuacion -= matrizReducida[i][j] * lambdas.get(contadorLambdas);
                    contadorLambdas++;
                }else{
                    lambdas.add(ladoDerechoEcuacion);
                    break;
                }
            }
        }
        double[] arrayLambdas = new double[lambdas.size()];
        for(int i = 0; i<arrayLambdas.length; i++){
            arrayLambdas[i] = lambdas.get(i);
        }
        return arrayLambdas;
    }

    private double[][] reducirMatriz(double[][] A, double[] b){
        double[][] matrizAumentada = new double[A.length][A[0].length + 1];
        for (int i = 0; i < A.length; i++){
            double[] fila = A[i];
            double[] nuevaFila = aumentarFila(fila, b[i]);
            matrizAumentada[i] = nuevaFila;
        }

        for (int j = 0; j < matrizAumentada[0].length; j++){
            if(!(j+1 == matrizAumentada[0].length)){
                double numeroPivote = matrizAumentada[j][j];
                for (int i = j+1; i < matrizAumentada.length; i++){
                    numeroPivote *= matrizAumentada[i][j] * -1;
                    matrizAumentada[i][j] += numeroPivote;
                }
            }
        }
        return matrizAumentada;
    }

    private double[] aumentarFila(double[] fila, double numeroAColocar){
        double[] nuevaFila = new double[fila.length + 1];
        for (int j = 0; j<nuevaFila.length; j++){
            if(j == fila.length){
                nuevaFila[j] = numeroAColocar;
                break;
            }
            nuevaFila[j] = fila[j];
        }
        return nuevaFila;
    }

    private void escribirDatosPorDefecto(){
        try {
            DBHelper helper = new DBHelper(this, "CalidadAire", null, 1);
            SQLiteDatabase db = helper.getWritableDatabase();
            String SQL = "select * from Estaciones";
            Cursor c = db.rawQuery(SQL, null);
            if (!c.moveToFirst()){
                insertarEstacion("Politecnico Colombiano Jaime Isaza Cadavid",
                        "Medellin", -75.57777, 6.20897, 18.0);
                insertarEstacion("Estación Tráfico Centro",
                        "Medellin", -75.56958, 6.25256, 23.0);
                insertarEstacion("Casa de Justicia Itagüí",
                        "Itagui", -75.59721, 6.18567, 13.0);
                insertarEstacion("Universidad San Buenaventura",
                        "Medellin", -75.56867, 6.33070, 14.0);
                insertarEstacion("I.E. Concejo Municipal de Itagüí",
                        "Itagui", -75.64436, 6.16850, 14.0);
                insertarEstacion("Tanques La Ye EPM",
                        "Medellin", -75.55064, 6.18254, 11.0);
                insertarEstacion("Estación Tráfico Sur",
                        "Medellin", -75.62749, 6.15231, 17.0);
                insertarEstacion("E U Joaquín Aristizabal",
                        "Caldas", -75.63776, 6.09308, 13.0);
                insertarEstacion("Hospital",
                        "La Estrella", -75.64417, 6.15553, 9.0);
                insertarEstacion("I.E. Pedro Octavio Amado",
                        "Medellin", -75.61060, 6.22189, 16.0);
                insertarEstacion("Planta de producción de agua potable EPM",
                        "Medellin", -75.54826, 6.25891, 13.0);
                insertarEstacion("Torre Social",
                        "Barbosa", -75.33040, 6.43696, 7.0);
                insertarEstacion("Ciudadela Educativa La Vida",
                        "Copacabana", -75.50475, 6.34536, 11.0);
                insertarEstacion("I.E Pedro Justo Berrio",
                        "Medellin", -75.61047, 6.23723, 17.0);
                insertarEstacion("I.E INEM sede Santa Catalina",
                        "Medellin", -75.56095, 6.19987, 9.0);
                insertarEstacion("Parque Biblioteca Fernando Botero",
                        "Medellin", -75.63643, 6.27785, 14.0);
                insertarEstacion("I.E Ciro Mendia",
                        "Medellin", -75.55552, 6.29048, 18.0);
                insertarEstacion("I.E. Fernando Vélez",
                        "Bello", -75.56780, 6.33755, 13.0);
                insertarEstacion("E.S.E. Santa Gertrudis",
                        "Envigado", -75.58197, 6.16868, 12.0);
                insertarEstacion("I.E. Rafael J. Mejía",
                        "Sabaneta", -75.62126, 6.14550, 12.0);
                insertarEstacion("Fiscalía General de la Nación",
                        "Medellin", -75.57371, 6.26879, 20.0);
                insertarEstacion("Tanques EPM",
                        "Girardota", -75.44831, 6.37325, 5.0);
                insertarEstacion("Estación Parque Biblioteca Tomás Carrasquilla",
                        "Medellin", -75.58305, 6.28500, 14.0);
            }
            db.close();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Ocurrió un error: "+e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void conectar(){
        etLongitud = findViewById(R.id.etLongitud);
        etLatitud = findViewById(R.id.etLatitud);
        btnConsultar = findViewById(R.id.btnConsultar);
        btnColorResultado = findViewById(R.id.btnColorResultado);
        tvResultado = findViewById(R.id.tvResultado);
        tvDescripcionResultado = findViewById(R.id.tvDescripcionResultado);
    }
}