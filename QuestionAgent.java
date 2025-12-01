import jade.core.Agent;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;



public class QuestionAgent extends Agent {
    private String question;

    @Override
    protected void setup() {
        System.out.println("[" + getLocalName() + "] запущен");

        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());

            ServiceDescription sd = new ServiceDescription();
            sd.setType("question_agent");
            sd.setName(getLocalName());
            dfd.addServices(sd);

            DFService.register(this, dfd);

            System.out.println("[" + getLocalName() + "] зарегистрирован в DF как [" + getLocalName() + "]");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Object[] args = getArguments();
        if (args != null && args.length != 0) {
            question = (String) args[0];
        }

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg != null) {
                    System.out.println("[" + getLocalName() + "] получил сообщение от [" + msg.getSender().getLocalName() + "]");

                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setConversationId("question_agent");
                    reply.setContent(question);

                    send(reply);
                    System.out.println(
                            "[" + getLocalName() + "] отправил ответ [" + msg.getSender().getLocalName() + "]; Содержание ответа: " + question);
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
