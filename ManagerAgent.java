import java.util.List;
import java.io.FileWriter;
import java.util.ArrayList;

import jade.core.Agent;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;



public class ManagerAgent extends Agent {
    private int num_question;
    private double medium_difficulty = -1;
    private final List<Long> difficulties = new ArrayList<>();


    private void get_question_agents() {
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("question_agent");
            template.addServices(sd);

            DFAgentDescription[] result = DFService.search(this, template);

            num_question = result.length;

            for (DFAgentDescription question : result) {
                ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                message.addReceiver(question.getName());
                message.setConversationId("return_question");

                send(message);
                System.out.println("[" + getLocalName() + "] запросил 'вопрос' у [" + question.getName().getLocalName() + "]");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void get_difficulty (ACLMessage message) {
        try {
            System.out.println("[" + getLocalName() + "] получил 'вопрос' от [" + message.getSender().getLocalName() + "]");

            JSONParser parser = new JSONParser();
            JSONObject question = (JSONObject) parser.parse(message.getContent());

            long difficulty = (long) question.get("difficulty");
            difficulties.add(difficulty);

            if (difficulties.size() == num_question) {
                medium_difficulty = difficulties.stream().mapToInt(Long::intValue).sum() / (double) num_question;
                System.out.println("[" + getLocalName() + "] вычислил 'среднюю сложность' вопросов: " + medium_difficulty);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void save_ticket(JSONObject ticket_json) {
        try (FileWriter writer = new FileWriter("src/tickets.json", true)) {
            writer.write(ticket_json.toJSONString());
            writer.write("\n");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void setup(){
        System.out.println("[" + getLocalName() + "] запустился");

        try {
            DFAgentDescription df = new DFAgentDescription();
            df.setName(getAID());

            ServiceDescription sd = new ServiceDescription();
            sd.setType("manager_agent");
            sd.setName(getLocalName());
            df.addServices(sd);

            DFService.register(this, df);
            System.out.println("[" + getLocalName() + "] зарегистрировался в DF как [" + getLocalName() + "]");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        get_question_agents();


        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage message = receive();

                if (message != null){
                    if ("get_question".equals(message.getConversationId())){
                        get_difficulty(message);
                    }

                    if ("return_medium_difficulty".equals(message.getConversationId())){
                        System.out.println("[" + getLocalName() + "] получил запрос от [" + message.getSender().getLocalName() + "]");

                        ACLMessage reply = message.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setConversationId("get_medium_difficulty");
                        reply.setContent("" + medium_difficulty);

                        send(reply);

                        System.out.println("[" + getLocalName() + "] отправил ответ [" + message.getSender().getLocalName() + "]; Содержание ответа: " + medium_difficulty);
                    }

                    if ("return_ticket".equals(message.getConversationId())) {
                        System.out.println("[" + getLocalName() + "] получил 'билет' [" + message.getSender().getLocalName() + "]; Содержание ответа: " + medium_difficulty);

                        Question question_1 = new Question(message.getContent().split(";")[0]);
                        Question question_2 = new Question(message.getContent().split(";")[1]);

                        JSONObject ticket_json = new JSONObject();
                        JSONArray questions = new JSONArray();

                        JSONObject question_1_json = new JSONObject();
                        question_1_json.put("id", question_1.getId());
                        question_1_json.put("difficulty", question_1.getDifficulty());
                        question_1_json.put("topic", question_1.getTopic());

                        JSONObject question_2_json = new JSONObject();
                        question_2_json.put("id", question_2.getId());
                        question_2_json.put("difficulty", question_2.getDifficulty());
                        question_2_json.put("topic", question_2.getTopic());

                        questions.add(question_1_json);
                        questions.add(question_2_json);

                        ticket_json.put("ticket", message.getSender().getLocalName());
                        ticket_json.put("questions", questions);

                        save_ticket(ticket_json);

                        System.out.println("[" + getLocalName() + "] сохранил 'билет' [" + message.getSender().getLocalName() + "]");
                    }
                }
                else {
                    block();
                }
            }
        });
    }


    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("[" + getLocalName() + "] завершил работу");
    }
}
