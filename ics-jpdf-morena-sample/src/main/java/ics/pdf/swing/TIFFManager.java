package ics.pdf.swing;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.NullOpImage;
import javax.media.jai.OpImage;
import javax.media.jai.PlanarImage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.media.imageio.plugins.tiff.TIFFImageWriteParam;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFDecodeParam;

public class TIFFManager {
    static Logger log = LogManager.getLogger(TIFFManager.class.getName());

    public static List<BufferedImage> getImageListFromTiff(String inputFile) {
        BufferedImage finalImage = null;
        List<BufferedImage> imageList = null;
        try {
            File file = new File(inputFile);
            SeekableStream s = new FileSeekableStream(file);

            TIFFDecodeParam param = null;

            ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param);
            System.out.println("Number of images in this TIFF: " + dec.getNumPages());

            imageList = new ArrayList<BufferedImage>();
            int numPages = dec.getNumPages();
            for (int i = 0; i < numPages; i++) {
                RenderedImage ri = dec.decodeAsRenderedImage(i);
                PlanarImage op = new NullOpImage(ri, null, null, OpImage.OP_IO_BOUND);
                BufferedImage bufferedImage = op.getAsBufferedImage();
                imageList.add(bufferedImage);
            }

            // Convert TIFF file multiple pages into JPEG file
            // finalImage = verticalMerge(numPages, imageList.toArray(new BufferedImage[numPages]));
            // OutputStream out = new FileOutputStream(outputFile);
            // JPEGEncodeParam params = new JPEGEncodeParam();
            // ImageEncoder encoder = ImageCodec.createImageEncoder("jpeg", out, params);
            // encoder.encode(finalImage);
            // out.close();

        } catch (IOException ex) {
            // ex.printStackTrace();
            log.fatal("ERROR", ex);
        }
        return imageList;
    }

    protected static BufferedImage verticalMerge(int rows, BufferedImage[] buffImages) {
        int cols = 1;
        int type = buffImages[0].getType();
        int chunkWidth = buffImages[0].getWidth();
        int finalHeight = 0;
        for (int i = 0; i < rows; i++) {
            finalHeight += buffImages[i].getHeight();
        }

        // Initializing the final image
        BufferedImage finalImg = new BufferedImage(chunkWidth * cols, finalHeight, type);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int chunkHeight = buffImages[i].getHeight();
                finalImg.createGraphics().drawImage(buffImages[i], chunkWidth * j, chunkHeight * i, null);
            }
        }

        return finalImg;
    }

    public static byte[] createTiffFile(List<BufferedImage> thisImage) {
        byte[] thisByteArray = null;
        if (thisImage != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
                boolean foundWriter = false;

                for (Iterator<ImageWriter> writerIter = ImageIO.getImageWritersByFormatName("tif"); writerIter
                        .hasNext() && !foundWriter;) {
                    foundWriter = true;
                    ImageWriter writer = writerIter.next();
                    writer.setOutput(ios);
                    TIFFImageWriteParam writeParam = (TIFFImageWriteParam) writer.getDefaultWriteParam();
                    writeParam.setCompressionMode(ImageWriteParam.MODE_DISABLED);
                    writer.prepareWriteSequence(null);

                    for (BufferedImage image : thisImage) {
                        ImageTypeSpecifier spec = ImageTypeSpecifier.createFromRenderedImage(image);
                        javax.imageio.metadata.IIOMetadata metadata = writer.getDefaultImageMetadata(spec, writeParam);
                        IIOImage iioImage = new IIOImage(image, null, metadata);
                        writer.writeToSequence(iioImage, writeParam);
                        image.flush();
                    }
                    writer.endWriteSequence();
                    ios.flush();
                    writer.dispose();
                    ios.close();
                    thisByteArray = baos.toByteArray();
                    baos.close();
                }
            } catch (IOException e) {
                // e.printStackTrace();
                log.fatal("ERROR", e);
                return null;
            }
        }
        return thisByteArray;

    }

    public static byte[] compressTIFFList(List<BufferedImage> imagesList) {
        byte[] thisByteArray = null;
        if (imagesList != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
                boolean foundWriter = false;

                for (Iterator<ImageWriter> writerIter = ImageIO.getImageWritersByFormatName("tif"); writerIter
                        .hasNext() && !foundWriter;) {
                    foundWriter = true;
                    ImageWriter writer = writerIter.next();
                    writer.setOutput(ios);
                    TIFFImageWriteParam writeParam = (TIFFImageWriteParam) writer.getDefaultWriteParam();
                    // writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    writeParam.setCompressionMode(ImageWriteParam.MODE_DISABLED);
                    // writeParam.setCompressionType(compressionType);
                    // writeParam.setCompressionQuality(1.0f);
                    writer.prepareWriteSequence(null);

                    for (BufferedImage image : imagesList) {

                        // int width = image.getWidth(null);
                        // int height = image.getHeight(null);
                        // BufferedImage bimg = new BufferedImage(width, height,
                        // BufferedImage.TYPE_BYTE_GRAY);
                        //
                        // bimg.createGraphics().drawImage(image.getScaledInstance(width, height,
                        // Image.SCALE_SMOOTH), 0,
                        // 0, null);

                        int pixelSize = image.getColorModel().getPixelSize();
                        if (pixelSize == 1) {
                            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                            writeParam.setCompressionType("CCITT T.4");
                            log.debug("TIFF Compression type was [CCITT T.4] - pixel size was 1");
                        } else if (pixelSize == 8) {
                            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                            writeParam.setCompressionType("Deflate");
                            log.debug("TIFF Compression type was [Deflate] - pixel size was 8");
                        } else if (pixelSize == 24) {
                            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                            writeParam.setCompressionType("JPEG");
                            log.debug("TIFF Compression type was [JPEG] - pixel size was 24");
                        }

                        ImageTypeSpecifier spec = ImageTypeSpecifier.createFromRenderedImage(image);
                        javax.imageio.metadata.IIOMetadata metadata = writer.getDefaultImageMetadata(spec, writeParam);
                        IIOImage iioImage = new IIOImage(image, null, metadata);
                        writer.writeToSequence(iioImage, writeParam);
                        image.flush();
                    }
                    writer.endWriteSequence();
                    ios.flush();
                    writer.dispose();
                    ios.close();
                    thisByteArray = baos.toByteArray();
                    baos.close();
                }
            } catch (IOException e) {
                // e.printStackTrace();
                log.fatal("ERROR", e);
                return null;
            }
        }
        return thisByteArray;
    }

    public static byte[] compressTIFFFileList(List<String> imagesFileList) {
        byte[] thisByteArray = null;
        if (imagesFileList != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
                boolean foundWriter = false;

                for (Iterator<ImageWriter> writerIter = ImageIO.getImageWritersByFormatName("tif"); writerIter
                        .hasNext() && !foundWriter;) {
                    foundWriter = true;
                    ImageWriter writer = writerIter.next();
                    writer.setOutput(ios);
                    TIFFImageWriteParam writeParam = (TIFFImageWriteParam) writer.getDefaultWriteParam();
                    // writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    writeParam.setCompressionMode(ImageWriteParam.MODE_DISABLED);
                    // writeParam.setCompressionType(compressionType);
                    // writeParam.setCompressionQuality(1.0f);
                    writer.prepareWriteSequence(null);

                    for (String imageName : imagesFileList) {

                        BufferedImage image = ImageIO.read(new File(imageName));

                        // int width = image.getWidth(null);
                        // int height = image.getHeight(null);
                        // BufferedImage bimg = new BufferedImage(width, height,
                        // BufferedImage.TYPE_BYTE_GRAY);
                        //
                        // bimg.createGraphics().drawImage(image.getScaledInstance(width, height,
                        // Image.SCALE_SMOOTH), 0,
                        // 0, null);

                        int pixelSize = image.getColorModel().getPixelSize();
                        if (pixelSize == 1) {
                            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                            writeParam.setCompressionType("CCITT T.4");
                            log.debug("TIFF Compression type was [CCITT T.4] - pixel size was 1");
                        } else if (pixelSize == 8) {
                            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                            writeParam.setCompressionType("Deflate");
                            log.debug("TIFF Compression type was [Deflate] - pixel size was 8");
                        } else if (pixelSize == 24) {
                            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                            writeParam.setCompressionType("JPEG");
                            log.debug("TIFF Compression type was [JPEG] - pixel size was 24");
                        }

                        ImageTypeSpecifier spec = ImageTypeSpecifier.createFromRenderedImage(image);
                        javax.imageio.metadata.IIOMetadata metadata = writer.getDefaultImageMetadata(spec, writeParam);
                        IIOImage iioImage = new IIOImage(image, null, metadata);
                        writer.writeToSequence(iioImage, writeParam);
                        image.flush();
                    }
                    writer.endWriteSequence();
                    // ios.flush();
                    writer.dispose();
                    ios.close();
                    thisByteArray = baos.toByteArray();
                    baos.close();
                }
            } catch (IOException e) {
                // e.printStackTrace();
                log.fatal("ERROR", e);
                return null;
            }
        }
        return thisByteArray;
    }
}
