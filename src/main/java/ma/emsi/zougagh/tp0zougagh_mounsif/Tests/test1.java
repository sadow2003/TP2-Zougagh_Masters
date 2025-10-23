package ma.emsi.zougagh.tp0zougagh_mounsif.Tests;


import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

import java.lang.Object;


public class test1 {
    public static void main(String[] args) {

        // 1. Création de l'instance du modèle avec le Builder
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .modelName("gemini-2.5-flash")
                .temperature(0.7)
                .apiKey("AIzaSyByaLQcZU_VxoPDdBpW-ZdWp0A3Y3v4zfM")
                .build();

        // 2. Définition d'une question simple
        String question = "Quelle est la capitale de la France ?";
        System.out.println("Question : " + question);

        // 3. Envoi de la question au LLM et affichage de la réponse
        String reponse = model.chat(question);
        System.out.println("Réponse : " + reponse);
    }
}
