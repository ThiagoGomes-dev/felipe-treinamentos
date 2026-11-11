package br.com.felipetrainer.areamembros.midia;

import br.com.felipetrainer.areamembros.config.AppProperties;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/** Cliente do Cloudflare R2 (API compatível com S3). */
class ArmazenamentoR2 {

    private final String bucket;
    private final S3Client s3;
    private final S3Presigner presigner;

    ArmazenamentoR2(AppProperties.R2 r2) {
        this.bucket = r2.bucket();
        URI endpoint = URI.create("https://" + r2.contaId().trim() + ".r2.cloudflarestorage.com");
        var credenciais = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(r2.chaveAcesso(), r2.chaveSecreta()));
        var config = S3Configuration.builder().pathStyleAccessEnabled(true).build();
        this.s3 = S3Client.builder()
                .endpointOverride(endpoint)
                .region(Region.of("auto"))
                .credentialsProvider(credenciais)
                .serviceConfiguration(config)
                .httpClientBuilder(UrlConnectionHttpClient.builder()
                        .connectionTimeout(Duration.ofSeconds(20))
                        .socketTimeout(Duration.ofMinutes(10)))
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
        this.presigner = S3Presigner.builder()
                .endpointOverride(endpoint)
                .region(Region.of("auto"))
                .credentialsProvider(credenciais)
                .serviceConfiguration(config)
                .build();
    }

    String bucket() { return bucket; }

    void gravar(String chave, InputStream dados, long tamanho, String contentType) throws IOException {
        try (dados) {
            s3.putObject(PutObjectRequest.builder().bucket(bucket).key(chave).contentType(contentType).build(),
                    RequestBody.fromInputStream(dados, tamanho));
        }
    }

    void apagar(String chave) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(chave).build());
    }

    /** Link temporário direto para o arquivo no R2. */
    String link(String chave, Duration validade) {
        return presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(validade)
                        .getObjectRequest(g -> g.bucket(bucket).key(chave))
                        .build())
                .url().toString();
    }
}
