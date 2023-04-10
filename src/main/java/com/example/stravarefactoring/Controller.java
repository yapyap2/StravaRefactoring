package com.example.stravarefactoring;

import com.example.stravarefactoring.DTO.Token;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

@RestController
public class Controller {

    @GetMapping("/request")
    public void userRequest(@RequestParam("code") String code){
        HttpURLConnection connection;

        Token token;

        try {
            URL url = new URL(ApiAddress.stravaGetToken(code));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            String res = JsonReader.readJson(connection);

            ObjectMapper mapper = new ObjectMapper();

            token = mapper.readValue(res, Token.class);
        } catch (IOException e) {
            throw new RuntimeException(e);   /** 스트라바 API 오류 예외처리 필요 **/
        }

        System.out.println(token);

    }

}
