import jade.core.Agent;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;



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

                        ACLMessage reply = message.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setConversationId("used_" + used);
                        reply.setContent(question);

                        if (!message.getSender().getLocalName().equals("manager_agent")){
                            used = true;
                        }

                        send(reply);
                        System.out.println("[" + getLocalName() + "] отправил ответ [" + message.getSender().getLocalName() + "] на [" + reply.getConversationId() + "]; Содержание ответа: " + question);
                    }

                    if (message.getConversationId().equals("accept_question")){
                        System.out.println("[" + getLocalName() + "] включился в [" + message.getSender().getLocalName() + "]");
                        takeDown();
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
