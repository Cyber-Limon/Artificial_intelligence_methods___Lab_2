import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;



public class CoordinatorAgent extends Agent {
    private int question_agents_num;
    private final List<Question> questions = new ArrayList<>();


    private int question_agent_number() {
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("question_agent");
            template.addServices(sd);

            DFAgentDescription[] result = DFService.search(this, template);

            return result.length;
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    private void receiving_questions() {
        addBehaviour(new OneShotBehaviour(this) {
            @Override
            public void action() {
                try {
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("question_agent");
                    template.addServices(sd);

                    DFAgentDescription[] result = DFService.search(myAgent, template);

                    for (DFAgentDescription dfd : result) {
                        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                        msg.addReceiver(dfd.getName());
                        msg.setContent("Запрос данных");

                        send(msg);

                        System.out.println("[" + getLocalName() + "] отправил запрос к [" + dfd.getName().getLocalName() + "]");

                        Thread.sleep(100);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private List<List<Question>> pairing(List<Question> questions) {
        long sum = questions.stream().mapToLong(Question::getDifficulty).sum();
        long count = questions.size();
        double medium_difficulty = (double) sum / count;

        List<Question> used = new ArrayList<>();
        List<List<Question>> couples = new ArrayList<>();

        for (Question i : questions) {
            if (!used.contains(i)) {
                for (Question j : questions) {
                    if (!used.contains(j)){
                        if ((i != j) && ((i.getDifficulty() + j.getDifficulty()) / 2.0 == medium_difficulty) && (!Objects.equals(i.getTopic(), j.getTopic()))){
                            used.add(i);
                            used.add(j);

                            List<Question> pair = new ArrayList<>();
                            pair.add(i);
                            pair.add(j);
                            couples.add(pair);

                            break;
                        }
                    }
                }
            }
        }

        return couples;
    }


    private void questions_processing(ACLMessage msg) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject question_fields = (JSONObject) parser.parse(msg.getContent());

            long id = (long) question_fields.get("id");
            long difficulty = (long) question_fields.get("difficulty");
            String topic = (String) question_fields.get("topic");

            Question question = new Question(id, difficulty, topic);
            questions.add(question);

            System.out.println("[" + getLocalName() + "] получил ответ от [" +
                    msg.getSender().getLocalName() + "]; Содержание ответа: " + question.toString());

            if (questions.size() == question_agents_num) {
                System.out.println("[" + getLocalName() + "] получил все вопросы [" + questions.size() + "]");

                List<List<Question>> couples = pairing(questions);
                send_tickets(couples);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    private String couples_json(List<List<Question>> couples) {
        JSONArray tickets = new JSONArray();

        for (List<Question> pair : couples) {
            JSONObject obj = new JSONObject();

            obj.put("q1", pair.get(0).getId());
            obj.put("q2", pair.get(1).getId());
            tickets.add(obj);
        }

        JSONObject wrapper = new JSONObject();
        wrapper.put("tickets", tickets);
        return wrapper.toJSONString();
    }


    private void send_tickets(List<List<Question>> couples) {
        String json = couples_json(couples);

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("ticket_agent", AID.ISLOCALNAME));
        msg.setContent(json);

        send(msg);
        System.out.println("[" + getLocalName() + "] отправил билеты [ticket_agent]");
    }


    @Override
    protected void setup() {
        System.out.println("[" + getLocalName() + "] запущен");

        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());

            ServiceDescription sd = new ServiceDescription();
            sd.setType("coordinator_agent");
            sd.setName(getLocalName());
            dfd.addServices(sd);

            DFService.register(this, dfd);
            System.out.println("[" + getLocalName() + "] зарегистрирован в DF как [coordinator_agent]");
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {

                int ready = question_agent_number();
                int need = question_agents_num;

                System.out.println("[" + getLocalName() + "] готово [question_agent]: " + ready + " / " + need);

                if (ready == need) {
                    System.out.println("[" + getLocalName() + "] все [question_agent] готовы");

                    receiving_questions();

                    stop();
                }
            }
        });


        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    try {
                        if ("question_agent".equals(msg.getConversationId()))
                            questions_processing(msg);

                        if ("ticket_agent".equals(msg.getConversationId())) {
                            System.out.println("[" + getLocalName() + "] получил подтверждение от [" + msg.getSender().getLocalName() + "]");
                        }

                        if ("agents_count".equals(msg.getConversationId())) {
                            question_agents_num = Integer.parseInt(msg.getContent());
                            System.out.println("[" + getLocalName() + "] получил количество агентов: " + question_agents_num);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
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
