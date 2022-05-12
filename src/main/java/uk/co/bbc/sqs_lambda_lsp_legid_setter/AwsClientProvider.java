package uk.co.bbc.sqs_lambda_lsp_legid_setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

/*
** A class for all the AWS client providers, and random other AWS methods.
*/

class AwsClientProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AwsClientProvider.class);

    private final Environment environment;

    AwsClientProvider(Environment environment) {
        this.environment = environment;
    }

    S3Client provideS3Client() {
        return S3Client.builder()
                .region(Region.of(environment.getRegion()))
                .build();
    }

    SnsClient provideSnsClient() {
        return SnsClient.builder()
                .region(Region.of(environment.getRegion()))
                .build();
    }

    SqsClient provideSqsClient() {
        return SqsClient.builder()
                .region(Region.of(environment.getRegion()))
                .build();
    }
}
