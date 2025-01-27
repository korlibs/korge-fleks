package korlibs.korge.fleks.assets

import korlibs.datastructure.*
import korlibs.datastructure.iterators.*
import korlibs.image.atlas.*
import korlibs.image.bitmap.*
import korlibs.image.font.*
import korlibs.image.format.*
import korlibs.io.dynamic.*
import korlibs.io.file.*
import korlibs.io.lang.*
import korlibs.io.stream.*
import korlibs.io.util.*
import korlibs.math.geom.*
import korlibs.math.geom.binpack.*
import kotlin.collections.set
import kotlin.collections.toList
import kotlin.math.*

typealias MutableAtlas2Unit = MutableAtlas2<Unit>

fun MutableAtlas2Unit.add(bmp: Bitmap32, name: String? = "Slice$size") = add(bmp, Unit, name)
fun MutableAtlas2Unit.add(bmp: BmpSlice32, name: String? = bmp.name) = this.add(bmp, Unit, name)

class MutableAtlas2<T>(
    var binPacker: BinPacker,
    val border: Int = 2,
    val premultiplied: Boolean = true,
    val allowToGrow: Boolean = true,
    val growMethod: GrowMethod = GrowMethod.NEW_IMAGES
) {
    private val borderMargin = MarginInt(border)

    constructor(
        width: Int = 2048,
        height: Int = width,
        border: Int = 2,
        premultiplied: Boolean = true,
        allowToGrow: Boolean = true,
        growMethod: GrowMethod = GrowMethod.NEW_IMAGES
    ) : this(BinPacker(width, height), border, premultiplied, allowToGrow, growMethod)

    val width: Int get() = binPacker.width.toInt()
    val height: Int get() = binPacker.height.toInt()

    enum class GrowMethod { GROW_IMAGE, NEW_IMAGES }

    data class Entry<T>(val slice: BmpSlice32, val data: T) {
        val name get() = slice.name
    }

    //val bitmap = NativeImage(binPacker.width.toInt(), binPacker.height.toInt(), premultiplied = premultiplied)
    var bitmap = Bitmap32(width, height, premultiplied = premultiplied)
    val allBitmaps = arrayListOf<Bitmap32>(bitmap)
    val entries = arrayListOf<Entry<T>>()
    val entriesByName = LinkedHashMap<String, Entry<T>>()
    val size get() = entries.size

    operator fun contains(name: String): Boolean = name in entriesByName

    operator fun get(name: String): BmpSlice = entriesByName[name]?.slice
        ?: error("Can't find '$name' it atlas")

    private fun reconstructWithSize(width: Int, height: Int) {
        val slices = entries.toList()
        binPacker = BinPacker(width, height)
        bitmap = Bitmap32(width, height, premultiplied = premultiplied)
        allBitmaps.clear()
        allBitmaps.add(bitmap)
        entriesByName.clear()
        entries.clear()
        for (entry in slices) add(entry.slice, entry.data, entry.slice.name)
    }

    private fun growAtlas(bmp: BmpSlice32) {
        when (growMethod) {
            GrowMethod.GROW_IMAGE -> reconstructWithSize(this.width * 2, this.height * 2)
            GrowMethod.NEW_IMAGES -> {
                if (bmp.width > width || bmp.height > height) error("Atlas is too small (${width}x${height}) to hold a slice of (${bmp.width}x${bmp.height})")
                binPacker = BinPacker(width, height)
                bitmap = Bitmap32(width, height, premultiplied = premultiplied)
                allBitmaps.add(bitmap)
            }
        }
    }

    fun add(bmp: Bitmap32, data: T, name: String? = "Slice$size"): Entry<T> = add(bmp.slice(name = name), data, name)

    @Suppress("UNCHECKED_CAST")
    fun add(bmp: BmpSlice, data: T, name: String? = bmp.name): Entry<T> {
        if (bmp.bmp !is Bitmap32) {
            return add(bmp.extract().toBMP32(), data, name)
        }
        bmp as BmpSlice32
        try {
            val rname = name ?: "Slice$size"
            val isEmpty = bmp.isFullyTransparent()
            var entry: Entry<T>? = null

            if (isEmpty && biggestEmptyEntry != null) {
                val bigEmptySlice = biggestEmptyEntry!!.slice
                if (bigEmptySlice.width >= bmp.width && bigEmptySlice.height >= bmp.height) {
                    entry = Entry(
                        bigEmptySlice.slice(RectangleInt(0, 0, bmp.width, bmp.height)),
                        data
                    )
                }
            }

            if (entry == null) {
                val rect = binPacker.add(bmp.width.toDouble() + border * 2, bmp.height.toDouble() + border * 2)
                val slice: BmpSlice32 = this.bitmap.sliceWithSize(
                    (rect.left + border).toInt(),
                    (rect.top + border).toInt(),
                    bmp.width,
                    bmp.height,
                    rname
                )
                val dstX = slice.left
                val dstY = slice.top
                val boundsWithBorder: RectangleInt = slice.bounds.expanded(borderMargin)
                this.bitmap.lock(boundsWithBorder) {
                    this.bitmap.draw(bmp, dstX, dstY)
                    this.bitmap.expandBorder(slice.rect, border)
                }
                //bmp.bmp.copy(srcX, srcY, this.bitmap, dstX, dstY, w, h)
                entry = Entry(slice, data)

                if (biggestEmptyEntry == null || bmp.area > biggestEmptyEntry!!.slice.area) {
                    biggestEmptyEntry = entry
                }
            }

            entries += entry
            entriesByName[rname] = entry

            return entry
        } catch (e: BinPacker.ImageDoNotFitException) {
            if (!allowToGrow) throw e
            growAtlas(bmp)
            return this.add(bmp, data, name)
        }
    }

    var biggestEmptyEntry: Entry<T>? = null

    fun toImmutable(): Atlas {
        val bitmap = this.bitmap.clone()
        return Atlas(this.entries.map {
            val slice = it.slice
            bitmap.sliceWithBounds(slice.left, slice.top, slice.width, slice.height, slice.name)
        })
    }
}

fun ImageData.packInMutableAtlas(mutableAtlas: MutableAtlas2<Unit>): ImageData {
    @Suppress("MoveSuspiciousCallableReferenceIntoParentheses")
    val frameLayers = frames.flatMap { it.layerData }.filter { it.includeInAtlas }.sortedByDescending { it.area }
    frameLayers.fastForEach { frameLayer ->
        frameLayer.slice = mutableAtlas.add(frameLayer.slice, Unit, frameLayer.slice.name).slice
    }
    return this
}

fun ImageDataContainer.packInMutableAtlas(mutableAtlas: MutableAtlas2<Unit>): ImageDataContainer {
    this.imageDatas.fastForEach { it.packInMutableAtlas(mutableAtlas) }
    return this
}

suspend fun VfsFile.readImageDataContainer(props: BaseImageDecodingProps = ImageDecodingProps.DEFAULT, atlas: MutableAtlas2<Unit>? = null): ImageDataContainer {
    val out = props.decodingProps.formatSure.readImageContainer(this.read().openSync(), props.decodingProps.withFile(this))
    return if (atlas != null) out.packInMutableAtlas(atlas) else out
}

suspend fun VfsFile.readBitmapFont(
    props: ImageDecodingProps = ImageDecodingProps.DEFAULT,
    mipmaps: Boolean = true,
    atlas: MutableAtlas2Unit? = null
) : BitmapFont {
    val fntFile = this
    val content = fntFile.readString().trim()
    val textures = hashMapOf<Int, BmpSlice>()

    return when {
        content.startsWith("info") -> readBitmapFontTxt(content, fntFile, textures, props, mipmaps, atlas)
        else -> TODO("Unsupported font type starting with ${content.substr(0, 16)}")
    }
}

private suspend fun readBitmapFontTxt(
    content: String,
    fntFile: VfsFile,
    textures: HashMap<Int, BmpSlice>,
    props: ImageDecodingProps = ImageDecodingProps.DEFAULT,
    mipmaps: Boolean = true,
    atlas: MutableAtlas2Unit? = null
): BitmapFont {
    val kernings = arrayListOf<BitmapFont.Kerning>()
    val glyphs = arrayListOf<BitmapFont.Glyph>()
    var lineHeight = 16.0
    var fontSize = 16.0
    var base: Double? = null
    for (rline in content.lines()) {
        val line = rline.trim()
        val map = LinkedHashMap<String, String>()
        for (part in line.split(' ')) {
            val (key, value) = part.split('=') + listOf("", "")
            map[key] = value
        }
        when {
            line.startsWith("info") -> {
                fontSize = (map["size"]?.toDouble() ?: 16.0).absoluteValue
            }
            line.startsWith("page") -> {
                val id = map["id"]?.toInt() ?: 0
                val file = map["file"]?.unquote() ?: error("page without file")
                textures[id] = fntFile.parent[file].readBitmap(props).mipmaps(mipmaps).slice()
            }
            line.startsWith("common ") -> {
                lineHeight = map["lineHeight"]?.toDoubleOrNull() ?: 16.0
                base = map["base"]?.toDoubleOrNull()
            }
            line.startsWith("char ") -> {
                //id=54 x=158 y=88 width=28 height=42 xoffset=2 yoffset=8 xadvance=28 page=0 chnl=0
                val page = map["page"]?.toIntOrNull() ?: 0
                val texture = textures[page] ?: textures.values.first()
                val dmap = map.dyn
                val id = dmap["id"].int
                glyphs += BitmapFont.Glyph(
                    fontSize = fontSize,
                    id = id,
                    xoffset = dmap["xoffset"].int,
                    yoffset = dmap["yoffset"].int,
                    xadvance = dmap["xadvance"].int,
                    texture = atlas?.add(texture.sliceWithSize(dmap["x"].int, dmap["y"].int, dmap["width"].int, dmap["height"].int, "glyph-${id.toChar()}") as BmpSlice, Unit)?.slice
                        ?: texture.sliceWithSize(dmap["x"].int, dmap["y"].int, dmap["width"].int, dmap["height"].int, "glyph-${id.toChar()}")
                )
            }
            line.startsWith("kerning ") -> {
                kernings += BitmapFont.Kerning(
                    first = map["first"]?.toIntOrNull() ?: 0,
                    second = map["second"]?.toIntOrNull() ?: 0,
                    amount = map["amount"]?.toIntOrNull() ?: 0
                )
            }
        }
    }
    return BitmapFont(
        fontSize = fontSize,
        lineHeight = lineHeight,
        base = base ?: lineHeight,
        glyphs = glyphs.associateBy { it.id }.toIntMap(),
        kernings = kernings.associateByInt { _, it ->
            BitmapFont.Kerning.buildKey(it.first, it.second)
        }
    )
}
