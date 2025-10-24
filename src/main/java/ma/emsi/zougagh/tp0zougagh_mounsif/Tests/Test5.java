package ma.emsi.zougagh.tp0zougagh_mounsif.Tests;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.*;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.IngestionResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Scanner;

public class Test5 {

    // IMPORTANT : Modifiez ceci avec le nom exact de votre PDF
    private static final String NOM_FICHIER_PDF = "agentsmcp.pdf";

    /**
     * Interface de l'assistant, utilisée par AiServices.
     * Le RAG sera "injecté" automatiquement.
     */
    interface Assistant {
        String chat(String message);
    }

    public static void main(String[] args) {
        try {
            // Configuration complète de l'assistant RAG
            Assistant assistant = setupRagAssistant();

            // Démarrage de la boucle de conversation
            System.out.println("✅ Assistant RAG prêt. Il a lu le document " + NOM_FICHIER_PDF);

            // Utilisation de votre boucle de scanner
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.println("\n==================================================");
                    System.out.println("Posez votre question (ou 'fin' pour quitter) : ");
                    String question = scanner.nextLine();
                    if (question.isBlank()) {
                        continue;
                    }
                    System.out.println("==================================================");
                    if ("fin".equalsIgnoreCase(question)) {
                        break;
                    }

                    System.out.println("🧠 Recherche d'informations pertinentes...");
                    String reponse = assistant.chat(question);

                    System.out.println("\nAssistant : " + reponse);
                    System.out.println("==================================================");
                }
            }
            System.out.println("Fin de la session.");

        } catch (Exception e) {
            System.err.println("Une erreur est survenue lors de l'initialisation :");
            e.printStackTrace();
        }
    }

    /**
     * Configure et retourne un Assistant (ChatBot)
     * qui utilise le RAG avec le document PDF spécifié.
     */
    private static Assistant setupRagAssistant() throws URISyntaxException {

        String apiKey = System.getenv("TP2_ZougaghMounsif");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("La variable d'environnement GEMINI_API_KEY n'est pas définie.");
        }

        // 1. Configurer les modèles (Chat et Embedding)
        System.out.println("⚙️ Configuration des modèles Google (Chat + Embedding)...");
        ChatModel chatModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-pro") // Un modèle rapide et efficace
                .timeout(Duration.ofSeconds(60))
                .build();

        EmbeddingModel embeddingModel = GoogleAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("embedding-001")
                .taskType(GoogleAiEmbeddingModel.TaskType.RETRIEVAL_DOCUMENT) // Important: optimisé pour la recherche
                .timeout(Duration.ofSeconds(60))
                .build();

        // 2. Créer la base de données vectorielle en mémoire
        System.out.println("💾 Création de la base vectorielle en mémoire...");
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        // 3. Charger le document PDF depuis la racine du projet
        System.out.println("Loading PDF: " + NOM_FICHIER_PDF);
        Path pdfPath = Paths.get("agentsmcp.pdf");
        DocumentParser documentParser= new ApachePdfBoxDocumentParser();
        Document document= FileSystemDocumentLoader.loadDocument(pdfPath, documentParser);
        //Document document = FileSystemDocumentLoader.loadDocument(pdfPath, new ApachePdfBoxDocumentParser());
        System.out.println("Document PDF chargé. (Total " + document.text().length() + " caractères)");


        // 4. Découper le document (chunking) et l'ingérer dans l'EmbeddingStore
        // On découpe en segments de 500 caractères, avec 50 caractères de chevauchement (overlap)
        // L'overlap aide à ne pas couper une idée en plein milieu entre deux chunks.
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);

        System.out.println("Découpage et ingestion des segments dans la base vectorielle (cela peut prendre un moment)...");
        EmbeddingStoreIngestor ingestor =
                EmbeddingStoreIngestor.builder()
                        .documentSplitter(splitter)
                        .embeddingModel(embeddingModel)
                        .embeddingStore(embeddingStore)
                        .build();
        ingestor.ingest(document);
        System.out.println("✅ Ingestion terminée.");

        // 5. Créer le "ContentRetriever"
        // C'est le composant qui va chercher les N chunks les plus pertinents
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3) // On récupère les 3 chunks les plus similaires
                .minScore(0.6) // Seuil de similarité (optionnel mais recommandé)
                .build();

        // 6. Créer la mémoire de conversation
        // Permet à l'assistant de se souvenir des questions/réponses précédentes
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        // 7. Construire l'AiService (notre Assistant)
        // On lui "injecte" le modèle de chat, le retriever (RAG) et la mémoire.
        return AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .contentRetriever(contentRetriever)
                .chatMemory(chatMemory)
                .build();
    }
}