import java.io.FileReader;

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
            prof.setParameter(Profile.MAIN_HOST, "10.33.0.2");
            prof.setParameter(Profile.MAIN_PORT, "1099");
            prof.setParameter(Profile.LOCAL_HOST, "10.33.0.2");
            prof.setParameter(Profile.LOCAL_PORT, "1100");
            prof.setParameter(Profile.CONTAINER_NAME, "ClientContainer");

            AgentContainer ClientContainer = rt.createAgentContainer(prof);

            JSONParser parser = new JSONParser();
            JSONArray questions = (JSONArray) parser.parse(new FileReader("src/questions.json"));

            for (Object question : questions) {
                JSONObject q = (JSONObject) question;
                long id = (long) q.get("id");
                String name = "question_agent" + id;

                AgentController question_agent = ClientContainer.createNewAgent(
                        name,
                        "QuestionAgent",
                        new Object[]{q.toJSONString()}
                );

                question_agent.start();
                System.out.println("[" + name + "] запускается");

                Thread.sleep(10);
            }

            AgentController manager_agent = ClientContainer.createNewAgent(
                    "manager_agent",
                    "ManagerAgent",
                    null
            );

            manager_agent.start();
            System.out.println("[manager_agent] запускается");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
