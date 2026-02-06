package fr.takima.oms.temporal;

import com.google.protobuf.ByteString;
import io.temporal.api.common.v1.Payload;
import io.temporal.payload.codec.PayloadCodec;
import io.temporal.payload.codec.PayloadCodecException;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;

import javax.annotation.Nonnull;
import java.util.List;

public class TemporalEncryptionCodec implements PayloadCodec {

    private static final String ENCODING_KEY = "encoding";
    private static final String ENCRYPTED_ENCODING = "binary/encrypted";

    public TemporalEncryptionCodec(
            String password,
            String salt
    ) {
        this.bytesEncryptor = Encryptors.standard(password, salt);
    }

    private final BytesEncryptor bytesEncryptor;

    @Override
    @Nonnull
    public List<Payload> encode(@Nonnull List<Payload> payloads) {
        return payloads.stream()
                .map(this::encodePayload)
                .toList();
    }

    @Override
    @Nonnull
    public List<Payload> decode(@Nonnull List<Payload> payloads) {
        return payloads.stream()
                .map(this::decodePayload)
                .toList();
    }

    private Payload encodePayload(Payload payload) {
        byte[] encryptedData;

        try {
            encryptedData = bytesEncryptor.encrypt(payload.toByteArray());
        } catch (Exception e) {
            throw new PayloadCodecException(e);
        }

        return Payload.newBuilder()
                .setData(ByteString.copyFrom(encryptedData))
                .putMetadata(ENCODING_KEY, ByteString.copyFromUtf8(ENCRYPTED_ENCODING))
                .build();
    }

    private Payload decodePayload(Payload payload) {
        if (!ENCRYPTED_ENCODING.equals(getMetadata(payload, ENCODING_KEY))) {
            return payload;
        }
        try {
            byte[] decryptedData = bytesEncryptor.decrypt(payload.getData().toByteArray());
            return Payload.parseFrom(decryptedData);
        } catch (Exception e) {
            throw new PayloadCodecException(e);
        }
    }

    private String getMetadata(Payload payload, String key) {
        ByteString value = payload.getMetadataOrDefault(key, ByteString.EMPTY);
        return value.isEmpty() ? null : value.toStringUtf8();
    }
}
