package com.spring.petDoctor.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.petDoctor.DTO.ThreadInfo;
import com.spring.petDoctor.config.GPTConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class GPTServiceImpl implements GPTService{

    private final static String ASSISTANT_ID = "asst_9sZSXMHcaGeNCHvYvRSRbk41";

    @Autowired
    private final GPTConf gptConf;

    public GPTServiceImpl(GPTConf gptConf) {
        this.gptConf = gptConf;
    }




    // 이미지를 Base64로 인코딩하는 함수
    private static String encodeImageToBase64(String imagePath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
        return Base64.getEncoder().encodeToString(imageBytes);
    }



    @Override
    public ThreadInfo createThreadAndRun(String question) throws URISyntaxException, IOException, InterruptedException {
        String apiKey = gptConf.getApiKey();
        Map<String, Object> requestBody = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        requestBody.put("assistant_id", ASSISTANT_ID);
        Map<String, Object> thread = new HashMap<>();
        thread.put("messages", List.of(
                Map.of(
                        "role", "user",
                        "content", question
                )
                )
        );
        requestBody.put("thread", thread);




        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.openai.com/v1/threads/runs"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("OpenAI-Beta", "assistants=v1")
                .POST(BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());


        Map responseMap = mapper.readValue(response.body(), Map.class);
        String runId = (String) responseMap.get("id");
        String threadId = (String) responseMap.get("thread_id");


        return ThreadInfo.builder()
                .runId(runId)
                .threadId(threadId)
                .build();
    }



    @Override
    public String imageAnalysis(String image, String question) throws URISyntaxException, IOException, InterruptedException {
        String apiKey = gptConf.getApiKey();
        Map<String, Object> requestBody = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        requestBody.put("model", "gpt-4-vision-preview");
        requestBody.put("max_tokens", 500);
        List<Map<String, Object>> messages = List.of(Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of("type", "text", "text", "동물의 사진을 분석해서 건강문제를 식별해줘. 너의 지식내로 조언을 해줘야해" + question),
                                Map.of("type", "image_url", "image_url", Map.of("url", image))
                        )
                )
        );
        requestBody.put("messages", messages);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        JsonNode rootNode = mapper.readTree(response.body());
        JsonNode contentNode = rootNode.path("choices").get(0).path("message").path("content");
        String content = contentNode.asText();


        return content;
    }



    @Override
    public String checkThreadStatus(String threadId, String runId) throws IOException, InterruptedException, URISyntaxException {
        String apiKey = gptConf.getApiKey();
        ObjectMapper mapper = new ObjectMapper();
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.openai.com/v1/threads/" + threadId + "/runs/" + runId))
                .header("Authorization", "Bearer " + apiKey)
                .header("OpenAI-Beta", "assistants=v1")
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        Map responseMap = mapper.readValue(response.body(), Map.class);
        String status = (String) responseMap.get("status");

//        System.out.println("Status: " + status);
        return status;
    }

    @Override
    public String getResponse(String threadId) throws URISyntaxException, IOException, InterruptedException {
        String apiKey = gptConf.getApiKey();
        ObjectMapper mapper = new ObjectMapper();
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.openai.com/v1/threads/" + threadId + "/messages"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("OpenAI-Beta", "assistants=v1")
                .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        Map responseMap = mapper.readValue(response.body(), Map.class);
        List<Map<String, Object>> data = (List<Map<String, Object>>) responseMap.get("data");
        Map<String, Object> firstItem = data.get(0);
        List<Map<String, Object>> content = (List<Map<String, Object>>) firstItem.get("content");
        Map<String, Object> textMap = (Map<String, Object>) content.get(0).get("text");
        String value = (String) textMap.get("value");

//        System.out.println(value);
        return value;
    }

    @Override
    public void deleteThread(String threadId) throws URISyntaxException, IOException, InterruptedException {
        String apiKey = gptConf.getApiKey();
        ObjectMapper mapper = new ObjectMapper();
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.openai.com/v1/threads/" + threadId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("OpenAI-Beta", "assistants=v1")
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response: " + response.body());

    }

/*

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException{
        String apiKey = "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";
        String question = "강아지의 털이 빠지고 각질이 생겨, 뭐가문제야? 종류는 푸들이고, 원래는 털이 잘 안빠져";
        String assistantId = "asst_9sZSXMHcaGeNCHvYvRSRbk41";
        String imagePath = "path_to_your_image.jpg";
        String threadId = "thread_gr3k43ctHDziIu8NxYyoarvG";
        String runId = "run_v2rfBgSvuAz0MFA3DyRx2pK0";
        // 이미지를 Base64로 인코딩
//        String base64Image = encodeImageToBase64(imagePath);

        ObjectMapper mapper = new ObjectMapper();


        String requestBody = "{"
                + "\"assistant_id\": \"" + ASSISTANT_ID + "\","
                + "\"thread\": {"
                + "  \"messages\": ["
                + "    {\"role\": \"user\", \"content\": \"" + question + "\"}"
                + "  ]"
                + "}"
                + "}";

        // 요청 본문을 위한 맵 생성
//        Map<String, Object> requestBodyMap = new HashMap<>();
//        requestBodyMap.put("assistant_id", assistantId);
//        Map<String, Object> thread = new HashMap<>();
//        thread.put("messages", List.of(
//                Map.of(
//                        "role", "user",
//                        "content", List.of(
//                                Map.of("type", "text", "text", question),
//                                Map.of("type", "image_url", "image_url", Map.of("url", "data:image/jpeg;base64," + base64Image))
//                        )
//                )
//        ));
//        requestBodyMap.put("thread", thread);
//
////         맵을 JSON 문자열로 변환
//        String requestBody = mapper.writeValueAsString(requestBodyMap);



//        HttpClient client = HttpClient.newHttpClient();
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(new URI("https://api.openai.com/v1/threads/runs"))
//                .header("Content-Type", "application/json")
//                .header("Authorization", "Bearer " + apiKey)
//                .header("OpenAI-Beta", "assistants=v1")
//                .POST(BodyPublishers.ofString(requestBody))
//                .build();
//
//        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
//
//        Map responseMap = mapper.readValue(response.body(), Map.class);
//        String runId1 = (String) responseMap.get("id");
//        String threadId1 = (String) responseMap.get("thread_id");
//
//        System.out.println("Response: " + response.body());
//        System.out.println("thread: " + threadId1);


        // 쓰레드 상태 확인
//        HttpClient client = HttpClient.newHttpClient();
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(new URI("https://api.openai.com/v1/threads/" + threadId + "/runs/" + runId))
//                .header("Authorization", "Bearer " + apiKey)
//                .header("OpenAI-Beta", "assistants=v1")
//                .build();
//
//        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

//        Map responseMap = mapper.readValue(response.body(), Map.class);
//        String status = (String) responseMap.get("status");
//
//        System.out.println("Status: " + status);



        // 응답 확인
//        HttpClient client = HttpClient.newHttpClient();
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(new URI("https://api.openai.com/v1/threads/" + threadId + "/messages"))
//                .header("Content-Type", "application/json")
//                .header("Authorization", "Bearer " + apiKey)
//                .header("OpenAI-Beta", "assistants=v1")
//                .build();
//
//        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
//        Map responseMap = mapper.readValue(response.body(), Map.class);
//        List<Map<String, Object>> data = (List<Map<String, Object>>) responseMap.get("data");
//        Map<String, Object> firstItem = data.get(0);
//        List<Map<String, Object>> content = (List<Map<String, Object>>) firstItem.get("content");
//        Map<String, Object> textMap = (Map<String, Object>) content.get(0).get("text");
//        String value = (String) textMap.get("value");
//
//        System.out.println(value);



        //쓰레드 삭제
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.openai.com/v1/threads/" + threadId))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("OpenAI-Beta", "assistants=v1")
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response: " + response.body());

    }

*/

}
