package br.com.atividades.image;

import br.com.atividades.dto.Mensagem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.regex.Pattern;

public class Image {

    public static final String IMAGE_CODE = "#image";
    public static final String WHITESPACE_REGEX = "\\s";
    public static final String RESOURCE_PATH = "C:\\repos\\routers-tf\\src\\br\\com\\atividades\\image\\";
    public static final String IMAGE_PATH = "C:\\repos\\routers-tf\\src\\br\\com\\atividades\\image\\lotr.jpg";
    public static final Pattern BASE64_PATTERN = Pattern.compile("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$");

    //codigica o jpg em base64
    public static String encodedImageToBase64() throws IOException {
        File fnew = new File(IMAGE_PATH);
        BufferedImage originalImage = ImageIO.read(fnew);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(originalImage, "jpg", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    //decodifica o base64 em jpg e salva no diretório
    public static void decodeImageToBase64(Mensagem mensagem) throws IOException {
        String nameFile = String.format("%s_%s_%s.jpg", mensagem.getPortaOrigem(), mensagem.getPortaDestino(), System.currentTimeMillis());
        byte[] data = Base64.getDecoder().decode(mensagem.getBase64Image());
        InputStream is = new ByteArrayInputStream(data);
        BufferedImage newBi = ImageIO.read(is);
        ImageIO.write(newBi, "jpg", new File(RESOURCE_PATH + nameFile));
        System.out.format("%s salvo com sucesso\n", nameFile);
    }

    //verifica se a mensagem possui um formato de base64
    public static boolean isValidBase64(final String s) {
        final String sanitized = s.replaceAll(WHITESPACE_REGEX, "");
        return BASE64_PATTERN.matcher(sanitized).matches();
    }
}
