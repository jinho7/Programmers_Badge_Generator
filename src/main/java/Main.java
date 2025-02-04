import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    private static final String PROGRAMMERS_SIGN_IN = "https://programmers.co.kr/api/v1/account/sign-in";
    private static final String PROGRAMMERS_USER_RECORD = "https://programmers.co.kr/api/v1/users/record";

    public static void main(String[] args) {
        try {
            // 환경 변수를 가져옴
            String id = System.getenv("PROGRAMMERS_TOKEN_ID");
            String pw = System.getenv("PROGRAMMERS_TOKEN_PW");

            if (id == null || pw == null) {
                System.out.println("환경 변수 PROGRAMMERS_TOKEN_ID 와 PROGRAMMERS_TOKEN_PW를 설정해주세요.");
                return;
            }

            HttpClient client = HttpClient.newHttpClient();

            // 프로그래머스 로그인
            String signInPayload = String.format("{\"user\": {\"email\": \"%s\", \"password\": \"%s\"}}", id, pw);
            HttpRequest signInRequest = HttpRequest.newBuilder()
                    .uri(new URI(PROGRAMMERS_SIGN_IN))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(signInPayload))
                    .build();

            HttpResponse<String> signInResponse = client.send(signInRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println(signInResponse);
            if (signInResponse.statusCode() != 200) {
                System.err.println("로그인 실패: " + signInResponse.body());
                return;
            }

            // 모든 쿠키를 추출
            List<String> cookies = signInResponse.headers().allValues("set-cookie").stream()
                    .map(cookie -> cookie.split(";", 2)[0])  // 쿠키 값만 추출
                    .collect(Collectors.toList());
            String cookiesHeader = String.join("; ", cookies);

            // 사용자 정보 요청
            HttpRequest userRecordRequest = HttpRequest.newBuilder()
                    .uri(new URI(PROGRAMMERS_USER_RECORD))
                    .header("Cookie", cookiesHeader)
                    .GET()
                    .build();

            HttpResponse<String> userRecordResponse = client.send(userRecordRequest, HttpResponse.BodyHandlers.ofString());

            if (userRecordResponse.statusCode() != 200) {
                System.err.println("----");
                System.err.println(userRecordResponse);
                System.err.println("사용자 정보 요청 실패: " + userRecordResponse.body());
                return;
            }

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> myData = objectMapper.readValue(userRecordResponse.body(), Map.class);

            // svg 뱃지 생성
            if (myData != null) {
                // 데이터를 안전하게 가져오기 위해 변수에 저장
                Object level = ((Map<String, Object>) myData.get("skillCheck")).get("level");
                Object score = ((Map<String, Object>) myData.get("ranking")).get("score");
                Object solved = ((Map<String, Object>) myData.get("codingTest")).get("solved");
                Object rank = ((Map<String, Object>) myData.get("ranking")).get("rank");

                // 데이터를 String 타입으로 변환하여 안전하게 사용
                String svgContent = String.format(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
                    "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"551px\" height=\"217px\" style=\"shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">" +
                    "<style>" +
                    ".title {" +
                    "fill: #434343;" +
                    "font-size: 1rem;" +
                    "line-height: 1.5rem;" +
                    "font-weight: bold;" +
                    "font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Helvetica, Arial, sans-serif, Apple Color Emoji, Segoe UI Emoji;" +
                    "}" +
                    ".desc {" +
                    "fill: #434343;" +
                    "font-size: 2.5rem;" +
                    "font-weight: bold;" +
                    "line-height: 1.5rem;" +
                    "font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Helvetica, Arial, sans-serif, Apple Color Emoji, Segoe UI Emoji;" +
                    "}" +
                    ".desc-2 {" +
                    "fill: #434343;" +
                    "font-size: 1rem;" +
                    "font-weight: bold;" +
                    "line-height: 1.5rem;" +
                    "font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Helvetica, Arial, sans-serif, Apple Color Emoji, Segoe UI Emoji;" +
                    "}" +
                    "</style>" +
                    
                    "<g><path style=\"opacity:1\" fill=\"#fefefe\" d=\"M -0.5,-0.5 C 183.167,-0.5 366.833,-0.5 550.5,-0.5C 550.5,71.8333 550.5,144.167 550.5,216.5C 366.833,216.5 183.167,216.5 -0.5,216.5C -0.5,212.167 -0.5,207.833 -0.5,203.5C 1.14011,204.32 2.80678,205.153 4.5,206C 88.1667,206.667 171.833,206.667 255.5,206C 259,205.167 261.167,203 262,199.5C 262.667,175.167 262.667,150.833 262,126.5C 261.167,123 259,120.833 255.5,120C 171.833,119.333 88.1667,119.333 4.5,120C 2.80678,120.847 1.14011,121.68 -0.5,122.5C -0.5,115.167 -0.5,107.833 -0.5,100.5C 1.14011,101.32 2.80678,102.153 4.5,103C 88.1667,103.667 171.833,103.667 255.5,103C 259,102.167 261.167,100 262,96.5C 262.667,72.1667 262.667,47.8333 262,23.5C 261.167,20 259,17.8333 255.5,17C 171.833,16.3333 88.1667,16.3333 4.5,17C 2.80678,17.8466 1.14011,18.6799 -0.5,19.5C -0.5,12.8333 -0.5,6.16667 -0.5,-0.5 Z\"/></g>\n" +
                    "<g><path style=\"opacity:1\" fill=\"#ecf5ff\" d=\"M -0.5,100.5 C -0.5,73.5 -0.5,46.5 -0.5,19.5C 1.14011,18.6799 2.80678,17.8466 4.5,17C 88.1667,16.3333 171.833,16.3333 255.5,17C 259,17.8333 261.167,20 262,23.5C 262.667,47.8333 262.667,72.1667 262,96.5C 261.167,100 259,102.167 255.5,103C 171.833,103.667 88.1667,103.667 4.5,103C 2.80678,102.153 1.14011,101.32 -0.5,100.5 Z\"/></g>\n" +
                    "<g><path style=\"opacity:1\" fill=\"#ecf5ff\" d=\"M 285.5,16.5 C 369.167,16.3333 452.834,16.5 536.5,17C 540,17.8333 542.167,20 543,23.5C 543.667,47.8333 543.667,72.1667 543,96.5C 542.167,100 540,102.167 536.5,103C 452.833,103.667 369.167,103.667 285.5,103C 282,102.167 279.833,100 279,96.5C 278.333,72.1667 278.333,47.8333 279,23.5C 280.018,19.9853 282.185,17.652 285.5,16.5 Z\"/></g>\n" +
                    
                    "<text text-anchor=\"middle\" x=\"80\" y=\"45\" class=\"title\" style=\"fill:#0078ff;\" stroke=\"#none\" stroke-width=\"1\" >나의 랭킹</text>" +
                    "<text text-anchor=\"middle\" x=\"60\" y=\"85\" class=\"desc\" style=\"fill:#000000;\" stroke=\"#none\" stroke-width=\"1\" >%s</text>" +
                    "<text text-anchor=\"middle\" x=\"100\" y=\"85\" class=\"desc-2\" style=\"fill:#434343;\" stroke=\"#none\" stroke-width=\"1\" >위</text>" +
                    
                    "<text text-anchor=\"middle\" x=\"340\" y=\"45\" class=\"title\" style=\"fill:#0078ff;\" stroke=\"#none\" stroke-width=\"1\" >현재 점수</text>" +
                    "<text text-anchor=\"middle\" x=\"360\" y=\"85\" class=\"desc\" style=\"fill:#000000;\" stroke=\"#none\" stroke-width=\"1\" >%s</text>" +
                    "<text text-anchor=\"middle\" x=\"450\" y=\"85\" class=\"desc-2\" style=\"fill:#434343;\" stroke=\"#none\" stroke-width=\"1\" >점</text>" +
                    
                    "<text text-anchor=\"middle\" x=\"100\" y=\"150\" class=\"title\" style=\"fill:#0078ff;\" stroke=\"#none\" stroke-width=\"1\" >해결한 코딩 테스트</text>" +
                    "<text text-anchor=\"middle\" x=\"65\" y=\"190\" class=\"desc\" style=\"fill:#000000;\" stroke=\"#none\" stroke-width=\"1\" >%s</text>" +
                    "<text text-anchor=\"middle\" x=\"120\" y=\"190\" class=\"desc-2\" style=\"fill:#434343;\" stroke=\"#none\" stroke-width=\"1\" >문제</text>" +
                    
                    "</svg>",
                    rank != null ? rank.toString() : "",
                    score != null ? score.toString() : "",
                    solved != null ? solved.toString() : ""
                );



                Path currentPath = Paths.get("").toAbsolutePath();
                System.out.println("현재 작업 디렉토리: " + currentPath);

                Path fileDirectory = Paths.get("./result");
                if (!Files.exists(fileDirectory)) {
                    Files.createDirectories(fileDirectory);  // dist 디렉토리가 없으면 생성
                }

                Path templateFile = fileDirectory.resolve("result.svg");
                if (!Files.exists(templateFile)) {
                    Files.writeString(templateFile, "");  // result 파일이 없으면 빈 파일 생성
                }

                Path resultFile = fileDirectory.resolve("result.svg");

                // svg 뱃지 파일 생성
                Files.writeString(resultFile, svgContent);
                System.out.println("뱃지 생성 성공");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
