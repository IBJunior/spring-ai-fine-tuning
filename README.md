# Spring AI : Génération des données synthétiques avec OpenAI pour le fine-tuning

Ce repos contient le code qui permet de générer les données synthétiques pour
le [fine-tuning avec OpenAI](https://platform.openai.com/docs/guides/fine-tuning) en utilisant Java et Spring AI.

Les données générées finales seront en fichier [jsonl](https://jsonlines.org/) ressemblant à :
````json lines
{"messages":[{"role":"system","content":"Tu es un assistant qui fournit des informations concises et précises sur\nla compagnie aérienne Fly Intelligent en fonction des requêtes des utilisateurs\n"},{"role":"user","content":"Quelle est la vision de Fly Intelligent pour l'avenir de l'aviation ?"},{"role":"assistant","content":"La vision de Fly Intelligent est de redéfinir l'avenir de l'aviation en intégrant des pratiques durables à tous les niveaux de ses opérations."}]}
{"messages":[{"role":"system","content":"Tu es un assistant qui fournit des informations concises et précises sur\nla compagnie aérienne Fly Intelligent en fonction des requêtes des utilisateurs\n"},{"role":"user","content":"Pourquoi les voyageurs devraient-ils choisir Fly Intelligent ?"},{"role":"assistant","content":"En choisissant Fly Intelligent, les voyageurs peuvent profiter d'une expérience de vol confortable et moderne tout en contribuant à un monde plus durable."}]}
````

## Données de départ

Au départ, on part d'un ensemble de documents PDF (`src/main/resources/documents`) et ensuite pour chaque page du
document PDF, on demande au modèle de
nous générer un nombre de couples (question, réponse) sous le format `jsonl` ci-dessus
confère `src/main/resources/fine-tuning-prompt-template.st` pour voir les détails du prompt.

## Dépendances

* OpenAI (une clé API est nécessaire)

````xml

<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>
````

* PDF Document Reader pour extraire les contenus des documents PDF

````xml

<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-pdf-document-reader</artifactId>
</dependency>
````

* Gson pour convertir les objets java en json

````xml

<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.11.0</version>
</dependency>
````

* Et cette dépendance que j'ai trouvée qui permet de calculer le nombre de tokens d'un prompt. Avant d'envoyer le prompt
  au
  modèle, on doit se rassurer de ne pas dépasser le nombre maximal token.

````xml

<dependency>
    <groupId>com.knuddels</groupId>
    <artifactId>jtokkit</artifactId>
    <version>1.1.0</version>
</dependency>
````

## Structure du projet

### Conversation

La Classe Conversation représente une conversation une ligne du jsonl final, avec le message utilisateur, système et
assistant.

````java
public class Conversation {
    private List<Message> messages;

    //Getters and Setters
    //...
    public static class Message {
        private String role;
        private String content;
        //Getters and Setters
    }
}
````

### FineTuningDataService

Ce service permet d'appeler le modèle pour chaque page de document pour générer les questions à utiliser pour faire le
fine-tuning.

````java

@Service
public class FineTuningDataService {
    //code
    public List<Conversation> getConversations() throws InterruptedException {
        //Code...
        PromptTemplate promptTemplate = new PromptTemplate(promptResource);

        for (Document document : documents) {

            Prompt prompt = promptTemplate.create(
                    Map.of("numberOfConversation", "6",
                            "textPassage", document.getContent())
            );
            //code...
            String content = this.chatClient
                    .prompt(prompt)
                    .call()
                    .content();
            //code...        
        }
        return allConversations;
    }
    //code
}
````

### FineTuningDataCreator

Appelle `FineTuningDataService`, ensuite enregistre les données dans le fichier `jsonl`

````java

@Service
public class FineTuningDataCreator {
    //code...
    public void run() throws InterruptedException {
        log.info("Creating synthetic data started...");

        List<Conversation> conversations = fineTuningDataService.getConversations();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        for (Conversation conversation : conversations) {
            String json = gson.toJson(conversation);
            writeToJsonFile(json.getBytes(StandardCharsets.UTF_8));
        }

        log.info("Created {} conversations", conversations.size());
        log.info("Creating synthetic data ended...");
    }
    //code...

}

````