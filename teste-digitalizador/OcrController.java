package com.example.demo;

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

@RestController
public class OcrController {

    private final String subscriptionKey = "SUA_CHAVE_DE_ASSINATURA";  // Substitua pela sua chave
    private final String ocrEndpoint = "https://SUA_INSTANCIA_DE_OCR.cognitiveservices.azure.com/vision/v3.2/ocr?language=pt&detectOrientation=true"; // Substitua pela URL correta

    @PostMapping(value = "/api/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String processImage(@RequestParam("file") MultipartFile file) throws IOException {
        // Salvar o arquivo temporariamente
        File tempFile = File.createTempFile("upload", file.getOriginalFilename());
        file.transferTo(tempFile);

        // Fazer a requisição para a API OCR do Azure
        FileEntity entity = new FileEntity(tempFile, ContentType.create("application/octet-stream"));

        String response = Request.post(ocrEndpoint)
                .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
                .body(entity)
                .execute()
                .returnContent()
                .asString();

        // Processar a resposta
        JSONObject jsonResponse = new JSONObject(response);
        StringBuilder extractedText = new StringBuilder();

        jsonResponse.getJSONArray("regions").forEach(region -> {
            JSONObject regionObject = (JSONObject) region;
            regionObject.getJSONArray("lines").forEach(line -> {
                JSONObject lineObject = (JSONObject) line;
                lineObject.getJSONArray("words").forEach(word -> {
                    extractedText.append(((JSONObject) word).getString("text")).append(" ");
                });
            });
        });

        // Apagar o arquivo temporário
        tempFile.delete();

        // Retornar o texto extraído
        return "{\"text\": \"" + extractedText.toString().trim() + "\"}";
    }
}
