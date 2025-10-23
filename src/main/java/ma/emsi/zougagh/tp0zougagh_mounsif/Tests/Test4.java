package ma.emsi.zougagh.tp0zougagh_mounsif.Tests;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.time.Duration;

/**
 * Le RAG facile !
 */
public class Test4 {

    // Assistant conversationnel
    interface Assistant {
        // Prend un message de l'utilisateur et retourne une réponse du LLM.
        String chat(String userMessage);
    }

    public static void main(String[] args) {
        String apiKey = System.getenv("TP2_ZougaghMounsif");
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .modelName("gemini-2.5-flash")
                .temperature(0.3)// ≤ 0.3 pour des réponses plus précises
                .timeout(Duration.ofSeconds(5))
                .apiKey(apiKey)
                .build();

        // Chargement du document, sous la forme d'embeddings, dans une base vectorielle en mémoire
        String nomDocument = "src/main/java/ma/emsi/zougagh/tp0zougagh_mounsif/Tests/infos.txt";
        Document document = FileSystemDocumentLoader.loadDocument(nomDocument);
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        // Calcule les embeddings et les enregistre dans la base vectorielle
        EmbeddingStoreIngestor.ingest(document, embeddingStore);

        // Création de l'assistant conversationnel, avec une mémoire.
        // L'implémentation de Assistant est faite par LangChain4j.
        // L'assistant gardera en mémoire les 10 derniers messages.
        // La base vectorielle en mémoire est utilisée pour retrouver les embeddings.
        Assistant assistant =
                AiServices.builder(Assistant.class)
                        .chatModel(model)
                        .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                        .contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore))
                        .build();

        // Le LLM va utiliser l'information du fichier infos.txt pour répondre à la question.
        String question = "Comment s'appelle le chat de Pierre ?";
        // L'assistant recherche dans la base vectorielle les informations les plus pertinentes
        // pour répondre à la question, en comparant les embeddings de la base et celui de la question.
        // Ces informations sont ajoutées à la question et le tout est envoyé au LLM.
        String reponse = assistant.chat(question);
        // Affiche la réponse du LLM.
        System.out.println(reponse);
    }

}