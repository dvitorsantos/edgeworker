package lsdi.edgeworker.Services;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public final class MqttService {
    private static MqttService instance;

    private MqttConnectOptions options;

    private MqttClient client;

    @Value("${mosquitto.url}")
    private String mosquittoUrl = System.getenv("MOSQUITTO_URL");

    @Value("${mosquitto.clientid}")
    private String clientUuid = System.getenv("EDGEWORKER_UUID");
    private static String CA_CRT_PATH = "src/main/java/lsdi/edgeworker/Certificates/ca.crt";
    private static String CLIENT_CRT_PATH = "src/main/java/lsdi/edgeworker/Certificates/client.crt";

    private MqttService() {
        try {
            InputStream clientCertificateInput = new FileInputStream(CLIENT_CRT_PATH);
            Certificate clientCertificate = CertificateFactory.getInstance("X.509").generateCertificate(clientCertificateInput);

            InputStream caCertificateInput = new FileInputStream(CA_CRT_PATH);
            Certificate caCertificate = CertificateFactory.getInstance("X.509").generateCertificate(caCertificateInput);

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, null);
            keyStore.setCertificateEntry("certificate", clientCertificate);
            keyStore.setCertificateEntry("ca-certificate", caCertificate);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, trustManagerFactory.getTrustManagers(), null);
            SSLSocketFactory socketFactory = context.getSocketFactory();

            options = new MqttConnectOptions();
            options.setSocketFactory(context.getSocketFactory());
            options.setUserName(this.clientUuid);
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(30000);
            options.setKeepAliveInterval(30);
            options.setSocketFactory(socketFactory);

            client = new MqttClient(this.mosquittoUrl, this.clientUuid);
            client.connect(options);
        } catch (MqttException | CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException |
                 KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    public static MqttService getInstance() {
        if (instance == null) instance = new MqttService();
        return instance;
    }

    public void publish(String topic, byte[] payload) {
        try {
            client.publish(topic, payload, 0, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic, IMqttMessageListener handler) {
        try {
            client.subscribe(topic, handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
