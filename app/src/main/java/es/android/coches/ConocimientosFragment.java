package es.android.coches;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import es.android.coches.databinding.FragmentConocimientosBinding;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ConocimientosFragment extends Fragment {

    private FragmentConocimientosBinding binding;

    List<Pregunta> todasLasPreguntas;
    List<String> todasLasRespuestas;

    List<Pregunta> preguntas;
    int respuestaCorrecta;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(todasLasPreguntas == null) {
            try {
                generarPreguntas("coches");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Collections.shuffle(todasLasPreguntas);
        preguntas = new ArrayList<>(todasLasPreguntas);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentConocimientosBinding.inflate(inflater,container,false);

        presentarPregunta();
        JSONObject objJson = new JSONObject();
        JSONObject objJson2 = new JSONObject();
        AtomicInteger num_puntos= new AtomicInteger();
        int ultima_puntuacion=0;


        if (fileExists(getContext(),"puntuacion")){

            try {
                FileInputStream fis = getContext().openFileInput("puntuacion");
                String fichero = leerArchivo(fis);
                objJson2 = new JSONObject(fichero);
                //finalObjJson=objJson2;



            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject finalObjJson = objJson2;

        binding.botonRespuesta.setOnClickListener(v -> {
            int seleccionado = binding.radioGroup.getCheckedRadioButtonId();

            CharSequence mensaje = seleccionado == respuestaCorrecta ? "¡Acertaste!" : "Fallaste";
            Snackbar.make(v, mensaje, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Siguiente", v1 -> presentarPregunta())
                    .show();

            v.setEnabled(false);

                if (mensaje.equals("¡Acertaste!")){
                    num_puntos.getAndIncrement();
                }
                try {
                        objJson.put("ultima_puntuacion", num_puntos.get());
                        objJson.put("puntuacion_maxima",ultima_puntuacion);
                       //String jsonSTring= objJson.toString();

                    if (objJson.getInt("ultima_puntuacion")> finalObjJson.getInt("puntuacion_maxima")){

                        objJson.put("puntuacion_maxima",num_puntos);

                    }

                } catch (JSONException e) {
                        e.printStackTrace();
                }

            try {

                if (preguntas.size()==0) {

                    presentarPregunta();

                    if ((objJson.getInt("puntuacion_maxima") > (finalObjJson.getInt("puntuacion_maxima")))) {

                        int puntuacion = objJson.getInt("ultima_puntuacion");
                        Snackbar.make(v, "¡Has batido tu récord de aciertos! Has alcanzado"+puntuacion+" puntos", Snackbar.LENGTH_INDEFINITE).show();
                       // Snackbar mySnackbar = Snackbar.make(v, "“¡Has batido tu récord de aciertos! Has alcanzado" +
                       //         puntuacion, 2);

                    } else {
                        int puntuacion = objJson.getInt("ultima_puntuacion");
                        //Snackbar mySnackbar = Snackbar.make(v, "“Has conseguido " + puntuacion + " puntos”", 2);
                        Snackbar.make(v, "“Has conseguido "+puntuacion+" puntos”", Snackbar.LENGTH_INDEFINITE).show();
                    }
                }
            }catch (JSONException e) {
                    e.printStackTrace();

                String jsonSTring= objJson.toString();
                salvarFichero("puntuacion" , jsonSTring);

            }

        });



        return binding.getRoot();
    }

    private List<String> generarRespuestasPosibles(String respuestaCorrecta) {
        List<String> respuestasPosibles = new ArrayList<>();
        respuestasPosibles.add(respuestaCorrecta);

        List<String> respuestasIncorrectas = new ArrayList<>(todasLasRespuestas);
        respuestasIncorrectas.remove(respuestaCorrecta);

        for(int i=0; i<binding.radioGroup.getChildCount()-1; i++) {
            int indiceRespuesta = new Random().nextInt(respuestasIncorrectas.size());
            respuestasPosibles.add(respuestasIncorrectas.remove(indiceRespuesta));

        }
        Collections.shuffle(respuestasPosibles);
        return respuestasPosibles;
    }

    private void presentarPregunta() {
        if(preguntas.size() > 0) {
            binding.botonRespuesta.setEnabled(true);

            int pregunta = new Random().nextInt(preguntas.size());

            Pregunta preguntaActual = preguntas.remove(pregunta);
            preguntaActual.setRespuetas(generarRespuestasPosibles(preguntaActual.respuestaCorrecta));

            InputStream coche = null;
            //coche = getContext().getAssets().open(preguntaActual.foto);
            int foto = getResources().getIdentifier(preguntaActual.foto, "raw",
                    getContext().getPackageName());
            coche = getContext().getResources().openRawResource(foto);
            binding.imagencoche.setImageBitmap(BitmapFactory.decodeStream(coche));

            binding.radioGroup.clearCheck();
            for (int i = 0; i < binding.radioGroup.getChildCount(); i++) {
                RadioButton radio = (RadioButton) binding.radioGroup.getChildAt(i);
                //radio.setChecked(false);
                CharSequence respuesta = preguntaActual.getRespuetas().get(i);
                if (respuesta.equals(preguntaActual.respuestaCorrecta))
                    respuestaCorrecta = radio.getId();

                radio.setText(respuesta);
            }
        } else {
            binding.imagencoche.setVisibility(View.GONE);
            binding.radioGroup.setVisibility(View.GONE);
            binding.botonRespuesta.setVisibility(View.GONE);
            binding.textView.setText("¡Fin!");

        }
    }


    class Pregunta {
        private String nombre;
        private String foto;
        private String respuestaCorrecta;
        private List<String> respuetas;

        public Pregunta(String nombre, String foto) {
            this.nombre = nombre;
            this.foto = foto;
            this.respuestaCorrecta = nombre;
        }

        public List<String> getRespuetas() {
            return respuetas;
        }

        public void setRespuetas(List<String> respuetas) {
            this.respuetas = respuetas;
        }
    }

    private Document leerXML(String fichero) throws Exception {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder constructor = factory.newDocumentBuilder();
        //Document doc = constructor.parse(getContext().getAssets().open(fichero));
        int idRecurso = getResources().getIdentifier(fichero, "raw",
                getContext().getPackageName());
        InputStream Recursoraw = getResources().openRawResource(idRecurso);
        Document doc = constructor.parse(Recursoraw);
        doc.getDocumentElement().normalize();
        return doc;
    }

    private void generarPreguntas(String fichero) throws Exception {
        todasLasPreguntas = new LinkedList<>();
        todasLasRespuestas = new LinkedList<>();
        Document doc = leerXML(fichero);
        Element documentElement = doc.getDocumentElement();
        NodeList paises = documentElement.getChildNodes();
        for(int i=0; i<paises.getLength(); i++) {
            if(paises.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element pais = (Element) paises.item(i);
                //String nombre = pais.getAttribute("nombre");
                String nombre = pais.getElementsByTagName("nombre").item(0).getTextContent();
                String foto = pais.getElementsByTagName("coche").item(0).getTextContent();
                todasLasPreguntas.add(new Pregunta(nombre, foto));
                todasLasRespuestas.add(nombre);

            }
        }
    }

    private void salvarFichero(String fichero, String texto) {
        FileOutputStream fos;
        try {
            fos = getContext().openFileOutput(fichero, Context.MODE_PRIVATE);
            fos.write(texto.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if (file == null || !file.exists()) {
            return false;
        }
        return true;
    }

    public String leerArchivo (FileInputStream fis) throws IOException {

        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String fileContent = br.readLine();
        while (fileContent != null) {
            sb.append(fileContent);
            fileContent = br.readLine();

        }
        return sb.toString();
    }
}