import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;



public class SenderAgent extends Agent {
    @Override
    protected void setup() {
        Object[] args = getArguments();
        String content = args[0].toString();

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("coordinator_agent", AID.ISLOCALNAME));
        msg.setConversationId("agents_count");
        msg.setContent(content);

        send(msg);
        System.out.println("[CLIENT] [SenderAgent] отправил количество [question_agent] [coordinator_agent]");

        doDelete();
    }
}
