import jade.core.Agent;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Objects;



public class TicketAgent extends Agent {
    private double medium_difficulty = -1;
    private Question question_1;
    private Question question_2;


    private void get_manager_agent() {
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("manager_agent");
            template.addServices(sd);

            DFAgentDescription[] result = DFService.search(this, template);

            if (result.length != 0) {
                ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                message.addReceiver(result[0].getName());

                if (medium_difficulty == -1) {
                    message.setConversationId("return_medium_difficulty");

                    send(message);
                    System.out.println("[" + getLocalName() + "] запросил 'среднюю сложность' у [manager_agent]");
                }
                else {
                    message.setConversationId("return_ticket");
                    message.setContent(question_1.toString() + ";" + question_2.toString());

                    send(message);
                    System.out.println("[" + getLocalName() + "] отправил 'билет' [manager_agent]");
                }
            }
            else {
                System.out.println("[" + getLocalName() + "] не нашел [manager_agent]");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void get_question_agents() {
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("question_agent");
            template.addServices(sd);

            DFAgentDescription[] result = DFService.search(this, template);

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


    private void questions_processing(ACLMessage message) {
        try {
            System.out.println("[" + getLocalName() + "] получил 'вопрос' от [" + message.getSender().getLocalName() + "]");

            JSONParser parser = new JSONParser();
            JSONObject question = (JSONObject) parser.parse(message.getContent());

            long id = (long) question.get("id");
            long difficulty = (long) question.get("difficulty");
            String topic = (String) question.get("topic");

            if (question_1 == null) {
                question_1 = new Question(id, difficulty, topic);
                System.out.println("[" + getLocalName() + "] сохранил 'вопрос' от [" + message.getSender().getLocalName() + "]");
            }
            else if ((question_2 == null) && ((question_1.getDifficulty() + difficulty) / 2.0 == medium_difficulty) && (!Objects.equals(question_1.getTopic(), topic))){
                question_2 = new Question(id, difficulty, topic);
                System.out.println("[" + getLocalName() + "] сохранил 'вопрос' от [" + message.getSender().getLocalName() + "]");
            }
            else {
                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                reply.setConversationId("reject_question");

                send(reply);
                System.out.println("[" + getLocalName() + "] не сохранил 'вопрос' от [" + message.getSender().getLocalName() + "]");
            }
        }
        catch (Exception e){
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
            sd.setType("ticket_agent");
            sd.setName(getLocalName());
            df.addServices(sd);

            DFService.register(this, df);
            System.out.println("[" + getLocalName() + "] зарегистрировался в DF как [" + getLocalName() + "]");
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                if (medium_difficulty == -1) {
                    get_manager_agent();
                }
                else if (question_2 == null) {
                    get_question_agents();
                }
                else {
                    get_manager_agent();
                    stop();
                }
            }
        });


        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage message = receive();

                if (message != null){
                    if ("get_medium_difficulty".equals(message.getConversationId())) {
                        System.out.println("[" + getLocalName() + "] получил 'среднюю сложность' от [" + message.getSender().getLocalName() + "]");

                        medium_difficulty = Double.parseDouble(message.getContent());
                    }

                    if ("get_question".equals(message.getConversationId())) {
                        questions_processing(message);
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
