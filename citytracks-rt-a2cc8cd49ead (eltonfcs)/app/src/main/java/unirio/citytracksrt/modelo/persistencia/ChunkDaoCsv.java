package unirio.citytracksrt.modelo.persistencia;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import unirio.citytracksrt.modelo.entidade.Chunk;

public class ChunkDaoCsv implements ChunkDao {

    @Override
    public void persistir(Chunk chunk, Context contexto) {

        String linhaCsv = chunk.toString();

        File arquivoCsv = new File(contexto.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "chunks.csv");
        arquivoCsv.setReadable(true);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoCsv,true));
            if(arquivoCsv.length() == 0){
                writer.append("velocidade_maxima,aceleracao_maxima,mudancas_de_direcao,modo_de_transporte\n");
            }
            writer.append(linhaCsv);
            writer.newLine();
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
