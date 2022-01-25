package unirio.citytracksrt.modelo.entidade;

import java.util.Calendar;

/**
    Classe que representa um registro de localização
 */
public class Ponto implements Comparable<Ponto> {

    private Calendar timestampDaMedicao;

    private float altitude;
    private float latitude;
    private float longitude;

    private boolean isParada;

    //Direção em graus
    private int direcao;

    //velocidade em metros por segundo
    private float velocidade;

    //precisao em metros
    private float precisao;

    //distancia em metros
    private float distancia;

    //tempo em segundos
    private long tempo;

    //aceleracao em metros^2/segundo
    private float aceleracao;

    public Ponto() {
        this.timestampDaMedicao = null;
        this.direcao = 0;
        this.altitude = 0;
        this.velocidade = 0;
        this.precisao = 0;
        this.latitude = 0;
        this.longitude = 0;
        this.distancia = 0;
        this.tempo = 0;
        this.aceleracao = 0;
        this.setParada(false);
    }

    public Calendar getTimestampDaMedicao() {
        return timestampDaMedicao;
    }

    public void setTimestampDaMedicao(Calendar timestampDaMedicao) {
        this.timestampDaMedicao = timestampDaMedicao;
    }

    public int getDirecao() {
        return direcao;
    }

    public void setDirecao(int direcao) {
        this.direcao = direcao;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    public float getVelocidade() {
        return velocidade;
    }

    public void setVelocidade(float velocidade) {
        this.velocidade = velocidade;
    }

    public float getPrecisao() {
        return precisao;
    }

    public void setPrecisao(float precisao) {
        this.precisao = precisao;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getDistancia() {
        return distancia;
    }

    public void setDistancia(float distancia) {
        this.distancia = distancia;
    }

    public long getTempo() {
        return tempo;
    }

    public void setTempo(long tempo) {
        this.tempo = tempo;
    }

    public float getAceleracao() {
        return aceleracao;
    }

    public void setAceleracao(float aceleracao) {
        this.aceleracao = aceleracao;
    }

    public boolean isParada() {
        return isParada;
    }

    public void setParada(boolean parada) {
        isParada = parada;
    }

    @Override
    public int compareTo(Ponto ponto) {
        if (timestampDaMedicao.before(ponto.getTimestampDaMedicao())) {
            return -1;
        } else if (timestampDaMedicao.after(ponto.getTimestampDaMedicao())) {
            return 1;
        } else {
            return 0;
        }
    }
}
