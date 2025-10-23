package ma.emsi.zougagh.tp0zougagh_mounsif.Tests;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;

import java.util.HashMap;
import java.util.Map;

public class Test2 {

    public static void main(String[] args) {

        // 1. Création de l'instance du modèle (comme au Test 1)
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .modelName("gemini-2.5-flash")
                .temperature(0.7)
                .apiKey("AIzaSyByaLQcZU_VxoPDdBpW-ZdWp0A3Y3v4zfM")
                .build();

        // 2. Définition du template de prompt
        // La variable est spécifiée avec la syntaxe {{nom_variable}}
        String templateTexte = "Traduis le texte suivant en anglais : {{texte}}";
        PromptTemplate promptTemplate = PromptTemplate.from(templateTexte);

        // 3. Création de la map pour les variables
        String texteATraduire = "Bonjour, j'apprends à utiliser LangChain4j, c'est formidable !";

        Map<String, Object> variables = new HashMap<>();
        variables.put("texte", texteATraduire);

        // 4. Création de l'objet Prompt en appliquant les variables au template
        Prompt prompt = promptTemplate.apply(variables);

        // 5. Envoi du Prompt au modèle et affichage de la réponse
        System.out.println("Texte original : " + texteATraduire);
        System.out.println("---");

        // Note: model.generate() accepte un String ou un objet Prompt
        String reponse = model.chat(String.valueOf(prompt));

        System.out.println("Traduction : " + reponse);
    }
}