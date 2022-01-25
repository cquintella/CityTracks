package unirio.citytracksrt.modelo.entidade;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static unirio.citytracksrt.utils.Utils.calcularDiferencaEmSegundos;

/*
 * Classe que utiliza o padrão Builder para simplificar a criação e manipulação do Chunk
 */
public class ChunkBuilder implements Serializable {

    //Maximo de pontos e tambem o tempo máximo de um chunkBuilder
    public static final int MAX_PONTOS = 90;

    //Mínimo de pontos que um chunkBuilder válido deve ter
    public static final int MIN_PONTOS = MAX_PONTOS / 10;

    // tempo máximo de parada em um chunkBuilder,
    // se o chunkBuilder tiver parada superior a este valor ele é isDescartado
    public static final int LIMITE_DE_PARADA = 20;

    // Velocidade mínima para que um ponto não seja considerado uma pausa
    public static final double LIMIAR_DE_DETECAO_DE_MOVIMENTO = 0.4;

    // precisão máxima em metros que uma localização pode ter
    public static final int LIMITE_DE_PRECISAO = 200;//20

    private Chunk chunk;
    private List<Ponto> pontos;
    private int numeroDeInterpolacoes;
    private int totalDePontosCapturados;
    private boolean sumarizado;
    private Ponto pontoDeInicioDaParada;
    private float tempoDeParada;

    /**
     * indica se o chunkBuilder atual foi isDescartado por violar algum dos thresholds
     */
    private boolean descartado;

    public ChunkBuilder(String idDispositivo) {
        chunk = new Chunk();
        pontos = new ArrayList<Ponto>();
        numeroDeInterpolacoes = 0;
        totalDePontosCapturados = 0;
        sumarizado = false;
        pontoDeInicioDaParada = null;
        tempoDeParada = 0;
        descartado = false;
        chunk.setIdDispositivo(idDispositivo);
    }

    /**
     * Adiciona ponto executando logica de interpolação de pontos com velocidade constante
     */
    public void adicionarPonto(Ponto ponto) {

        //se já houverem pontos na lista
        if (getPontos().size() > 0) {

            //Retorna o ultimo ponto da lista
            Ponto pontoAnterior = getPontos().get(getPontos().size() - 1);

            //copia o timeStamp do pontoAnterior
            Calendar timeStamp = (Calendar) pontoAnterior.getTimestampDaMedicao().clone();

            //calcula a diferenca em segundos entre o ponto atual e o ponto anterior
            long diferencaEmSegundos = calcularDiferencaEmSegundos(ponto.getTimestampDaMedicao(), timeStamp);

            //insere novos pontos com velocidade igual a do pontoAnterior
            // até que a diferença entre todos os pontos seja de 1 segundo sem ultrapassar o limite de pontos
            while (diferencaEmSegundos > 1 && getPontos().size() < MAX_PONTOS) {

                //adiciona um segundo ao timestamp
                timeStamp.add(Calendar.SECOND, 1);

                // instancia um novo ponto com base nas características do pontoAnterior
                Ponto novoPonto = new Ponto();
                novoPonto.setParada(pontoAnterior.isParada());
                novoPonto.setTempo(1);
                novoPonto.setVelocidade(pontoAnterior.getVelocidade());
                novoPonto.setAceleracao(0);
                novoPonto.setAltitude(pontoAnterior.getAltitude());
                novoPonto.setDistancia(pontoAnterior.getDistancia());
                novoPonto.setPrecisao(pontoAnterior.getPrecisao());
                novoPonto.setTimestampDaMedicao(timeStamp);
                novoPonto.setDirecao(pontoAnterior.getDirecao());

                //adiciona o novoPonto na lista
                getPontos().add(novoPonto);

                //incrementa o contador de interpolações

                numeroDeInterpolacoes = getNumeroDeInterpolacoes() + 1;

                //decrementa a diferença em segundos
                diferencaEmSegundos--;

                pontoAnterior = novoPonto;

            }

            if (getPontos().size() < MAX_PONTOS) {
                ponto.setParada(isParada(ponto));
                ponto.setTempo(calcularDiferencaEmSegundos(ponto.getTimestampDaMedicao(), pontoAnterior.getTimestampDaMedicao()));
                getPontos().add(ponto);
                totalDePontosCapturados = getTotalDePontosCapturados() + 1;
            }

        } else {
            //adicionar primeiro ponto
            ponto.setParada(isParada(ponto));
            getPontos().add(ponto);
            totalDePontosCapturados = getTotalDePontosCapturados() + 1;
        }


        //ordena os pontos por timeStamp
        Collections.sort(getPontos());

        chunk.setPontos(getPontos());
        chunk.setNumeroDeInterpolacoes(getNumeroDeInterpolacoes());
        chunk.setTotalDePontosCapturados(getTotalDePontosCapturados());

    }

    /**
     * calcula os atributos de sumarização do chunkBuilder com base na lista de pontos
     */
    public void sumarizar() {

        int numeroDeParadas = 0;
        int tempoDeParada = 0;
        float velocidadeMaxima = 0;
        float aceleracaoMaxima = 0;
        int numeroDeMudancasDeDirecao = 0;
        float velocidadeMedia = 0;
        float tempoDeParadaMedio = 0;

        //utiliza somente os 90 primeiros registros, os demais são descartados
        pontos = getPontos().subList(0, MAX_PONTOS);

        float velocidadeTotal = 0;

        Ponto pontoAnterior = getPontos().get(0);

        //As informacoes foram calculadas no mesmo for para aumentar a performance

        for (Ponto p : getPontos()) {

            if (p.isParada()) {
                if (!pontoAnterior.isParada()) {
                    numeroDeParadas++;
                }
                tempoDeParada++;
            } else {

                //processamento da velocidade maxima

                if (p.getVelocidade() > velocidadeMaxima) {
                    velocidadeMaxima = p.getVelocidade();
                }

                //processamento aceleracao maxima
                if (p.getAceleracao() > aceleracaoMaxima) {
                    aceleracaoMaxima = p.getAceleracao();
                }

                //processaento mudancas de direcao
                if (p.getDirecao() != pontoAnterior.getDirecao()) {
                    numeroDeMudancasDeDirecao++;
                }

                //processamento da velocidade media
                velocidadeTotal += p.getVelocidade();
                pontoAnterior = p;
            }

        }

        //processamento velocidade media
        velocidadeMedia = velocidadeTotal / getPontos().size();

        //processamento tempo de parada media
        if (numeroDeParadas > 0 && tempoDeParada > 0) {
            tempoDeParadaMedio = tempoDeParada / numeroDeParadas;
        }

        //atualiza os campos do Chunk
        chunk.setNumeroDeParadas(numeroDeParadas);
        chunk.setTempoDeParada(tempoDeParada);
        chunk.setVelocidadeMaxima(velocidadeMaxima);
        chunk.setAceleracaoMaxima(aceleracaoMaxima);
        chunk.setNumeroDeMudancasDeDirecao(numeroDeMudancasDeDirecao);
        chunk.setVelocidadeMedia(velocidadeMedia);
        chunk.setTempoDeParadaMedio(tempoDeParadaMedio);
        sumarizado = true;

    }

    public boolean isSumarizado() {
        return sumarizado;
    }

    public boolean isValido() {
        return getTotalDePontos() >= MIN_PONTOS;
    }

    public boolean isCheio() {
        return getTotalDePontos() >= MAX_PONTOS;
    }

    public boolean isDescartado() {
        return descartado;
    }

    private boolean isParada(Ponto ponto) {
        if (ponto.getDistancia() < LIMIAR_DE_DETECAO_DE_MOVIMENTO) {
            adicionarParada(ponto);
            return true;
        } else {
            if (getPontoDeInicioDaParada() != null) {
                pontoDeInicioDaParada = null;
            }
        }
        return false;
    }

    private void adicionarParada(Ponto ponto) {
        //verifica o ja existe uma parada anterior a esse ponto
        if (getPontoDeInicioDaParada() == null) {
            pontoDeInicioDaParada = ponto;
        } else {
            //verifica se o limite de tempo parado foi atingido e caso seja descarta o chunkBuilder atual e inicia um novo chunkBuilder
            verificarLimiteDeParada(ponto);
        }
    }

    private void verificarLimiteDeParada(Ponto ponto) {
        tempoDeParada = calcularDiferencaEmSegundos(ponto.getTimestampDaMedicao(), getPontoDeInicioDaParada().getTimestampDaMedicao());
        if (getTempoDeParada() > LIMITE_DE_PARADA) {
            descartado = true;
        } else {
            descartado = false;
        }
    }

    public int getTotalDePontos() {
        return getNumeroDeInterpolacoes() + getTotalDePontosCapturados();
    }

    public Chunk getChunk() {
        return chunk;
    }

    public void setModoDeTransporteColetado(ModosDeTransporte modoDeTransporteColetado) {
        chunk.setModoDeTransporteColetado(modoDeTransporteColetado);
    }

    public void setModoDeTransporteInferido(ModosDeTransporte modosDeTransporteInferido){
        chunk.setModoDeTransporteRedeNeural(modosDeTransporteInferido);
    }

    public ModosDeTransporte getModoDeTransporteColetado() {
        return chunk.getModoDeTransporteColetado();
    }

    public ModosDeTransporte getModoDeTransporteInferido(){
        return chunk.getModoDeTransporteRedeNeural();
    }

    public List<Ponto> getPontos() {
        return pontos;
    }

    public int getNumeroDeInterpolacoes() {
        return numeroDeInterpolacoes;
    }

    public int getTotalDePontosCapturados() {
        return totalDePontosCapturados;
    }

    /**
     * Armazena o ponto da ultima parada
     */
    public Ponto getPontoDeInicioDaParada() {
        return pontoDeInicioDaParada;
    }

    /**
     * contador da parada atual
     */
    public float getTempoDeParada() {
        return tempoDeParada;
    }
}
