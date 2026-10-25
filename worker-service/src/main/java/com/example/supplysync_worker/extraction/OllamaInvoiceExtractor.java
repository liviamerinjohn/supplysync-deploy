package com.example.supplysync_worker.extraction;

import com.example.supplysync_worker.dto.InvoiceExtractionResult;
import com.example.supplysync_worker.entity.Document;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.util.Locale;

@Service
@ConditionalOnProperty(name="supplysync.extractor",havingValue="ollama")
public class OllamaInvoiceExtractor implements InvoiceExtractor{
    private static final String PROMPT =
            "You are reading a supplier invoice image. Reply with ONLY a JSON object, no markdown, "
                    + "with exactly these keys: invoiceNumber (string), "
                    + "vendor (the company that issued the invoice, not the customer), "
                    + "totalAmount (the final total payable as a number with exactly "
                    + "two decimal places, e.g. 1234.50, no currency symbol, no commas), "
                    + "invoiceDate (the invoice date in YYYY-MM-DD format). "
                    + "Use null for anything not visible.";

    private final ChatClient chatClient;

    public OllamaInvoiceExtractor(ChatModel chatModel) {
        this.chatClient = ChatClient.create(chatModel);
    }
    @Override
    public InvoiceExtractionResult extract(Document document) {
        FileSystemResource resource = new FileSystemResource(document.getFilePath());
        if (!resource.exists()) {
            throw new IllegalStateException("File not found: " + document.getFilePath());
        }

        MimeType mimeType = mimeTypeFor(document.getFilePath());

        long start = System.currentTimeMillis();
        String raw = chatClient.prompt()
                .user(u -> u.text(PROMPT).media(mimeType, resource))
                .call()
                .content();
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("RAW ANSWER (" + elapsed + " ms): " + raw);

        return InvoiceAnswerParser.parse(raw);

        //throw new IllegalStateException("Parsing not implemented yet");
    }

    private MimeType mimeTypeFor(String path) {
        String lowerPath = path.toLowerCase(Locale.ROOT);

        if (lowerPath.endsWith(".png")) {
            return MimeTypeUtils.IMAGE_PNG;
        }

        if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return MimeTypeUtils.IMAGE_JPEG;
        }

        throw new IllegalStateException("Unsupported file type: " + path);
    }
}
