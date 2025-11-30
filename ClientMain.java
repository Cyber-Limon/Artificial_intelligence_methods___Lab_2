import java.util.List;
import java.io.FileReader;
import java.util.ArrayList;

import jade.core.Profile;
import jade.core.Runtime;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;



public class ClientMain {
    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();

            Profile prof = new ProfileImpl();
            prof.setParameter(Profile.MAIN_HOST, "192.168.1.10");
            prof.setParameter(Profile.MAIN_PORT, "1099");
            prof.setParameter(Profile.CONTAINER_NAME, "ClientContainer");

            AgentContainer clientContainer = rt.createAgentContainer(prof);

            JSONParser parser = new JSONParser();
            JSONArray questions = (JSONArray) parser.parse(new FileReader("src/questions.json"));

            List<String> question_agent_names = new ArrayList<>();

            for (Object question : questions) {
                JSONObject q = (JSONObject) question;
                long id = (long) q.get("id");
                String name = "question_agent" + id;
                question_agent_names.add(name);

                AgentController question_agent = clientContainer.createNewAgent(
                        name,
                        "QuestionAgent",
                        new Object[]{q.toJSONString()}
                );

                question_agent.start();
                System.out.println("[" + name + "] запускается");

                Thread.sleep(10);
            }

            AgentController sender = clientContainer.createNewAgent(
                    "sender_agent",
                    "SenderAgent",
                    new Object[]{ String.valueOf(questions.size()) }
            );

            sender.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
