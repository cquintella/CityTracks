package unirio.citytracksrt.modelo.persistencia;

import android.content.Context;
import android.os.Environment;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import unirio.citytracksrt.modelo.entidade.Chunk;

public class ChunkDaoJson implements ChunkDao {

    @Override
    public void persistir(Chunk chunk, Context contexto) {

        Gson gson = new Gson();

        String linhaJson = gson.toJson(chunk);

        File arquivoJson = new File(contexto.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "chunks.js");
        arquivoJson.setReadable(true);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoJson,true));
            writer.append(linhaJson);
            writer.newLine();
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
