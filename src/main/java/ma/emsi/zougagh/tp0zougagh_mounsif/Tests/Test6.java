package ma.emsi.zougagh.tp0zougagh_mounsif.Tests;

import dev.langchain4j.model.chat.ChatModel; // Renommé par rapport à votre Test 5
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel; // Votre classe de Test 5
import dev.langchain4j.service.AiServices;
import ma.emsi.zougagh.tp0zougagh_mounsif.services.AssistantMeteo; // Votre interface
import ma.emsi.zougagh.tp0zougagh_mounsif.tools.meteo.MeteoTool; // Votre outil

import java.time.Duration;

public class Test6 {

    public static void main(String[] args) {

        // Utilisez la variable d'environnement de votre Test 5
        String apiKey = System.getenv("TP2_ZougaghMounsif");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("La variable d'environnement TP2_ZougaghMounsif n'est pas définie.");
        }

        // 1. Créer le ChatModel avec le logging activé
        ChatModel model = GoogleAiGeminiChatModel.builder() // En gardant votre classe
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash")
                .timeout(Duration.ofSeconds(30))

                // C'est la ligne clé pour le logging JSON !
                .logRequests(true)
                .logResponses(true)
                .build();

        // 2. Créer l'outil
        MeteoTool meteoTool = new MeteoTool();

        // 3. Créer l'assistant en lui fournissant l'outil
        AssistantMeteo assistant = AiServices.builder(AssistantMeteo.class)
                .chatModel(model)
                .tools(meteoTool) // Injection de l'outil
                .build();

        // 4. Lancer les tests
        System.out.println("==================================================");
        System.out.println("🤖 TEST 1: Requête météo explicite (Paris)");
        System.out.println("==================================================");
        String reponse1 = assistant.chat("Quel temps fait-il à Paris ?");
        System.out.println("Réponse Assistant : " + reponse1);

        System.out.println("\n==================================================");
        System.out.println("🤖 TEST 2: Requête météo implicite (Londres)");
        System.out.println("==================================================");
        String reponse2 = assistant.chat("Je pars à Londres aujourd'hui. Dois-je prendre un parapluie ?");
        System.out.println("Réponse Assistant : " + reponse2);

        System.out.println("\n==================================================");
        System.out.println("🤖 TEST 3: Ville qui n'existe pas (Pétaouchnok)");
        System.out.println("==================================================");
        String reponse3 = assistant.chat("Donne-moi la météo pour Pétaouchnok.");
        System.out.println("Réponse Assistant : " + reponse3);

        System.out.println("\n==================================================");
        System.out.println("🤖 TEST 4: Requête sans rapport (Poème)");
        System.out.println("==================================================");
        String reponse4 = assistant.chat("Écris-moi un court poème sur un chat.");
        System.out.println("Réponse Assistant : " + reponse4);
    }
}
