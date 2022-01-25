package unirio.citytracksrt.modelo.persistencia;

import android.content.Context;

import unirio.citytracksrt.modelo.entidade.Chunk;

public interface ChunkDao {

    void persistir(Chunk chunk, Context context);
}
