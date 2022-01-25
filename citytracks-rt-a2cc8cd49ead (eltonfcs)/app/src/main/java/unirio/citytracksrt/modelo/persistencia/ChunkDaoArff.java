package unirio.citytracksrt.modelo.persistencia;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import unirio.citytracksrt.modelo.entidade.Chunk;
import unirio.citytracksrt.modelo.entidade.ModosDeTransporte;

public class ChunkDaoArff implements ChunkDao {

    @Override
    public void persistir(Chunk chunk, Context contexto) {

        String linhaArff = chunk.toString();

        File arquivoArff = new File(contexto.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "chunks.arff");
        arquivoArff.setReadable(true);

        StringBuilder cabecalho = getCabecalho();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoArff,true));

            if(arquivoArff.length() == 0){
                writer.append(cabecalho.toString());
            }

            writer.append(linhaArff);
            writer.newLine();
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @NonNull
    public StringBuilder getCabecalho() {
        StringBuilder cabecalho = new StringBuilder();
        cabecalho.append("@relation chunks\n");
        cabecalho.append("\n");
        cabecalho.append("@attribute velocidade_maxima numeric\n");
        cabecalho.append("@attribute aceleracao_maxima numeric\n");
        cabecalho.append("@attribute mudancas_de_direcao numeric\n");
        cabecalho.append("@attribute modo_de_transporte {");
        for(ModosDeTransporte m : ModosDeTransporte.values()){
            cabecalho.append(m.toString() + ",");
        }
        cabecalho.deleteCharAt(cabecalho.lastIndexOf(","));
        cabecalho.append("}\n\n");
        cabecalho.append("@data\n");
        return cabecalho;
    }

}
