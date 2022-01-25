package unirio.citytracksrt.modelo.entidade;

import android.location.Location;

import java.util.Calendar;

import static unirio.citytracksrt.utils.Utils.calcularDistancia;

public class PontoFactory {

    private Ponto ponto;

    public Ponto constroiPonto(Location localizacaoAtual, Location novaLocalizacao, Ponto pontoAtual, Calendar timestampDaNovaLocalizacao, int totalDePontos, long diferencaEmSegundos) {

        ponto = new Ponto();

        ponto.setAltitude((float) novaLocalizacao.getAltitude());
        ponto.setLatitude((float) novaLocalizacao.getLatitude());
        ponto.setLongitude((float) novaLocalizacao.getLongitude());
        ponto.setPrecisao(novaLocalizacao.getAccuracy());
        ponto.setTimestampDaMedicao(timestampDaNovaLocalizacao);

        //So sera possivel calcular distancia e velocidade com 2 ou mais pontos
        if (totalDePontos > 0) {

            //calcular distancia, tempo e velocidade
            ponto.setTempo(diferencaEmSegundos);
            ponto.setDistancia(calcularDistancia(localizacaoAtual, novaLocalizacao));
            ponto.setVelocidade(ponto.getDistancia() / ponto.getTempo());

            //calcular direção, dividindo por 10 e considerando somente a parte inteira para diminuir a sensibilidade
            ponto.setDirecao(calcularDirecao(localizacaoAtual, novaLocalizacao));

            //so sera possivel calcular a aceleracao com 3 ou mais pontos
            if (totalDePontos > 1) {
                ponto.setAceleracao(ponto.getVelocidade() - pontoAtual.getVelocidade());
            }
        }

        return ponto;

    }

    private int calcularDirecao(Location localizacaoAtual, Location novaLocalizacao) {
        return Math.round(localizacaoAtual.bearingTo(novaLocalizacao) / 10);
    }

}
