package unirio.citytracksrt.controle;

import android.app.*;
import android.content.*;
import android.location.*;
import android.os.*;
import android.provider.*;
import android.support.annotation.*;
import android.support.v4.content.*;
import android.support.v7.app.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.google.android.gms.common.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.location.*;

import unirio.citytracksrt.R;
import unirio.citytracksrt.modelo.entidade.*;


public class MainActivity extends ActionBarActivity implements
         ResultCallback<LocationSettingsResult>, AdapterView.OnItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    /**
     * O intervalo desejado para as atualizações de localização. Inexato. Atualizações podem ser mais ou menos frequentes.
     */
    public static final long INTERVALO_DE_ATUALIZACAO_EM_MILISEGUNDOS = 1000;

    /**
     * A taxa mais alta para atualizações de localização ativas. Exata. Atualizações nunca serão mais frequentes que este valor
     */
    public static final long INTERVALO_DE_ATUALIZACAO_MAIS_RAPIDO_EM_MILISEGUNDOS = INTERVALO_DE_ATUALIZACAO_EM_MILISEGUNDOS;

    protected static final String TAG = "city-tracks-rt";

    // Chave para armazenamento do estado da atividade no Bundle
    protected final static String CHAVE_REQUISITANDO_ATUALIZACAO_DE_LOCALIZACAO = "requesting-location-updates-key";
    protected final static String CHAVE_DE_LOCALIZACAO = "location-key";
    protected final static String CHAVE_STRING_ULTIMA_HORA_ATUALIZADA = "last-updated-time-string-key";

    /**
     * Constante utilizada na janela de dialogo de configurações de localização
     */
    protected static final int CONFIGURACOES_DE_VERIFICACAO_DE_REQUISICAO = 0x1;

    // UI Widgets.
    protected Button iniciarAtualizacoesButton;
    protected Button pararAtualizacoesButton;
    protected TextView horaDaUltimaAtualizacaoTextView;
    protected TextView latitudeTextView;
    protected TextView longitudeTextView;
    protected TextView velocidadeMaximaTextView;
    protected TextView velocidadeMediaTextView;
    protected TextView numeroDeParadasTextView;
    protected TextView tempoDeParadaTextView;
    protected TextView aceleracaoMaximaTextView;
    protected TextView mudancasDeDirecaoTextView;
    protected TextView numeroDeInterpolacoesTextView;
    protected TextView tempoDeParadaMedioTextView;
    protected TextView totalDePontosTextView;
    protected TextView totalDePontosCapturadosTextView;
    protected Spinner modosDeTransporteSpinner;

    // Labels.
    protected String latitudeLabel;
    protected String longitudeLabel;
    protected String horaDaUltimaAtualizacaoLabel;
    protected String velocidadeMaximaLabel;
    protected String velocidadeMediaLabel;
    protected String numeroDeParadasLabel;
    protected String tempoDeParadaLabel;
    protected String aceleracaoMaximaLabel;
    protected String mudancasDeDirecaoLabel;
    protected String numeroDeInterpolacoesLabel;
    protected String tempoDeParadaMedioLabel;
    protected String totalDePontosLabel;
    protected String totalDePontosCapturadosLabel;

    /**
     * Prove um ponto de entrada para os serviços Google Play
     */
    protected GoogleApiClient clienteDaGoogleApi;

    /**
     * Armazena parâmetros de requisições para o FusedLocationProviderApi
     */
    protected LocationRequest requisicaoDeLocalizacao;

    /**
     * Armazena parametros das requisicoes de configurações de localização
     */
    private LocationSettingsRequest requisicaoDeConfiguracaoDeLocalizacao;

    /**
     * Representa uma localização geográfica
     */
    protected Location localizacaoAtual;

    /**
     * Rastreia o status da requisição de atualizações de localização.
     * O valor é modificado qunado o usuário pressiona os botões Inicar Atualizações ou Parar Atualizações
     */
    protected Boolean solicitandoAtualizacoesDeLocalizacao;

    /**
     * Hora quando a localização foi atualizada representada como uma String
     */
    protected String horaDaUltimaAtualizacao;

    /**
     * Armazena os pontos capturados e faz as sumarizacoes
     */
    protected ChunkBuilder chunkBuilder;

    /**
     * armazena a opção de modo de transporte selecionada pelo usuário
     */
    private ModosDeTransporte modoDeTransporteSelecionado;

    private BroadcastReceiver receiver;
    private LocalBroadcastManager broadcaster;
    private Intent servico;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(unirio.citytracksrt.R.layout.main_activity);

        solicitandoAtualizacoesDeLocalizacao = false;
        horaDaUltimaAtualizacao = "";
        chunkBuilder = new ChunkBuilder(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        localizacaoAtual = null;

        localizarOsUIWidgets();

        configurarLabels();

        construirGoogleApiClient();
        verificarConfiguracoesDeLocalizacao();

        // Atualiza os valores utilizando os dados armazenados no Bundle
        atualizarValoresComBundle(savedInstanceState);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                atualizarValoresComBundle(intent.getExtras());
            }
        };

        broadcaster = LocalBroadcastManager.getInstance(this);

    }

    /**
     * Constroi um GoogleApiClient. Utiliza o método {@code #addApi} para efetuar requisições a LocationServices API
     */
    protected synchronized void construirGoogleApiClient() {
        Log.i(TAG, "Construindo GoogleApiClient");
        clienteDaGoogleApi = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }

    @Override
    protected void onStart() {
        super.onStart();
        verificarConfiguracoesDeLocalizacao();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(CityTracksRTService.CITYTRACKSRT_RESULT)
        );
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "Todas as configurações de localização foram satisfeitas");
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Configurações de localização não foram satisfeitas. Mostrar janela de diálogo para que o usuário atualize as configurações");

                try {
                    status.startResolutionForResult(MainActivity.this, CONFIGURACOES_DE_VERIFICACAO_DE_REQUISICAO);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent incapaz de executar requisicao.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Configurações de localização inadequadas, e não podem ser corrigidas aqui. Janela de dialogo não criada.");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case CONFIGURACOES_DE_VERIFICACAO_DE_REQUISICAO:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "Uuário concordou em fazer as mudanças de configurações de localizações necessárias.");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "Usuário escolheu não realizar as mudanças de localização necessárias.");
                        break;
                }
                break;
        }
    }

    static final public String CITYTRACKSRT_RESULT = "unirio.citytracksrt.REQUEST_PROCESSED";

    static final public String CITYTRACKSRT_MESSAGE = "unirio.citytracksrt.CITYTRACKSRT_MSG";

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //capturar modo de transporte informador pelo usuario
        modoDeTransporteSelecionado = ModosDeTransporte.valueOf(position);
        Intent intent = new Intent(CITYTRACKSRT_RESULT);
        intent.putExtra(CITYTRACKSRT_MESSAGE, modoDeTransporteSelecionado.toString());
        broadcaster.sendBroadcast(intent);
        Log.i(TAG, "Modo de transporte alterado para " + modoDeTransporteSelecionado.toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    /**
     * Armazena os dados da atividade no Bundle
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(CHAVE_REQUISITANDO_ATUALIZACAO_DE_LOCALIZACAO, solicitandoAtualizacoesDeLocalizacao);
        savedInstanceState.putParcelable(CHAVE_DE_LOCALIZACAO, localizacaoAtual);
        savedInstanceState.putString(CHAVE_STRING_ULTIMA_HORA_ATUALIZADA, horaDaUltimaAtualizacao);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void configurarLabels() {
        latitudeLabel = getResources().getString(R.string.latitude_label);
        longitudeLabel = getResources().getString(R.string.longitude_label);
        horaDaUltimaAtualizacaoLabel = getResources().getString(R.string.hora_da_ultima_atualizacao_label);
        velocidadeMaximaLabel = getResources().getString(R.string.velocidade_maxima_label);
        velocidadeMediaLabel = getResources().getString(R.string.velocidade_media_label);
        numeroDeParadasLabel = getResources().getString(R.string.numero_de_paradas_label);
        tempoDeParadaLabel = getResources().getString(R.string.tempo_de_parada_label);
        aceleracaoMaximaLabel = getResources().getString(R.string.aceleracao_maxima_label);
        mudancasDeDirecaoLabel = getResources().getString(R.string.mudancas_de_direcao_label);
        numeroDeInterpolacoesLabel = getResources().getString(R.string.numero_de_interpolacoes);
        tempoDeParadaMedioLabel = getResources().getString(R.string.tempo_de_parada_medio);
        totalDePontosLabel = getResources().getString(R.string.total_de_pontos);
        totalDePontosCapturadosLabel = getResources().getString(R.string.total_de_pontos_capturados);
    }

    private void localizarOsUIWidgets() {
        iniciarAtualizacoesButton = (Button) findViewById(R.id.iniciar_atualizacoes_button);
        pararAtualizacoesButton = (Button) findViewById(R.id.parar_atualizacoes_button);
        latitudeTextView = (TextView) findViewById(R.id.latitude_text);
        longitudeTextView = (TextView) findViewById(R.id.longitude_text);
        velocidadeMaximaTextView = (TextView) findViewById(R.id.velocidade_maxima);
        velocidadeMediaTextView = (TextView) findViewById(R.id.velocidade_media);
        numeroDeParadasTextView = (TextView) findViewById(R.id.numero_de_paradas);
        tempoDeParadaTextView = (TextView) findViewById(R.id.tempo_de_parada);
        horaDaUltimaAtualizacaoTextView = (TextView) findViewById(R.id.hora_da_ultima_atualizacao_text);
        aceleracaoMaximaTextView = (TextView) findViewById(R.id.aceleracao_maxima);
        mudancasDeDirecaoTextView = (TextView) findViewById(R.id.mudancas_de_direcao);
        tempoDeParadaMedioTextView = (TextView) findViewById(R.id.tempo_de_parada_medio);
        numeroDeInterpolacoesTextView = (TextView) findViewById(R.id.numero_de_interpolacoes);
        totalDePontosTextView = (TextView) findViewById(R.id.total_de_pontos);
        totalDePontosCapturadosTextView = (TextView) findViewById(R.id.total_de_pontos_capturados);

        //seleção de modo de transporte
        modosDeTransporteSpinner = (Spinner) findViewById(R.id.modo_de_transporte_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.modos_de_transporte_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modosDeTransporteSpinner.setAdapter(adapter);
        modosDeTransporteSpinner.setOnItemSelectedListener(this);
    }

    /**
     * Atualiza os campos baseado-se nos dados armazenados o bundle
     *
     * @param savedInstanceState O estado da atividade salva no Bunde
     */
    private void atualizarValoresComBundle(Bundle savedInstanceState) {
        //Log.i(TAG, "Atualizando valores com bundle");
        if (savedInstanceState != null) {
            // Atualiza o valor de solicitandoAtualizacoesDeLocalizacao com o Bundle, e garante que
            // os botões Iniciar Atualizações e Parar Atualizações são habilitados ou desabilitados corretamente.
            if (savedInstanceState.keySet().contains(CHAVE_REQUISITANDO_ATUALIZACAO_DE_LOCALIZACAO)) {
                solicitandoAtualizacoesDeLocalizacao = savedInstanceState.getBoolean(
                        CHAVE_REQUISITANDO_ATUALIZACAO_DE_LOCALIZACAO);
            }

            // Autaliza o valor de localizacaoAtual com o Bundle e atualiza a UI para mostrar latitude e longitude atualizadas
            if (savedInstanceState.keySet().contains(CHAVE_DE_LOCALIZACAO)) {
                // Uma vez que CHAVE_DE_LOCALIZACAO foi encontrada no Bundle, nos podemos ter certeza que localizacaoAtual não é nula
                localizacaoAtual = savedInstanceState.getParcelable(CHAVE_DE_LOCALIZACAO);
            }

            // Atualiza o valor de horaDaUltimaAtualizacao com o Bundle e atualiza a UI
            if (savedInstanceState.keySet().contains(CHAVE_STRING_ULTIMA_HORA_ATUALIZADA)) {
                horaDaUltimaAtualizacao = savedInstanceState.getString(CHAVE_STRING_ULTIMA_HORA_ATUALIZADA);
            }

            if(savedInstanceState.keySet().contains("chunkBuilder")){
                chunkBuilder = (ChunkBuilder) savedInstanceState.getSerializable("chunkBuilder");
            }

            atualizarUI();
        }
    }

    /**
     * Configura o botão Iniciar Atualizações e solicita o início das atualizações de localização.
     * Não faz nada se as atualizações já tiverem sido solicitadas.
     */
    public void iniciarAtualizacoesButtonHandler(View view) {
        if (!solicitandoAtualizacoesDeLocalizacao) {
            solicitandoAtualizacoesDeLocalizacao = true;
            configurarEstadoHabilitadoDosButtons();
            servico = new Intent(this, CityTracksRTService.class).putExtra("modoDeTransporte", modoDeTransporteSelecionado.toString());
            startService(servico);
        }
    }

    /**
     * Configura o botão Parar Atualizacoes, e solicita a remoção das atualizações de localização.
     * Não faz nada se as atualizações já nao tiverem sido requisitadas previamente.
     */
    public void paraAtualizacoesButtonHandler(View view) {
        if (solicitandoAtualizacoesDeLocalizacao) {
            solicitandoAtualizacoesDeLocalizacao = false;
            configurarEstadoHabilitadoDosButtons();
            stopService(servico);
        }
    }

    /**
     * Garante que somente um botão é habilitado de cada vez.
     * O botão Iniciar Atualizações é habilitado se o usuário não estiver requisitando atualizações de localização.
     * O botão Parar Atualizações é habilitado se o usuário não estiver requisitando atualizações de localização.
     */
    private void configurarEstadoHabilitadoDosButtons() {
        if (solicitandoAtualizacoesDeLocalizacao) {
            iniciarAtualizacoesButton.setEnabled(false);
            pararAtualizacoesButton.setEnabled(true);
        } else {
            iniciarAtualizacoesButton.setEnabled(true);
            pararAtualizacoesButton.setEnabled(false);
        }
    }

    /**
     * Atualiza a latitude, a longitude, e a hora da ultima localizacao na UI.
     */
    private void atualizarUI() {

        if (localizacaoAtual != null) {
            String latitude = String.format("%s: %f", latitudeLabel,
                    localizacaoAtual.getLatitude());
            latitudeTextView.setText(latitude);
            String longitude = String.format("%s: %f", longitudeLabel,
                    localizacaoAtual.getLongitude());
            longitudeTextView.setText(longitude);
            horaDaUltimaAtualizacaoTextView.setText(String.format("%s: %s", horaDaUltimaAtualizacaoLabel,
                    horaDaUltimaAtualizacao));
        }

        if (chunkBuilder.isSumarizado()) {

            Chunk chunk = chunkBuilder.getChunk();

            String velocidadeMaxima = String.format("%s: %s m/segundo", velocidadeMaximaLabel, Float.toString(chunk.getVelocidadeMaxima()));
            velocidadeMaximaTextView.setText(velocidadeMaxima);
            String velocidadeMedia = String.format("%s: %s m/segundo", velocidadeMediaLabel, Float.toString(chunk.getVelocidadeMedia()));
            velocidadeMediaTextView.setText(velocidadeMedia);
            String numeroDeParadas = String.format("%s: %s", numeroDeParadasLabel, Integer.toString(chunk.getNumeroDeParadas()));
            numeroDeParadasTextView.setText(numeroDeParadas);
            String tempoDeParada = String.format("%s: %s segundos", tempoDeParadaLabel, Integer.toString(chunk.getTempoDeParada()));
            tempoDeParadaTextView.setText(tempoDeParada);
            String aceleracaoMaxima = String.format("%s: %s m*m/s", aceleracaoMaximaLabel, Float.toString(chunk.getAceleracaoMaxima()));
            aceleracaoMaximaTextView.setText(aceleracaoMaxima);
            String mudancasDeDirecao = String.format("%s: %s", mudancasDeDirecaoLabel, Integer.toString(chunk.getNumeroDeMudancasDeDirecao()));
            mudancasDeDirecaoTextView.setText(mudancasDeDirecao);
            String tempoDeParadaMedio = String.format("%s: %s segundos", tempoDeParadaMedioLabel, Float.toString(chunk.getTempoDeParadaMedio()));
            tempoDeParadaMedioTextView.setText(tempoDeParadaMedio);
            String numeroDeInterpolacoes = String.format("%s: %s", numeroDeInterpolacoesLabel, Integer.toString(chunk.getNumeroDeInterpolacoes()));
            numeroDeInterpolacoesTextView.setText(numeroDeInterpolacoes);
            String totalDePontosCapturados = String.format("%s: %s", totalDePontosCapturadosLabel, Integer.toString(chunk.getTotalDePontosCapturados()));
            totalDePontosCapturadosTextView.setText(totalDePontosCapturados);
            String totalDePontos = String.format("%s: %s", totalDePontosLabel, Integer.toString(chunkBuilder.getTotalDePontos()));
            totalDePontosTextView.setText(totalDePontos);


            Log.i(TAG, "Velocidade Maxima: " + chunk.getVelocidadeMaxima() +
                    ", Velocidade Media: " + chunk.getVelocidadeMedia() +
                    ", Aceleração Máxima: " + chunk.getAceleracaoMaxima() +
                    ", Mudanças de Direção: " + chunk.getNumeroDeMudancasDeDirecao() +
                    ", Numero de paradas: " + chunk.getNumeroDeParadas() +
                    ", Tempo de parada: " + chunk.getTempoDeParada() +
                    ", Tempo de parada médio: " + chunk.getTempoDeParada() +
                    ", Numero de interpolações: " + chunk.getNumeroDeInterpolacoes() +
                    ", Total de capturados: " + chunk.getTotalDePontosCapturados() +
                    ", Total de pontos: " + chunkBuilder.getTotalDePontos()
            );

            /*
            for (Ponto p : chunk.getPontos()) {
                Log.i(TAG, "Timestamp: " + p.getTimestampDaMedicao().getTime()
                        + " Precisao: " + p.getPrecisao()
                        + ", Diff. Segundos: " + p.getTempo()
                        + ", Distancia: " + p.getDistancia()
                        + ", Velocidade: " + p.getVelocidade()
                        + ", Aceleração: " + p.getAceleracao()
                        + ", Curso: " + p.getDirecao()
                        + ", Parada: " + p.isParada());
            }
            */

        }
    }

    private void verificarConfiguracoesDeLocalizacao() {
        requisicaoDeLocalizacao = LocationRequest.create();
        requisicaoDeLocalizacao.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        requisicaoDeLocalizacao.setInterval(INTERVALO_DE_ATUALIZACAO_EM_MILISEGUNDOS);
        requisicaoDeLocalizacao.setFastestInterval(INTERVALO_DE_ATUALIZACAO_MAIS_RAPIDO_EM_MILISEGUNDOS);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(requisicaoDeLocalizacao);
        requisicaoDeConfiguracaoDeLocalizacao = builder.build();
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        clienteDaGoogleApi,
                        requisicaoDeConfiguracaoDeLocalizacao
                );
        result.setResultCallback(this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
