package bootiful.singularity;

import org.springframework.ai.client.AiClient;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.ai.prompt.Prompt;
import org.springframework.ai.prompt.PromptTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class SingularityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SingularityApplication.class, args);
    }

    private void one(Resource resource, AiClient aiClient) {
        var promptTemplate = new PromptTemplate(resource);
        var prompt = promptTemplate.create(Map.of("adjective", "funny", "topic", "cows"));
        var aiResponse = aiClient.generate(prompt);
        System.out.println("provider output");
        aiResponse.getProviderOutput().forEach((k, v) -> System.out.println(k + '=' + v));
        System.out.println("run info");
        aiResponse.getRunInfo().forEach((k, v) -> System.out.println(k + '=' + v));
        System.out.println("generation:" + aiResponse.getGeneration());
    }


    private void two(AiClient aiClient) throws Exception {

        var bop = new BeanOutputParser<>(ActorsFilms.class);
        var formatString = bop.getFormat();
        System.out.println("format: " + formatString);
        var userMessage = """
                Generate the filmography for the actor {actor}.
                {format}
                """;
        var pt = new PromptTemplate(userMessage, Map.of("format", formatString, "actor",
                "Jeff Bridges"));
        var prompt = new Prompt(pt.createMessage());
        var generation = aiClient.generate(prompt).getGeneration();
        var actorFilmsResults = bop.parse(generation.getText());
        System.out.println("actorFilmResults: " + actorFilmsResults.toString());

    }

    private void three(Resource resource, AiClient aiClient) {

        var qaPrompt = """
                Use the following pieces of context to answer the question at the end. If you don't know the answer, just say that you don't know, don't try to make up an answer.
                                    
                {context} 
                                    
                Question : {question}
                Helpful answer: 
                """;
        var pt = new PromptTemplate(qaPrompt);
        var q = "which athletes won the gold medal in curling at the 2022 Winter Olympics?";
        var prompt = pt.create(Map.of("question", q, "context", resource));
        System.out.println(aiClient.generate(prompt).getGeneration());
    }

    @Bean
    ApplicationRunner applicationRunner(
            AiClient aiClient) {
        return args -> {
            one(new ClassPathResource("/joke-prompt.st"), aiClient);
            two(aiClient);
            three(new ClassPathResource("/wikipedia-curling.md"), aiClient);
        };
    }
}

record ActorsFilms(String actor, List<String> movies) {
}
