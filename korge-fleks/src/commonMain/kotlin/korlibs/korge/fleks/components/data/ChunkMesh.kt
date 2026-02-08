package korlibs.korge.fleks.components.data


data class ChunkMesh(
    val chunkTop: ChunkMesh? = null,
    val chunkBottom: ChunkMesh? = null,
    val chunkLeft: ChunkMesh? = null,
    val chunkRight: ChunkMesh? = null,
)
