import jade.core.Agent;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.Objects;



public class QuestionAgent extends Agent {
    private boolean used = false;
    private String question;


    @Override
    protected void setup(){
        Object[] args = getArguments();
        if (args != null && args.length != 0) {
            question = (String) args[0];
        }

        System.out.println("[" + getLocalName() + "] запустился");

        try {
            DFAgentDescription df = new DFAgentDescription();
            df.setName(getAID());

            ServiceDescription sd = new ServiceDescription();
            sd.setType("question_agent");
            sd.setName(getLocalName());
            df.addServices(sd);

            DFService.register(this, df);
            System.out.println("[" + getLocalName() + "] зарегистрировался в DF как [" + getLocalName() + "]");
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage message = receive();

                if (message != null){
                    if ("return_question".equals(message.getConversationId())){
                        System.out.println("[" + getLocalName() + "] получил запрос от [" + message.getSender().getLocalName() + "]");

                        if (!used) {
                            used = true;
                            System.out.println("[" + getLocalName() + "] включился в [" + message.getSender().getLocalName() + "]");
                        }

                        ACLMessage reply = message.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setConversationId("get_question");
                        reply.setContent(question);

                        send(reply);
                        System.out.println("[" + getLocalName() + "] отправил ответ [" + message.getSender().getLocalName() + "]; Содержание ответа: " + question);
                    }

                    if (message.getConversationId().equals("reject_question")){
                        used = false;
                        System.out.println("[" + getLocalName() + "] не включился в [" + message.getSender().getLocalName() + "]");
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
