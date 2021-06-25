# roteador-java
- [ ] Enviar arquivos (imagem) 
- [ ] Algoritmo para atualiza a tabela de roteamento (Incluir e excluir)
- [ ] Testar roteamento de mensagens (dá para fazer na mão a config)
- [ ] Apos a tabela de roteamento estar OK, testar o código para possiveis problemas de paralelismo

Existem algumas partes do codigo marcados como TODO, que tem ideias e dicas do que poderia ser feito

### Trechos de código que podem ajudar a dev a tarefa

example

```java
if (message.contains("#image")) {
                messageBytes = (message + " " + encodedImageToBase64()).getBytes();
                DatagramPacket newPacket = new DatagramPacket(messageBytes, 0, messageBytes.length, address, 9000);
                socket.send(newPacket);
                
     //codigica o jpg em base64
    static String encodedImageToBase64() throws IOException {
        File fnew = new File(IMAGE_PATH);
        BufferedImage originalImage = ImageIO.read(fnew);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(originalImage, "jpg", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    //decodifica o base64 em jpg e salva no diretório
    static void decodeImageToBase64(String encodedString) throws IOException {
        String nameFile = String.format("lotr_%s.jpg", System.currentTimeMillis());
        byte[] data = Base64.getDecoder().decode(encodedString);
        InputStream is = new ByteArrayInputStream(data);
        BufferedImage newBi = ImageIO.read(is);
        ImageIO.write(newBi, "jpg", new File(RESOURCE_PATH + nameFile));

    //verifica se a mensagem possui um formato de base64
    static boolean isValidBase64(final String s) {
        final String sanitized = s.replaceAll(WHITESPACE_REGEX, "");
        return BASE64_PATTERN.matcher(sanitized).matches();
    }
    
    //mesangem
     if (response.contains(IMAGE_CODE) && isValidBase64(response.split(WHITESPACE_REGEX)[3])) {
                            //decodifica o base64 em imagem e salva no diretório
                            decodeImageToBase64(response.split(WHITESPACE_REGEX)[3]);
                            

```
