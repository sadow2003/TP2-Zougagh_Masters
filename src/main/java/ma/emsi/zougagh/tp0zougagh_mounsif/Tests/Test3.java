package ma.emsi.zougagh.tp0zougagh_mounsif.Tests;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.*;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.CosineSimilarity;

import java.time.Duration;

public class Test3 {

    public static void main(String[] args) {

        // Remplacez par votre clé API Gemini
        String apiKey = System.getenv("TP2_ZougaghMounsif");

        // 1. Création du modèle d'embedding avec le pattern builder
        EmbeddingModel embeddingModel = GoogleAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("text-embedding-004") // Le modèle le plus récent
                .taskType(GoogleAiEmbeddingModel.TaskType.SEMANTIC_SIMILARITY) // Spécifié pour la similarité
                .timeout(Duration.ofSeconds(10))
                .logRequests(true) // Utile pour le débogage
                .logResponses(true)
                .build();

        // 2. Définition des couples de phrases
        String phrase1_sim = "Le roi est assis sur le trône.";
        String phrase2_sim = "Le monarque est sur son siège royal.";

        String phrase1_dissim = "Le chat dort paisiblement au soleil.";
        String phrase2_dissim = "La recette de la tarte aux pommes est facile.";

        String phrase1_theme = "Quel temps fait-il à Paris aujourd'hui ?";
        String phrase2_theme = "La météo annonce de la pluie sur la capitale.";

        // 3. Génération des embeddings (vecteurs)
        System.out.println("Génération des embeddings...");

        // Utilisation de .content() pour récupérer l'embedding de la réponse
        Embedding embedding1_sim = embeddingModel.embed(phrase1_sim).content();
        Embedding embedding2_sim = embeddingModel.embed(phrase2_sim).content();

        Embedding embedding1_dissim = embeddingModel.embed(phrase1_dissim).content();
        Embedding embedding2_dissim = embeddingModel.embed(phrase2_dissim).content();

        Embedding embedding1_theme = embeddingModel.embed(phrase1_theme).content();
        Embedding embedding2_theme = embeddingModel.embed(phrase2_theme).content();

        System.out.println("Calcul des similarités...");

        // 4. Calcul de la similarité cosinus
        double similarite_sim = CosineSimilarity.between(embedding1_sim, embedding2_sim);
        double similarite_dissim = CosineSimilarity.between(embedding1_dissim, embedding2_dissim);
        double similarite_theme = CosineSimilarity.between(embedding1_theme, embedding2_theme);

        // 5. Affichage des résultats
        System.out.println("--- Résultats de Similarité (plus c'est proche de 1, plus c'est similaire) ---");

        System.out.printf("\nCouple SIMILAIRE : (Score attendu : élevé)\n");
        System.out.printf("  \"%s\"\n  \"%s\"\n  => Similarité : %.4f\n", phrase1_sim, phrase2_sim, similarite_sim);

        System.out.printf("\nCouple NON SIMILAIRE : (Score attendu : bas)\n");
        System.out.printf("  \"%s\"\n  \"%s\"\n  => Similarité : %.4f\n", phrase1_dissim, phrase2_dissim, similarite_dissim);

        System.out.printf("\nCouple THÉMATIQUE : (Score attendu : moyen/élevé)\n");
        System.out.printf("  \"%s\"\n  \"%s\"\n  => Similarité : %.4f\n", phrase1_theme, phrase2_theme, similarite_theme);
    }
}