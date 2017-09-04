package sz.scaffold.captcha

import jj.play.ns.nl.captcha.Captcha
import jj.play.ns.nl.captcha.backgrounds.FlatColorBackgroundProducer
import jj.play.ns.nl.captcha.backgrounds.GradiatedBackgroundProducer
import jj.play.ns.nl.captcha.backgrounds.TransparentBackgroundProducer
import jj.play.ns.nl.captcha.noise.CurvedLineNoiseProducer
import jj.play.ns.nl.captcha.text.producer.TextProducer
import jj.play.ns.nl.captcha.text.renderer.ColoredEdgesWordRenderer
import jj.play.ns.nl.captcha.text.renderer.WordRenderer
import org.apache.commons.lang3.StringUtils
import sz.scaffold.Application
import sz.scaffold.ext.getBooleanOrElse
import sz.scaffold.ext.getIntOrElse
import sz.scaffold.ext.getStringListOrEmpty
import sz.scaffold.ext.getStringOrElse
import java.awt.*
import java.awt.image.BufferedImage
import java.io.*
import java.security.SecureRandom
import java.util.*
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageOutputStream

//
// Created by kk on 17/9/4.
//
class Images {
    /**
     * Resize an image

     * @param originalImage The image file
     * *
     * @param to            The destination file
     * *
     * @param w             The new width (or -1 mail_to proportionally resize)
     * *
     * @param h             The new height (or -1 mail_to proportionally resize)
     */
    fun resize(originalImage: File, to: File, w: Int, h: Int) {
        resize(originalImage, to, w, h, false)
    }

    /**
     * Resize an image

     * @param originalImage The image file
     * *
     * @param to            The destination file
     * *
     * @param w             The new width (or -1 mail_to proportionally resize) or the maxWidth if keepRatio is true
     * *
     * @param h             The new height (or -1 mail_to proportionally resize) or the maxHeight if keepRatio is true
     * *
     * @param keepRatio     : if true, resize will keep the original image ratio and use w and h as max dimensions
     */
    fun resize(originalImage: File, to: File, w: Int, h: Int, keepRatio: Boolean) {
        var w = w
        var h = h
        try {
            val source = ImageIO.read(originalImage)
            val owidth = source.getWidth()
            val oheight = source.getHeight()
            val ratio = owidth.toDouble() / oheight

            val maxWidth = w
            val maxHeight = h

            if (w < 0 && h < 0) {
                w = owidth
                h = oheight
            }
            if (w < 0 && h > 0) {
                w = (h * ratio).toInt()
            }
            if (w > 0 && h < 0) {
                h = (w / ratio).toInt()
            }

            if (keepRatio) {
                h = (w / ratio).toInt()
                if (h > maxHeight) {
                    h = maxHeight
                    w = (h * ratio).toInt()
                }
                if (w > maxWidth) {
                    w = maxWidth
                    h = (w / ratio).toInt()
                }
            }

            var mimeType = "image/jpeg"
            if (to.getName().endsWith(".png")) {
                mimeType = "image/png"
            }
            if (to.getName().endsWith(".gif")) {
                mimeType = "image/gif"
            }

            // out
            val dest = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
            val srcSized = source.getScaledInstance(w, h, Image.SCALE_SMOOTH)
            val graphics = dest.graphics
            graphics.color = Color.WHITE
            graphics.fillRect(0, 0, w, h)
            graphics.drawImage(srcSized, 0, 0, null)
            val writer = ImageIO.getImageWritersByMIMEType(mimeType).next()
            val params = writer.defaultWriteParam
            val toFs = FileImageOutputStream(to)
            writer.output = toFs
            val image = IIOImage(dest, null, null)
            writer.write(null, image, params)
            toFs.flush()
            toFs.close()
            writer.dispose()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    /**
     * Crop an image

     * @param originalImage The image file
     * *
     * @param to            The destination file
     * *
     * @param x1            The new x origin
     * *
     * @param y1            The new y origin
     * *
     * @param x2            The new x end
     * *
     * @param y2            The new y end
     */
    fun crop(originalImage: File, to: File, x1: Int, y1: Int, x2: Int, y2: Int) {
        try {
            val source = ImageIO.read(originalImage)

            var mimeType = "image/jpeg"
            if (to.getName().endsWith(".png")) {
                mimeType = "image/png"
            }
            if (to.getName().endsWith(".gif")) {
                mimeType = "image/gif"
            }
            val width = x2 - x1
            val height = y2 - y1

            // out
            val dest = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            val croppedImage = source.getSubimage(x1, y1, width, height)
            val graphics = dest.graphics
            graphics.color = Color.WHITE
            graphics.fillRect(0, 0, width, height)
            graphics.drawImage(croppedImage, 0, 0, null)
            val writer = ImageIO.getImageWritersByMIMEType(mimeType).next()
            val params = writer.defaultWriteParam
            writer.output = FileImageOutputStream(to)
            val image = IIOImage(dest, null, null)
            writer.write(null, image, params)
            writer.dispose()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    /**
     * Embeds an image watermark over a source image to produce
     * a watermarked one.

     * @param watermarkImageFile The image file used as the watermark.
     * *
     * @param sourceImageFile    The source image file.
     * *
     * @param destImageFile      The output image file.
     */
    @Throws(IOException::class)
    fun addImageWatermark(watermarkImageFile: File, sourceImageFile: File,
                          destImageFile: File, alpha: Float) {
        val sourceImage = ImageIO.read(sourceImageFile)
        val watermarkImage = ImageIO.read(watermarkImageFile)

        // initializes necessary graphic properties
        val g2d = sourceImage.getGraphics() as Graphics2D
        val alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
        g2d.composite = alphaChannel
        val width = sourceImage.getWidth()
        val scaleWidth = watermarkImage.getWidth()
        val height = sourceImage.getHeight()
        val scaleHeight = watermarkImage.getHeight()

        //Logger.debug("width="+width+",height="+height+",scaleWidth="+scaleWidth+",scaleHeight"+scaleHeight);
        var x = 10
        while (x < width) {
            var y = 10
            while (y < height) {
                g2d.drawImage(watermarkImage, x, y, null)
                y += 2 * scaleHeight
            }
            x += 2 * scaleWidth
        }

        ImageIO.write(sourceImage, "png", destImageFile)
        g2d.dispose()

    }


    @Throws(IOException::class)
    fun addImageWatermark(sourceImage: BufferedImage, watermarkImage: BufferedImage,
                          alpha: Float): InputStream {
        // initializes necessary graphic properties
        val g2d = sourceImage.graphics as Graphics2D
        val alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
        g2d.composite = alphaChannel

        // 平铺水印图片
        var x = 0
        while (x < sourceImage.width) {
            var y = 0
            while (y < sourceImage.height) {
                g2d.drawImage(watermarkImage, x, y, null)
                y += watermarkImage.height
            }
            x += watermarkImage.width
        }

        val outs = ByteArrayOutputStream()
        ImageIO.write(sourceImage, "png", outs)
        g2d.dispose()
        val ins = ByteArrayInputStream(outs.toByteArray())
        return ins
    }

    @Throws(IOException::class)
    fun ImageWatermark(filePath: String, watermarkFilePath: String,
                       alpha: Float): InputStream {
        val sourceImage = ImageIO.read(File(filePath))
        val watermarkImage = ImageIO.read(File(watermarkFilePath))
        return addImageWatermark(sourceImage, watermarkImage, alpha)

    }

    /**
     * A captcha image.
     */
    class ImageCaptcha : InputStream, TextProducer {

        var width: Int = 0
        var height: Int = 0
        var answer: String? = null

        constructor(width: Int, height: Int) {
            this.width = width
            this.height = height
        }

        constructor() {
            this.width = DEFAULT_WIDTH
            this.height = DEFAULT_HEIGHT
        }

        fun SetAnswer(text: String) {
            this.answer = text
        }

        fun GetAnswer(): String? {
            return this.answer
        }

        private var bais: ByteArrayInputStream? = null

        @Throws(IOException::class)
        override fun read(): Int {
            check()
            return bais!!.read()
        }

        @Throws(IOException::class)
        override fun read(b: ByteArray): Int {
            check()
            return bais!!.read(b)
        }

        override fun getText(): String? {
            return this.answer
        }

        internal fun check() {
            try {
                if (StringUtils.isBlank(this.text)) {
                    SetAnswer(RandomText())
                }
                if (bais == null) {
                    val builder = Captcha.Builder(width, height)

                    val wordRenderer: WordRenderer
                    if (OutlineFont) {
                        // 空心字体
                        wordRenderer = ColoredEdgesWordRenderer(DEFAULT_COLORS, DEFAULT_FONTS)
                    } else {
                        // 实心字体
                        wordRenderer = CgWordRenderer(DEFAULT_COLORS, DEFAULT_FONTS, TextMarginLeft, height - TextMarginBottom)
                    }

                    builder.addText(this, wordRenderer)

                    if (AddBorder) {
                        builder.addBorder()
                        //                        Logger.debug("ADD Border");
                    }
                    if (CurvedLine) {
                        builder.addNoise(CurvedLineNoiseProducer(Color.decode(CurvedLineColor), CurvedLineWidth.toFloat()))
                        builder.addNoise(CurvedLineNoiseProducer(Color.decode(CurvedLineColor), CurvedLineWidth.toFloat()))
                    }
                    if (BackgroundTransparent) {
                        val tbp = TransparentBackgroundProducer()
                        builder.addBackground(tbp)
                    }
                    if (BackgroundFlatColor) {
                        val fbp = FlatColorBackgroundProducer(Color.decode(FlatColor))
                        builder.addBackground(fbp)
                    }
                    if (BackgroundGradiated) {
                        val gbp = GradiatedBackgroundProducer()
                        gbp.setFromColor(Color.decode(GradiatedFromColor))
                        gbp.setToColor(Color.decode(GradiatedToColor))
                        builder.addBackground(gbp)
                    }
                    if (RippleGimpyRenderer) {
                        val rip = jj.play.ns.nl.captcha.gimpy.RippleGimpyRenderer()
                        builder.gimp(rip)
                    }
                    if (BlockGimpyRenderer) {
                        val blk = jj.play.ns.nl.captcha.gimpy.BlockGimpyRenderer(BlockGimpyBlockSize)
                        builder.gimp(blk)
                    }
                    if (DropShadowGimpyRenderer) {
                        val dsh = jj.play.ns.nl.captcha.gimpy.DropShadowGimpyRenderer(DropShadowGimpyRadius,
                                DropShadowGimpyOpacity)
                        builder.gimp(dsh)
                    }
                    val ca = builder.build()
                    val bi = ca.getImage()
                    val baos = ByteArrayOutputStream()
                    ImageIO.write(bi, "png", baos)
                    bais = ByteArrayInputStream(baos.toByteArray())
                }
            } catch (ex: Exception) {
                throw RuntimeException(ex)
            }


        }

        companion object {
            internal val DEFAULT_WIDTH = Application.config.getIntOrElse("captcha.Width", 150)
            internal val DEFAULT_HEIGHT = Application.config.getIntOrElse("captcha.Height", 50)
            internal val TextLength = Application.config.getIntOrElse("captcha.TextLength", 4)
            internal val TextMarginBottom = Application.config.getIntOrElse("captcha.TextMarginBottom", 15)
            internal val TextMarginLeft = Application.config.getIntOrElse("captcha.TextMarginLeft", 10)
            internal val OutlineFont = Application.config.getBooleanOrElse("captcha.Outline_Font", true)
            internal val BackgroundTransparent = Application.config.getBooleanOrElse("captcha.BackgroundTransparent", true)
            internal val BackgroundGradiated = Application.config.getBooleanOrElse("captcha.BackgroundGradiated", false)
            internal val GradiatedFromColor = Application.config.getStringOrElse("captcha.BackgroundGradiatedFromColor", "#EDEEF0")
            internal val GradiatedToColor = Application.config.getStringOrElse("captcha.BackgroundGradiatedToColor", "#C5D0E6")
            internal val BackgroundFlatColor = Application.config.getBooleanOrElse("captcha.BackgroundFlatColorEnable", false)
            internal val FlatColor = Application.config.getStringOrElse("captcha.BackgroundFlatColor", "#EDEEF0")
            internal val RippleGimpyRenderer = Application.config.getBooleanOrElse("captcha.GimpyRendererRippleGimpyRenderer", true)
            internal val BlockGimpyRenderer = Application.config.getBooleanOrElse("captcha.GimpyRendererBlockGimpyRenderer", false)
            internal val BlockGimpyBlockSize = Application.config.getIntOrElse("captcha.GimpyRendererBlockGimpyRendererBlockSize", 1)
            internal val DropShadowGimpyRenderer = Application.config.getBooleanOrElse("captcha.DropShadowGimpyRenderer", false)
            internal val DropShadowGimpyRadius = Application.config.getIntOrElse("captcha.DropShadowGimpyRendererRadius", 3)
            internal val DropShadowGimpyOpacity = Application.config.getIntOrElse("captcha.DropShadowGimpyRendererOpacity", 75)
            internal val CurvedLine = Application.config.getBooleanOrElse("captcha.NoiseCurvedLine", false)
            internal val CurvedLineWidth = Application.config.getIntOrElse("captcha.NoiseCurvedLineWidth", 2)
            internal val CurvedLineColor = Application.config.getStringOrElse("captcha.NoiseCurvedLineColor", "#2795EA")
            internal val AddBorder = Application.config.getBooleanOrElse("captcha.AddBorder", false)

            private val CHAR_CODES = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789"

            private val DEFAULT_COLORS = ArrayList<Color>()
            private val DEFAULT_FONTS = ArrayList<Font>()

            init {
                val colors = Application.config.getStringListOrEmpty("captcha.FontColors")
                if (colors.isEmpty()) {
                    DEFAULT_COLORS.add(Color.BLUE)
                    DEFAULT_COLORS.add(Color.GREEN)
                    DEFAULT_COLORS.add(Color.RED)
                    DEFAULT_COLORS.add(Color.BLACK)
                } else {
                    for (color_name in colors) {
                        DEFAULT_COLORS.add(Color.decode(color_name))
                    }
                }

                val fonts = Application.config.getStringListOrEmpty("captcha.Fonts")
                if (fonts.isEmpty()) {
                    DEFAULT_FONTS.add(Font("Arial", Font.BOLD, 40))
                    DEFAULT_FONTS.add(Font("Courier", Font.BOLD, 40))
                    DEFAULT_FONTS.add(Font("Arial", Font.ITALIC, 40))
                    DEFAULT_FONTS.add(Font("Courier", Font.ITALIC, 40))
                } else {
                    for (font_name in fonts) {
                        DEFAULT_FONTS.add(Font.decode(font_name))
                    }
                }
            }

            fun RandomText(): String {
                val charsArray = CHAR_CODES.toCharArray()
                val random = Random(System.currentTimeMillis())
                val sb = StringBuffer(TextLength)
                for (i in 0..TextLength - 1) {
                    sb.append(charsArray[random.nextInt(charsArray.size)])
                }
                return sb.toString()
            }
        }
    }
}

class CgWordRenderer : WordRenderer {

    var _colors = arrayListOf<Color>()
    var _fonts = arrayListOf<Font>()
    var pos_x_: Int? = null
    var pos_y_: Int? = null

    constructor(colors: ArrayList<Color>, fonts: ArrayList<Font>, x: Int, y: Int) {
        _colors = colors
        _fonts = fonts
        pos_y_ = y
        pos_x_ = x
    }

    override fun render(word: String, image: BufferedImage) {
        val g = image.createGraphics()

        val hints = RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        hints.add(RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY))
        g.setRenderingHints(hints)

        g.color = getRandomColor()
        val frc = g.fontRenderContext
        var startPosX = pos_x_
        val wc = word.toCharArray()
        val generator = Random()
        for (element in wc) {
            val itchar = charArrayOf(element)
            val itFont = getRandomFont()
            g.font = itFont

            val gv = itFont.createGlyphVector(frc, itchar)
            val charWitdth = gv.getVisualBounds().getWidth()

            g.drawChars(itchar, 0, itchar.size, startPosX!!, pos_y_!!)
            startPosX = startPosX + charWitdth.toInt()
        }
    }

    private fun getRandomColor(): Color {
        return getRandomObject(_colors) as Color
    }

    private fun getRandomFont(): Font {
        return getRandomObject(_fonts) as Font
    }

    private fun getRandomObject(objs: ArrayList<*>): Any {
        if (objs.size == 1) {
            return objs[0]
        }

        val gen = SecureRandom()
        val i = gen.nextInt(objs.size)
        return objs[i]

    }
}