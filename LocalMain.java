import java.util.List;
import java.io.FileReader;
import java.util.ArrayList;

import jade.core.Profile;
import jade.core.Runtime;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;



public class LocalMain {
    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();

            Profile prof = new ProfileImpl();
            prof.setParameter(Profile.GUI, "true");
            ContainerController container = rt.createMainContainer(prof);



            // Запуск question_agent //

            JSONParser parser = new JSONParser();
            JSONArray questions = (JSONArray) parser.parse(new FileReader("src/questions.json"));

            List<String> question_agent_names = new ArrayList<>();

            for (Object question : questions) {
                JSONObject q = (JSONObject) question;
                long id = (long) q.get("id");
                String name = "question_agent" + id;
                question_agent_names.add(name);

                AgentController question_agent = container.createNewAgent(
                        name,
                        "QuestionAgent",
                        new Object[]{q.toJSONString()}
                );

                question_agent.start();
                System.out.println("[" + name + "] запускается");

                Thread.sleep(10);
            }

            Object[] question_agents = question_agent_names.toArray(new String[0]);



            // Запуск ticket_agent //

            AgentController ticket_agent = container.createNewAgent(
                    "ticket_agent",
                    "TicketAgent",
                    null
            );

            ticket_agent.start();
            System.out.println("[ticket_agent] запускается");



            // Запуск coordinator_agent //

            AgentController coordinator_agent = container.createNewAgent(
                    "coordinator_agent",
                    "CoordinatorAgent",
                    question_agents
            );

            coordinator_agent.start();
            System.out.println("[coordinator_agent] запускается");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
