import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CrptApi {
    private final int requestLimit;
    private final long timeUnitMillis;
    private final HttpClient httpClient;
    private final Gson gson;
    private final BlockingQueue<Long> requestTimes;
    private final Lock lock;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        this.timeUnitMillis = timeUnit.toMillis(1);
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new GsonBuilder().create();
        this.requestTimes = new LinkedBlockingQueue<>(requestLimit);
        this.lock = new ReentrantLock();
    }

    public void createDocument(Document document, String signature) throws InterruptedException, IOException {
        lock.lock();
        try {
            ensureRateLimit();
            sendRequest(document, signature);
        } finally {
            lock.unlock();
        }
    }

    private void ensureRateLimit() throws InterruptedException {
        long now = System.currentTimeMillis();
        while (!requestTimes.isEmpty() && now - requestTimes.peek() > timeUnitMillis) {
            requestTimes.poll();
        }
        if (requestTimes.size() >= requestLimit) {
            long oldestRequestTime = requestTimes.take();
            long sleepTime = timeUnitMillis - (now - oldestRequestTime);
            if (sleepTime > 0) {
                Thread.sleep(sleepTime);
            }
        }
        requestTimes.add(now);
    }

    private void sendRequest(Document document, String signature) throws IOException, InterruptedException {
        String jsonBody = gson.toJson(document);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                .header("Content-Type", "application/json")
                .header("Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Failed to create document: " + response.body());
        }
    }


    public static void main(String[] args) {
        try {
            CrptApi api = new CrptApi(TimeUnit.SECONDS, 5);
            Document document = new Document();
            String signature = "your-signature";

            api.createDocument(document, signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static class Document {
        private Description description;

        private String docId;

        private String docStatus;

        private String docType;

        private boolean importRequest;

        private String ownerInn;

        private String participantInn;

        private String producerInn;

        private String productionDate;

        private String productionType;

        private List<Product> products;

        private String regDate;

        private String regNumber;

        public Description getDescription() {
            return description;
        }

        public void setDescription(Description description) {
            this.description = description;
        }

        public String getDocId() {
            return docId;
        }

        public void setDocId(String docId) {
            this.docId = docId;
        }

        public String getDocStatus() {
            return docStatus;
        }

        public void setDocStatus(String docStatus) {
            this.docStatus = docStatus;
        }

        public String getDocType() {
            return docType;
        }

        public void setDocType(String docType) {
            this.docType = docType;
        }

        public boolean isImportRequest() {
            return importRequest;
        }

        public void setImportRequest(boolean importRequest) {
            this.importRequest = importRequest;
        }

        public String getOwnerInn() {
            return ownerInn;
        }

        public void setOwnerInn(String ownerInn) {
            this.ownerInn = ownerInn;
        }

        public String getParticipantInn() {
            return participantInn;
        }

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }

        public String getProducerInn() {
            return producerInn;
        }

        public void setProducerInn(String producerInn) {
            this.producerInn = producerInn;
        }

        public String getProductionDate() {
            return productionDate;
        }

        public void setProductionDate(String productionDate) {
            this.productionDate = productionDate;
        }

        public String getProductionType() {
            return productionType;
        }

        public void setProductionType(String productionType) {
            this.productionType = productionType;
        }

        public List<Product> getProducts() {
            return products;
        }

        public void setProducts(List<Product> products) {
            this.products = products;
        }

        public String getRegDate() {
            return regDate;
        }

        public void setRegDate(String regDate) {
            this.regDate = regDate;
        }

        public String getRegNumber() {
            return regNumber;
        }

        public void setRegNumber(String regNumber) {
            this.regNumber = regNumber;
        }

        public static class Description {
            private String participantInn;
        }

        public static class Product {
            private String certificateDocument;

            private String certificateDocumentDate;

            private String certificateDocumentNumber;

            private String ownerInn;

            private String producerInn;

            private String productionDate;

            private String tnvedCode;

            private String uitCode;

            private String uituCode;

            public String getCertificateDocument() {
                return certificateDocument;
            }

            public void setCertificateDocument(String certificateDocument) {
                this.certificateDocument = certificateDocument;
            }

            public String getCertificateDocumentDate() {
                return certificateDocumentDate;
            }

            public void setCertificateDocumentDate(String certificateDocumentDate) {
                this.certificateDocumentDate = certificateDocumentDate;
            }

            public String getCertificateDocumentNumber() {
                return certificateDocumentNumber;
            }

            public void setCertificateDocumentNumber(String certificateDocumentNumber) {
                this.certificateDocumentNumber = certificateDocumentNumber;
            }

            public String getOwnerInn() {
                return ownerInn;
            }

            public void setOwnerInn(String ownerInn) {
                this.ownerInn = ownerInn;
            }

            public String getProducerInn() {
                return producerInn;
            }

            public void setProducerInn(String producerInn) {
                this.producerInn = producerInn;
            }

            public String getProductionDate() {
                return productionDate;
            }

            public void setProductionDate(String productionDate) {
                this.productionDate = productionDate;
            }

            public String getTnvedCode() {
                return tnvedCode;
            }

            public void setTnvedCode(String tnvedCode) {
                this.tnvedCode = tnvedCode;
            }

            public String getUitCode() {
                return uitCode;
            }

            public void setUitCode(String uitCode) {
                this.uitCode = uitCode;
            }

            public String getUituCode() {
                return uituCode;
            }

            public void setUituCode(String uituCode) {
                this.uituCode = uituCode;
            }
        }
    }
}
