package unirio.citytracksrt.controle;

import android.app.*;
import android.content.*;
import android.location.*;
import android.os.*;
import android.provider.*;
import android.support.annotation.*;
import android.support.v4.content.*;
import android.util.*;

import com.google.android.gms.common.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.location.*;

import java.text.*;
import java.util.*;

import unirio.citytracksrt.R;
import unirio.citytracksrt.modelo.entidade.*;
import unirio.citytracksrt.modelo.persistencia.*;
import unirio.citytracksrt.utils.inferencia.*;

import static unirio.citytracksrt.utils.Utils.*;



public class CityTracksRTService extends Service
{

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
     * Prove um ponto de entrada para os serviços Google Play
     */
    protected GoogleApiClient clienteDaGoogleApi;

    /**
     * Armazena parâmetros de requisições para o FusedLocationProviderApi
     */
    protected LocationRequest requisicaoDeLocalizacao;

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
     * Timestamp quando a localização foi atualizada representada como um Calendar
     */
    private Calendar timestampDaUltimaAtualizacao;

    /**
     * ultimo Ponto registrado
     */
    private Ponto pontoAtual;

    /**
     * armazena a opção de modo de transporte selecionada pelo usuário
     */
    private ModosDeTransporte modoDeTransporteSelecionado;

    /**
     * Classe de inferência de modo de transporte
     * */
    private Classificador classificador;

    private LocalBroadcastManager broadcaster;
    private BroadcastReceiver receiver;


    private class LocationListener implements ResultCallback<LocationSettingsResult>, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
    {

        public LocationListener()
        {
            inicializarAtributos();
            // Inicia o processo de construção de um GoogleApiClient e requisição do LocationServices API
            construirGoogleApiClient();
            criarRequisicaoDeLocalizacao();
        }

        /**
         * Constroi um GoogleApiClient. Utiliza o método {@code #addApi} para efetuar requisições a LocationServices API
         */
        protected synchronized void construirGoogleApiClient() {
            Log.i(TAG, "Construindo GoogleApiClient");
            clienteDaGoogleApi = new GoogleApiClient.Builder(getBaseContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        }

        private void inicializarAtributos() {
            solicitandoAtualizacoesDeLocalizacao = false;
            horaDaUltimaAtualizacao = "";
            iniciarNovoChunk();
            classificador = new Classificador();
            try {
                classificador.treinarAlgoritmos(getBaseContext());
                Log.i(TAG, "Classificadores treinados!");
            } catch (Exception e) {
                Log.e(TAG,e.getMessage());
            }
        }

        /**
         * Solicitar atualizações de localização da FusedLocationApi
         */
        protected void iniciarAtualizacoesDeLocalizacao() {
            // O argumento final para {@code requestLocationUpdates()} é um LocationListener
            // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
            LocationServices.FusedLocationApi.requestLocationUpdates(clienteDaGoogleApi, requisicaoDeLocalizacao, (com.google.android.gms.location.LocationListener) this);
            solicitandoAtualizacoesDeLocalizacao = true;
        }



        /**
         * Configura a requisição de localização. Android tem duas configurações de requisição de localização:
         * {@code ACCESS_COARSE_LOCATION} e {@code ACCESS_FINE_LOCATION}. Estas configurações controlam a precisão da localização atual
         * Neste projeto é utilizado ACESS_FINE_LOCATION, conforme definido no AndroidManifest.xml
         * <p>
         * Quando a configuração ACCESS_FINE_LOCATION é especificada, em conjunto com um intervalo de atualização curto (5 segundos),
         * a Fused Location Provider API retorna atualizações de localização com precisão de alguns pés.
         * <p>
         * Estas configurações são apropriadas para mapeamento de aplicações que mostram localizações em tempo-real
         */
        protected void criarRequisicaoDeLocalizacao() {
            requisicaoDeLocalizacao = new LocationRequest();

            // Configura o intervalo desejado de atualizações de localização ativas. Este intervalo é inexato.
            // É possível que não seja recebida nenhuma atualização se nenhum recurso de localização estiver disponível,
            // ou então pode ser que as atualizações sejam recebidas mais lentamente do que o solicitado.
            // É possível também que sejam recebidas atualizações mais rapidamente do que o solicitado
            // se outras aplicações estiverem solicitando localizações em um intervalo mais curto.
            requisicaoDeLocalizacao.setInterval(INTERVALO_DE_ATUALIZACAO_EM_MILISEGUNDOS);

            // Configura o intervalo mais curto para que atualizações de localização ativas sejam realizadas.
            // Este intervalo é exato, e a aplicação nunca vai receber atualizações em intervalos menores que este.
            requisicaoDeLocalizacao.setFastestInterval(INTERVALO_DE_ATUALIZACAO_MAIS_RAPIDO_EM_MILISEGUNDOS);

            requisicaoDeLocalizacao.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        @Override
        public void onLocationChanged(Location novaLocalizacao)
        {
            Calendar timestampDaNovaLocalizacao = Calendar.getInstance();
            timestampDaNovaLocalizacao.clear(Calendar.MILLISECOND);

            //processamento do ponto
            capturarPonto(novaLocalizacao, timestampDaNovaLocalizacao);

            localizacaoAtual = novaLocalizacao;
            horaDaUltimaAtualizacao = DateFormat.getTimeInstance().format(new Date(timestampDaNovaLocalizacao.getTimeInMillis()));
            timestampDaUltimaAtualizacao = timestampDaNovaLocalizacao;

            //verificar se chunkBuilder atingiu o limite de pontos
            if (chunkBuilder.isCheio()) {
                if (chunkBuilder.isValido()) {
                    chunkBuilder.sumarizar();

                    //salvar chunkBuilder em JSON ou CSV
                    Chunk chunk = chunkBuilder.getChunk();

                    try {
                        chunk  = classificador.classificar(chunk);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao classificar chunk!");
                    }

                    new ChunkDaoJson().persistir(chunk, getBaseContext());
                    new ChunkDaoCsv().persistir(chunk, getBaseContext());
                    new ChunkDaoArff().persistir(chunk, getBaseContext());

                    Log.i(TAG, "Chunk persistido em:" + getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS));
                    Log.i(TAG, "Linha csv: " + chunk.toString());



                } else {
                    String mensagem = getResources().getString(R.string.mensagem_chunk_descartado) + " numero de pontos = " + chunkBuilder.getTotalDePontos();
                    Log.e(TAG, mensagem);
                }

                iniciarNovoChunk();
            }

            sendResult();

        }

        private void iniciarNovoChunk() {
            chunkBuilder = new ChunkBuilder(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
            pontoAtual = null;
            localizacaoAtual = null;

            if (modoDeTransporteSelecionado != null) {
                chunkBuilder.setModoDeTransporteColetado(modoDeTransporteSelecionado);
            }
        }

        private void capturarPonto(Location novaLocalizacao, Calendar timestampDaNovaLocalizacao) {

            long diferencaEmSegundos = 0;

            if (timestampDaUltimaAtualizacao != null && timestampDaNovaLocalizacao != null) {
                diferencaEmSegundos = calcularDiferencaEmSegundos(timestampDaNovaLocalizacao, timestampDaUltimaAtualizacao);
            }

            // ignorar localizacoes com erro de precisao maior que 200 metros ou igual a 0.0 ou com Timestamps iguais
            if (novaLocalizacao.getAccuracy() < ChunkBuilder.LIMITE_DE_PRECISAO && novaLocalizacao.getAccuracy() > 0.0 && diferencaEmSegundos > 0) {

                PontoFactory pontoFactory = new PontoFactory();

                // se o chunkBuilder nao foi isDescartado por limite de tempo de parada
                if (!chunkBuilder.isDescartado()) {
                    //atualizar pontoAtual e adicionar ponto ao chunks
                    pontoAtual = pontoFactory.constroiPonto(
                            localizacaoAtual,
                            novaLocalizacao,
                            pontoAtual,
                            timestampDaNovaLocalizacao,
                            chunkBuilder.getTotalDePontos(),
                            diferencaEmSegundos);
                    chunkBuilder.adicionarPonto(pontoAtual);
                /*Toast.makeText(this, "Localização adicionada - Precisão:" + novaLocalizacao.getAccuracy()
                                + " Diferença em Segundos: " + diferencaEmSegundos
                                + " Velocidade: " + pontoAtual.getVelocidade()
                                + " Parada: " + pontoAtual.isParada(),
                        Toast.LENGTH_SHORT).show();*/
                } else {
                    String mensagem = getResources().getString(R.string.mensagem_chunk_descartado) + " tempo de parada = " + chunkBuilder.getTempoDeParada();
                    Log.e(TAG, mensagem);
                    iniciarNovoChunk();
                }

            } else {
                Log.e(TAG, "Localização descartada - Precisão:" + novaLocalizacao.getAccuracy()
                        + " Diferença em Segundos: " + diferencaEmSegundos);
            }

        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Log.i(TAG, "Conectado a GoogleApiClient");

            // Se a localização inicial não foi solicitada anteriormente, nós utilizamos FusedLocationApi.getLastLocation() para captura-la.
            // Se foi solicitada previamente, nos armazenamos seu valor no Bundle e procuramos por ele no OnCreate().
            // Não solicitaremos esta localização novamente a não ser que o usuário solicite atualizações de localizações pressionando o botão Iniciar Atualizações
            //
            // Porque nos armazenamos o valor da localização atual no Bundle, se o usuário executar a atividade, mudar de localização,
            // e depois modificar a orientação do dispositivo, a localização original é exibida uma vez que a actividade e re-criada
            if (localizacaoAtual == null) {
                Location localizacao = LocationServices.FusedLocationApi.getLastLocation(clienteDaGoogleApi);
                if (localizacao != null) {
                    timestampDaUltimaAtualizacao = Calendar.getInstance();
                    timestampDaUltimaAtualizacao.clear(Calendar.MILLISECOND);
                    capturarPonto(localizacao, timestampDaUltimaAtualizacao);
                    horaDaUltimaAtualizacao = DateFormat.getTimeInstance().format(new Date());
                    localizacaoAtual = localizacao;
                }
            }

            iniciarAtualizacoesDeLocalizacao();
        }

        @Override
        public void onConnectionSuspended(int i) {
            // A conexão com os serviços Google Play foi perdida por algum motivo.
            // Nós chamamos connect() para tentar reestabelecer a conexão.
            Log.i(TAG, "Conexão suspensa");
            clienteDaGoogleApi.connect();
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            // Consultar o javadoc de ConnectionResult para ver quais códigos de erro podem ser retornados em onConnectionFailed.
            Log.i(TAG, "Conexão falhou: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
        }

        @Override
        public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {

        }
    }

    static final public String CITYTRACKSRT_RESULT = "unirio.citytracksrt.CityTracksRTService.REQUEST_PROCESSED";

    public void sendResult() {

        Bundle savedInstanceState = new Bundle();
        savedInstanceState.putBoolean(CHAVE_REQUISITANDO_ATUALIZACAO_DE_LOCALIZACAO, solicitandoAtualizacoesDeLocalizacao);
        savedInstanceState.putParcelable(CHAVE_DE_LOCALIZACAO, localizacaoAtual);
        savedInstanceState.putString(CHAVE_STRING_ULTIMA_HORA_ATUALIZADA, horaDaUltimaAtualizacao);
        savedInstanceState.putSerializable("chunkBuilder", chunkBuilder);

        Intent intent = new Intent(CITYTRACKSRT_RESULT);
        intent.putExtras(savedInstanceState);

        broadcaster.sendBroadcast(intent);
    }


    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        LocationListener locationListener = new LocationListener();
        clienteDaGoogleApi.connect();
        String modoDeTransporte = intent.getStringExtra("modoDeTransporte");
        modoDeTransporteSelecionado = ModosDeTransporte.valueOf(modoDeTransporte);
        chunkBuilder.setModoDeTransporteColetado(modoDeTransporteSelecionado);
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(MainActivity.CITYTRACKSRT_RESULT)
        );
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        broadcaster = LocalBroadcastManager.getInstance(this);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String modoDeTransporte = intent.getStringExtra(MainActivity.CITYTRACKSRT_MESSAGE);
                modoDeTransporteSelecionado = ModosDeTransporte.valueOf(modoDeTransporte);
                chunkBuilder.setModoDeTransporteColetado(modoDeTransporteSelecionado);
            }
        };
    }

    @Override
    public void onDestroy()
    {
        clienteDaGoogleApi.disconnect();
    }


}