import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Leitor {

    public static String readQrcode(String filePath) throws IOException, NotFoundException {
        BufferedImage bufferedImage = ImageIO.read(new File(filePath));
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();  
    }

    public static void main(String[] args) {
        try {
            String conteudo = readQrcode("qrcode.png");
            System.out.println("Conteúdo do QR code: " + conteudo);
        } catch (Exception e) {  
            System.err.println("Erro ao ler QR code: " + e.getMessage());
        }
    }
}