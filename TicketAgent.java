import java.util.Map;
import java.util.HashMap;
import java.io.FileWriter;
import java.io.FileReader;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;



public class TicketAgent extends Agent {
    private final Map<Long, JSONObject> questions_json = new HashMap<>();


    private void load_questions() {
        try {
            JSONParser parser = new JSONParser();
            JSONArray questions = (JSONArray) parser.parse(new FileReader("src/questions.json"));

            for (Object question : questions) {
                JSONObject q = (JSONObject) question;
                long id = (long) q.get("id");
                questions_json.put(id, q);
            }

            System.out.println("[" + getLocalName() + "] загрузил вопросы [" + questions_json.size() + "]");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void write_line(String text) {
        try (FileWriter writer = new FileWriter("src/tickets.json", true)) {
            writer.write(text + "\n");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void setup() {
        System.out.println("[" + getLocalName() + "] запущен");

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    try {
                        System.out.println("[" + getLocalName() + "] получил сообщение от [" + msg.getSender().getLocalName() + "]");

                        load_questions();

                        try (FileWriter writer = new FileWriter("src/tickets.json")) {
                            writer.write("");
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }

                        JSONParser parser = new JSONParser();
                        JSONObject wrapper = (JSONObject) parser.parse(msg.getContent());
                        JSONArray tickets = (JSONArray) wrapper.get("tickets");

                        for (Object obj : tickets) {
                            JSONObject pair = (JSONObject) obj;

                            long id1 = (long) pair.get("q1");
                            long id2 = (long) pair.get("q2");

                            JSONObject ticket = new JSONObject();
                            ticket.put("Вопрос 1", questions_json.get(id1));
                            ticket.put("Вопрос 2", questions_json.get(id2));

                            write_line(ticket.toJSONString());

                            System.out.println("[" + getLocalName() + "] сохранён билет: " + ticket);
                        }

                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setConversationId("ticket_agent");
                        reply.setContent("ответ от [" + getAID().getLocalName() + "]");

                        send(reply);
                        System.out.println("[" + getLocalName() + "] отправил ответ [" + msg.getSender().getLocalName() + "]");
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
}
