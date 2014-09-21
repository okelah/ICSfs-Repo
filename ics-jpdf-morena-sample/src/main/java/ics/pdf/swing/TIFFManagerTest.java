package ics.pdf.swing;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFDecodeParam;
import com.sun.media.jai.codec.TIFFEncodeParam;

public class TIFFManagerTest {

    static Logger log = LogManager.getLogger(TIFFManagerTest.class.getName());

    File inputFile = new File("C:\\morena_img.tif");

    public static void main(String[] args) throws IOException {
        try {
            new TIFFManagerTest().test1();
            new TIFFManagerTest().test2();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void test1() throws Exception {
        String fileName = "C:\\morena_img-color";

        List<BufferedImage> images = TIFFManager.getImageListFromTiff(inputFile.getAbsolutePath());

        FileOutputStream fos = null;
        fos = new FileOutputStream(fileName + "-CCITT T.6.tif");

        fos = new FileOutputStream(fileName + "-Deflate.tif");
        fos.write(TIFFManager.compressTIFFList(images));
        fos.close();

        System.out.println("Don.");
    }

    public void test2() throws Exception {
        SeekableStream s = new FileSeekableStream(inputFile);

        TIFFDecodeParam param = new TIFFDecodeParam();
        param.setJPEGDecompressYCbCrToRGB(true);

        ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param);
        System.out.println("Number of images in this TIFF: " + dec.getNumPages());

        ArrayList<BufferedImage> imageList = new ArrayList<BufferedImage>();
        int numPages = dec.getNumPages();
        for (int i = 0; i < numPages; i++) {
            RenderedImage ri = dec.decodeAsRenderedImage(i);
            PlanarImage op = new NullOpImage(ri, null, null, OpImage.OP_IO_BOUND);
            BufferedImage bufferedImage = op.getAsBufferedImage();

            // imageList.add(bufferedImage);
            // convert image to gray scale image -- start
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
            ColorConvertOp opt = new ColorConvertOp(cs, null);
            imageList.add(opt.filter(bufferedImage, null));
            // convert image to gray scale image -- end
        }

        TIFFEncodeParam params = new TIFFEncodeParam();
        // params.setCompression(TIFFEncodeParam.COMPRESSION_GROUP4);
        // params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        // params.setCompressionType(compressionType);
        // params.setCompressionQuality(1.0f);
        OutputStream out = new FileOutputStream("C:/del.tif");
        ImageEncoder encoder = ImageCodec.createImageEncoder("tiff", out, params);

        params.setExtraImages(imageList.iterator());
        encoder.encode(imageList.get(0));
        out.close();

        System.out.println("Done.");
    }

    public static byte[] compressTIFFList(List<BufferedImage> thisImage, String compressionType) {
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
                    writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    writeParam.setCompressionType(compressionType);
                    writeParam.setCompressionQuality(1.0f);
                    writer.prepareWriteSequence(null);

                    for (BufferedImage image : thisImage) {
                        BufferedImage bimg = new BufferedImage(image.getWidth(null), image.getHeight(null),
                            BufferedImage.TYPE_BYTE_BINARY);

                        bimg.createGraphics().drawImage(image, 0, 0, null);
                        ImageTypeSpecifier spec = ImageTypeSpecifier.createFromRenderedImage(bimg);
                        javax.imageio.metadata.IIOMetadata metadata = writer.getDefaultImageMetadata(spec, writeParam);
                        IIOImage iioImage = new IIOImage(bimg, null, metadata);
                        writer.writeToSequence(iioImage, writeParam);
                        bimg.flush();
                    }
                    writer.endWriteSequence();
                    ios.flush();
                    writer.dispose();
                    ios.close();
                    thisByteArray = baos.toByteArray();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return thisByteArray;
    }

}
