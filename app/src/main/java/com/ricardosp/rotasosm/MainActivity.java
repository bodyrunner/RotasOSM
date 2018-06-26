package com.ricardosp.rotasosm;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSAO_REQUERIDA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                String[] permissoes = {Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissoes, PERMISSAO_REQUERIDA);
            }
        }

        //Pega o mapa adicionada no arquivo activity_main.xml
        MapView mapa = (MapView) findViewById(R.id.mapaId);
//Fonte de imagens
        mapa.setTileSource(TileSourceFactory.MAPNIK);

//Cria um ponto de referência com base na latitude e longitude
        GeoPoint pontoInicial = new GeoPoint(-23.601090, -48.051361);

        IMapController mapController = mapa.getController();
//Faz zoom no mapa
        mapController.setZoom(15);
//Centraliza o mapa no ponto de referência
        mapController.setCenter(pontoInicial);

//Cria um marcador no mapa
        Marker startMarker = new Marker(mapa);
        startMarker.setPosition(pontoInicial);
        startMarker.setTitle("Ponto Inicial");
//Posição do ícone
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapa.getOverlays().add(startMarker);

        //Matriz com pontos geográficos(latitudes e longitudes) por onde a rota deve ser traçada
//Utilizando a API do Google Maps, nós só conseguimos desenhar uma rota com até 10 pontos
//Com a API do OpemStreetMap o números de pontos é ilimitado
        double matrizPontos[][] = {
                {-23.601228, -48.051345},
                {-23.601154, -48.050830},
                {-23.600677, -48.050916},
                {-23.598103, -48.050279},
                {-23.595485, -48.050661}};


//Cria uma lista de pontos (GeoPoint) pela latitude e longitude
        ArrayList<GeoPoint> pontos = new ArrayList<>();
        for (double[] array : matrizPontos) {
            pontos.add(new GeoPoint(array[0], array[1]));
        }


//Cria o objeto gerenciador de rotas
        RoadManager roadManager = new OSRMRoadManager(this);
        Road road = null;
        try {
            //Chama a classe(DesenhaRotaTask) que executa tarefas assincronas, passa os pontos de referências
            //para a classe DesenhaRotaTask traçar a rota
            road = new DesenhaRotaTask(pontos, roadManager).execute(roadManager).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

//Desenha a rota
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
//Adiciona a rota no mapa
        mapa.getOverlays().add(roadOverlay);
//atualiza o mapa
        mapa.invalidate();

        GeoPoint pontoFinal = new GeoPoint(-23.595485, -48.050661);
        Marker endMarker = new Marker(mapa);
        endMarker.setPosition(pontoFinal);
        endMarker.setTitle("Ponto Final");
//Posição do ícone
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapa.getOverlays().add(endMarker);




    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                // Se a solicitação de permissão foi cancelada o array vem vazio.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permissão cedida, recria a activity para carregar o mapa, só será executado uma vez
                    this.recreate();

                }

            }
        }
    }
}
